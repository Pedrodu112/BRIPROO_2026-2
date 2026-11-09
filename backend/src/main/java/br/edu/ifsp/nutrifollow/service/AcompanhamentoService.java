
package br.edu.ifsp.nutrifollow.service;

import br.edu.ifsp.nutrifollow.entity.Acompanhamento;
import br.edu.ifsp.nutrifollow.entity.Profissional;
import br.edu.ifsp.nutrifollow.entity.Usuario;
import br.edu.ifsp.nutrifollow.entity.UsuarioComum;
import br.edu.ifsp.nutrifollow.repository.AcompanhamentoRepository;
import br.edu.ifsp.nutrifollow.repository.UsuarioRepository;

import br.edu.ifsp.nutrifollow.exception.RecursoNaoEncontradoException;
import br.edu.ifsp.nutrifollow.exception.OperacaoNaoPermitidaException;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcompanhamentoService {

    private final AcompanhamentoRepository acompanhamentoRepository;
    private final UsuarioRepository usuarioRepository;

    public AcompanhamentoService(
            AcompanhamentoRepository acompanhamentoRepository,
            UsuarioRepository usuarioRepository) {
        this.acompanhamentoRepository = acompanhamentoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Acompanhamento solicitar(
            Long usuarioId,
            Long profissionalId,
            String finalidade) {

        Usuario usuarioEncontrado = buscarUsuarioAtivo(usuarioId);
        Usuario profissionalEncontrado = buscarUsuarioAtivo(profissionalId);

        if (!(usuarioEncontrado instanceof UsuarioComum usuario)) {
            throw new IllegalArgumentException(
                    "O solicitante precisa ser um usuário comum."
            );
        }

        if (!(profissionalEncontrado instanceof Profissional profissional)) {
            throw new IllegalArgumentException(
                    "O destinatário precisa ser um profissional."
            );
        }

        if (finalidade == null || finalidade.isBlank()) {
            throw new IllegalArgumentException(
                "A finalidade do acompanhamento é obrigatória."
            );
        }

        String finalidadeNormalizada = finalidade.trim();

        boolean jaExisteAtivo =
        acompanhamentoRepository.existsByUsuario_IdAndProfissional_IdAndFinalidadeIgnoreCaseAndStatus(
            usuario.getId(),
            profissional.getId(),
            finalidadeNormalizada,
            "ATIVO"
        );

        if (jaExisteAtivo) {
            throw new IllegalStateException(
                "Já existe um acompanhamento ativo para este profissional e finalidade."
            );
        }

        Acompanhamento acompanhamento =
            usuario.solicitarAcompanhamento(
                profissional,
                finalidadeNormalizada
            );

        return acompanhamentoRepository.save(acompanhamento);
    }

    @Transactional(readOnly = true)
    public List<Acompanhamento> listarPorUsuario(Long usuarioId) {
        Usuario usuario = buscarUsuarioAtivo(usuarioId);

        return acompanhamentoRepository.findByUsuario_Id(usuario.getId());
    }

    @Transactional(readOnly = true)
    public List<Acompanhamento> listarPorProfissional(Long profissionalId) {
        Usuario usuario = buscarUsuarioAtivo(profissionalId);

        if (!(usuario instanceof Profissional profissional)) {
            throw new IllegalArgumentException(
                    "O usuário informado não é um profissional."
            );
        }

        return acompanhamentoRepository.findByProfissional_Id(profissional.getId());
    }

    @Transactional
    public Acompanhamento aceitar(Integer acompanhamentoId, Long profissionalId) {
        Acompanhamento acompanhamento = buscarAcompanhamento(acompanhamentoId);
        Usuario usuario = buscarUsuarioAtivo(profissionalId);

        if (!(usuario instanceof Profissional profissional)) {
            throw new OperacaoNaoPermitidaException(
                "Somente profissionais podem aceitar acompanhamentos."
            );
        }

        acompanhamento.aceitar(profissional);
        return acompanhamentoRepository.save(acompanhamento);
    }

    @Transactional
    public Acompanhamento recusar(
            Integer acompanhamentoId,
            Long profissionalId,
            String motivo) {

        Acompanhamento acompanhamento = buscarAcompanhamento(acompanhamentoId);
        Usuario usuario = buscarUsuarioAtivo(profissionalId);

        if (!(usuario instanceof Profissional profissional)) {
            throw new OperacaoNaoPermitidaException(
                "Somente profissionais podem aceitar acompanhamentos."
            );
        }

        acompanhamento.recusar(profissional, motivo);
        return acompanhamentoRepository.save(acompanhamento);
    }

    @Transactional
    public Acompanhamento autorizarCompartilhamento(
            Integer acompanhamentoId,
            Long usuarioId,
            List<String> permissoes) {

        Acompanhamento acompanhamento = buscarAcompanhamento(acompanhamentoId);
        Usuario usuarioEncontrado = buscarUsuarioAtivo(usuarioId);

        if (!(usuarioEncontrado instanceof UsuarioComum usuario)) {
            throw new OperacaoNaoPermitidaException(
                "Somente usuários comuns podem autorizar o compartilhamento de dados."
            );
        }

        acompanhamento.autorizarCompartilhamento(usuario, permissoes);
        return acompanhamentoRepository.save(acompanhamento);
    }

    @Transactional
    public Acompanhamento encerrar(
            Integer acompanhamentoId,
            Long autorId,
            String motivo) {

        Acompanhamento acompanhamento = buscarAcompanhamento(acompanhamentoId);
        Usuario autor = buscarUsuarioAtivo(autorId);

        acompanhamento.encerrar(autor, motivo);
        return acompanhamentoRepository.save(acompanhamento);
    }

    private Acompanhamento buscarAcompanhamento(Integer acompanhamentoId) {
        if (acompanhamentoId == null) {
            throw new IllegalArgumentException(
                    "O identificador do acompanhamento é obrigatório."
            );
        }

        return acompanhamentoRepository.findById(acompanhamentoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Acompanhamento não encontrado."
            ));
    }

    private Usuario buscarUsuarioAtivo(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException(
                    "O identificador do usuário é obrigatório."
            );
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Usuário não encontrado."
            ));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new IllegalStateException("O usuário está inativo.");
        }

        return usuario;
    }
}
