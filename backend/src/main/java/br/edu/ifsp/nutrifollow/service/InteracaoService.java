
package br.edu.ifsp.nutrifollow.service;

import br.edu.ifsp.nutrifollow.entity.Acompanhamento;
import br.edu.ifsp.nutrifollow.entity.Interacao;
import br.edu.ifsp.nutrifollow.entity.Usuario;
import br.edu.ifsp.nutrifollow.repository.AcompanhamentoRepository;
import br.edu.ifsp.nutrifollow.repository.InteracaoRepository;
import br.edu.ifsp.nutrifollow.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteracaoService {

    private final InteracaoRepository interacaoRepository;
    private final AcompanhamentoRepository acompanhamentoRepository;
    private final UsuarioRepository usuarioRepository;

    public InteracaoService(
            InteracaoRepository interacaoRepository,
            AcompanhamentoRepository acompanhamentoRepository,
            UsuarioRepository usuarioRepository) {
        this.interacaoRepository = interacaoRepository;
        this.acompanhamentoRepository = acompanhamentoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Interacao enviar(
            Long acompanhamentoId,
            Long autorId,
            Long destinatarioId,
            String tipo,
            String conteudo,
            LocalDateTime prazo,
            String situacaoMeta) {

        Acompanhamento acompanhamento = buscarAcompanhamento(
                acompanhamentoId
        );

        Usuario autor = buscarUsuarioAtivo(autorId);
        Usuario destinatario = buscarUsuarioAtivo(destinatarioId);

        Interacao interacao = new Interacao();

        interacao.setAcompanhamento(acompanhamento);
        interacao.setAutor(autor);
        interacao.setDestinatario(destinatario);

        if (tipo != null) {
            interacao.setTipo(tipo.trim().toUpperCase(Locale.ROOT));
        }

        interacao.setConteudo(conteudo);
        interacao.setPrazo(prazo);
        interacao.setSituacaoMeta(situacaoMeta);

        interacao.enviar();

        return interacaoRepository.save(interacao);
    }

    @Transactional(readOnly = true)
    public List<Interacao> listarPorAcompanhamento(
            Long acompanhamentoId,
            Long usuarioId) {

        Acompanhamento acompanhamento = buscarAcompanhamento(
                acompanhamentoId
        );

        Usuario usuario = buscarUsuarioAtivo(usuarioId);

        validarParticipante(acompanhamento, usuario);

        return interacaoRepository.findAll()
        .stream()
        .filter(interacao ->
                acompanhamentoId.equals(
                        interacao.getAcompanhamento().getId().longValue()
                ))
        .toList();
    }

    @Transactional
    public Interacao atualizar(
            Integer interacaoId,
            Long usuarioId,
            String conteudo,
            LocalDateTime prazo) {

        Interacao interacao = buscarInteracao(interacaoId);
        Usuario usuario = buscarUsuarioAtivo(usuarioId);

        if (!interacao.getAutor().equals(usuario)) {
            throw new IllegalArgumentException(
                    "Somente o autor pode atualizar a interação."
            );
        }

        interacao.atualizar(conteudo, prazo);

        return interacaoRepository.save(interacao);
    }

    @Transactional
    public Interacao atualizarSituacaoMeta(
            Integer interacaoId,
            Long usuarioId,
            String situacao) {

        Interacao interacao = buscarInteracao(interacaoId);
        Usuario usuario = buscarUsuarioAtivo(usuarioId);

        if (!interacao.getAutor().equals(usuario)) {
            throw new IllegalArgumentException(
                    "Somente o autor pode atualizar a situação da meta."
            );
        }

        interacao.atualizarSituacaoMeta(situacao);

        return interacaoRepository.save(interacao);
    }

    @Transactional
    public Interacao marcarComoLida(
            Integer interacaoId,
            Long destinatarioId) {

        Interacao interacao = buscarInteracao(interacaoId);
        Usuario destinatario = buscarUsuarioAtivo(destinatarioId);

        interacao.marcarComoLida(destinatario);

        return interacaoRepository.save(interacao);
    }

    private Acompanhamento buscarAcompanhamento(Long acompanhamentoId) {
        if (acompanhamentoId == null) {
            throw new IllegalArgumentException(
                    "O identificador do acompanhamento é obrigatório."
            );
        }

        return acompanhamentoRepository.findById(acompanhamentoId)
                .orElseThrow(() -> new IllegalArgumentException(
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
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado."
                ));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new IllegalStateException(
                    "O usuário está inativo."
            );
        }

        return usuario;
    }

    private Interacao buscarInteracao(Integer interacaoId) {
        if (interacaoId == null) {
            throw new IllegalArgumentException(
                    "O identificador da interação é obrigatório."
            );
        }

        return interacaoRepository.findById(interacaoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Interação não encontrada."
                ));
    }

    private void validarParticipante(
            Acompanhamento acompanhamento,
            Usuario usuario) {

        boolean participa =
                usuario.equals(acompanhamento.getUsuario())
                || usuario.equals(acompanhamento.getProfissional());

        if (!participa) {
            throw new IllegalArgumentException(
                    "O usuário não participa deste acompanhamento."
            );
        }
    }
}
