
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@DiscriminatorValue("PROFISSIONAL_EDUCACAO_FISICA")
public class ProfissionalEducacaoFisica extends Profissional {

    private String especialidade = "EDUCACAO_FISICA";

    public ProfissionalEducacaoFisica() {
    }

    @Override
    public boolean podeGerenciarPlano(String tipoPlano) {
        return "TREINO".equalsIgnoreCase(tipoPlano);
    }

    public Plano criarPlanoTreino(
        Acompanhamento acompanhamento,
        String titulo,
        List<ItemPlano> itens) {

        if (!Boolean.TRUE.equals(getAtivo()) || !estaVerificado()) {
            throw new IllegalStateException(
                "O profissional precisa estar ativo e verificado."
            );
        }

        if (acompanhamento == null
            || titulo == null
            || titulo.isBlank()
            || itens == null
            || itens.isEmpty()) {
            throw new IllegalArgumentException(
                "O acompanhamento, o título e os itens são obrigatórios."
            );
        }

        if (!acompanhamento.permiteAcesso(
            this, "TREINO", "CRIAR")) {
            throw new IllegalStateException(
                "O profissional não tem permissão para criar este plano."
            );
        }

        Plano plano = new Plano();

        plano.setUsuario(acompanhamento.getUsuario());
        plano.setTipo("TREINO");
        plano.setTitulo(titulo.trim());
        plano.setObjetivo(acompanhamento.getUsuario().getObjetivo());
        plano.setCriadoPor(this);
        plano.setCriadoEm(LocalDateTime.now());

        for (ItemPlano item : itens) {
            if (item == null) {
                throw new IllegalArgumentException(
                    "A lista não pode conter itens nulos."
                );
            }

            plano.incluirItem(item);
        }

        return plano;
    }

    public void alterarPlanoTreino(
        Acompanhamento acompanhamento,
        Plano plano,
        List<ItemPlano> alteracoes) {

        if (!Boolean.TRUE.equals(getAtivo()) || !estaVerificado()) {
            throw new IllegalStateException(
                "O profissional precisa estar ativo e verificado."
            );
        }

        if (acompanhamento == null
            || plano == null
            || alteracoes == null
            || alteracoes.isEmpty()) {
            throw new IllegalArgumentException(
                "O acompanhamento, o plano e as alterações são obrigatórios."
            );
        }

        if (!podeGerenciarPlano(plano.getTipo())) {
            throw new IllegalArgumentException(
                "O profissional só pode alterar planos de treino."
            );
        }

        if (!plano.podeSerAlteradoPor(this, acompanhamento)) {
            throw new IllegalStateException(
                "O profissional não tem permissão para alterar este plano."
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

            plano.alterarItem(itemAtual, novosDados);
        }
    }

    public String getEspecialidade() {
        return especialidade;
    }
}
