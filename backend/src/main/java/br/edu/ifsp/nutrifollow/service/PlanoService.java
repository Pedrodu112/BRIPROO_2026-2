
package br.edu.ifsp.nutrifollow.service;

import java.time.LocalDateTime;
import br.edu.ifsp.nutrifollow.entity.*;
import br.edu.ifsp.nutrifollow.repository.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanoService {

    private final PlanoRepository planoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AcompanhamentoRepository acompanhamentoRepository;

    public PlanoService(
            PlanoRepository planoRepository,
            UsuarioRepository usuarioRepository,
            AcompanhamentoRepository acompanhamentoRepository) {

        this.planoRepository = planoRepository;
        this.usuarioRepository = usuarioRepository;
        this.acompanhamentoRepository = acompanhamentoRepository;
    }

    @Transactional
    public Plano criarTreino(
            Long profissionalId,
            Integer acompanhamentoId,
            String titulo,
            List<ItemPlano> itens) {

        ProfissionalEducacaoFisica profissional =
                buscarProfissionalEducacaoFisica(profissionalId);

        Acompanhamento acompanhamento =
                buscarAcompanhamento(acompanhamentoId);

        Plano plano = profissional.criarPlanoTreino(
                acompanhamento, titulo, itens);

        return planoRepository.save(plano);
    }

    @Transactional
    public Plano criarAlimentar(
            Long profissionalId,
            Integer acompanhamentoId,
            List<ItemPlano> orientacoes) {

        Nutricionista profissional =
                buscarNutricionista(profissionalId);

        Acompanhamento acompanhamento =
                buscarAcompanhamento(acompanhamentoId);

        Plano plano = profissional.criarPlanoAlimentar(
                acompanhamento, orientacoes);

        return planoRepository.save(plano);
    }

    @Transactional
    public Plano criarTreinoProprio(
            Long usuarioId,
            String titulo,
            List<ItemPlano> itens) {

        Usuario usuario = buscarUsuarioAtivo(usuarioId);

        if (!(usuario instanceof UsuarioComum usuarioComum)) {
            throw new IllegalArgumentException(
                "Somente usuários comuns podem criar planos próprios de treino."
            );
        }

        if (titulo == null
            || titulo.isBlank()
            || titulo.trim().length() > 255) {
            throw new IllegalArgumentException(
                "O título é obrigatório e não pode ultrapassar 255 caracteres."
            );
        }

        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException(
                "O plano precisa possuir ao menos um item."
            );
        }

        Plano plano = new Plano();

        plano.setUsuario(usuarioComum);
        plano.setTipo("TREINO");
        plano.setTitulo(titulo.trim());
        plano.setObjetivo(usuarioComum.getObjetivo());
        plano.setCriadoPor(usuarioComum);
        plano.setCriadoEm(LocalDateTime.now());

        for (ItemPlano item : itens) {
            if (item == null) {
                throw new IllegalArgumentException(
                    "A lista não pode conter itens nulos."
                );
            }

            plano.incluirItem(item);
        }

        return planoRepository.save(plano);
    }


    @Transactional
    public Plano alterarProprio(
            Long usuarioId,
            Integer planoId,
            List<ItemPlano> alteracoes) {

        Usuario usuario = buscarUsuarioAtivo(usuarioId);

        if (!(usuario instanceof UsuarioComum usuarioComum)) {
            throw new IllegalArgumentException(
                "Somente usuários comuns podem alterar seus próprios planos."
            );
        }

        Plano plano = buscarPlano(planoId);

        if (!plano.podeSerAlteradoPor(usuarioComum, null)) {
            throw new IllegalStateException(
                "O usuário não pode alterar este plano."
            );
        }

        if (alteracoes == null || alteracoes.isEmpty()) {
            throw new IllegalArgumentException(
                "Informe os itens atualizados do plano."
            );
        }

        List<ItemPlano> itensAtivos = plano.getItens().stream()
            .filter(item -> Boolean.TRUE.equals(item.getAtivo()))
            .toList();

        if (itensAtivos.size() != alteracoes.size()) {
            throw new IllegalArgumentException(
                "A quantidade de alterações deve corresponder aos itens ativos do plano."
            );
        }

        for (ItemPlano novosDados : alteracoes) {
            if (novosDados == null) {
                throw new IllegalArgumentException(
                    "A lista não pode conter itens nulos."
                );
            }

            novosDados.setPlano(plano);

            if (!novosDados.validarConfiguracao()) {
                throw new IllegalArgumentException(
                    "Um dos itens de treino está inválido."
                );
            }
        }

        for (int i = 0; i < alteracoes.size(); i++) {
            ItemPlano itemAtual = itensAtivos.get(i);
            ItemPlano novosDados = alteracoes.get(i);

            plano.alterarItem(
                itemAtual,
                novosDados,
                usuarioComum,
                null
            );
        }

        return planoRepository.save(plano);
    }

    @Transactional(readOnly = true)
    public List<Plano> listarPorUsuario(Long usuarioId) {

        buscarUsuarioAtivo(usuarioId);

        return planoRepository.findByUsuario_IdAndAtivoTrue(usuarioId);
    }

    @Transactional
    public Plano alterar(
            Long profissionalId,
            Integer acompanhamentoId,
            Integer planoId,
            List<ItemPlano> alteracoes) {

        Profissional profissional =
                buscarProfissional(profissionalId);

        Acompanhamento acompanhamento =
                buscarAcompanhamento(acompanhamentoId);

        Plano plano = buscarPlano(planoId);

        if (profissional instanceof Nutricionista nutricionista) {
            nutricionista.alterarPlanoAlimentar(
                    acompanhamento, plano, alteracoes);

        } else if (profissional instanceof ProfissionalEducacaoFisica educador) {
            educador.alterarPlanoTreino(
                    acompanhamento, plano, alteracoes);

        } else {
            throw new IllegalArgumentException(
                    "Tipo de profissional não reconhecido.");
        }

        return planoRepository.save(plano);
    }

    @Transactional
    public Plano inativar(Integer planoId, Long autorId) {

        Plano plano = buscarPlano(planoId);
        Usuario autor = buscarUsuarioAtivo(autorId);

        plano.inativar(autor);

        return planoRepository.save(plano);
    }

    private Plano buscarPlano(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "O identificador do plano é obrigatório.");
        }

        return planoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Plano não encontrado."));
    }

    private Acompanhamento buscarAcompanhamento(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "O identificador do acompanhamento é obrigatório.");
        }

        return acompanhamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Acompanhamento não encontrado."));
    }

    private Usuario buscarUsuarioAtivo(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "O identificador do usuário é obrigatório.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado."));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new IllegalStateException("O usuário está inativo.");
        }

        return usuario;
    }

    private Profissional buscarProfissional(Long id) {
        Usuario usuario = buscarUsuarioAtivo(id);

        if (!(usuario instanceof Profissional profissional)) {
            throw new IllegalArgumentException(
                    "O usuário informado não é um profissional.");
        }

        return profissional;
    }

    private ProfissionalEducacaoFisica buscarProfissionalEducacaoFisica(
            Long id) {

        Profissional profissional = buscarProfissional(id);

        if (!(profissional instanceof ProfissionalEducacaoFisica educador)) {
            throw new IllegalArgumentException(
                    "O profissional não é da área de educação física.");
        }

        return educador;
    }

    private Nutricionista buscarNutricionista(Long id) {
        Profissional profissional = buscarProfissional(id);

        if (!(profissional instanceof Nutricionista nutricionista)) {
            throw new IllegalArgumentException(
                    "O profissional não é nutricionista.");
        }

        return nutricionista;
    }
}
