
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@DiscriminatorValue("NUTRICIONISTA")
public class Nutricionista extends Profissional {

    private String especialidade = "NUTRICAO";

    public Nutricionista() {
    }

    @Override
    public boolean podeGerenciarPlano(String tipoPlano) {
        return "ALIMENTAR".equalsIgnoreCase(tipoPlano);
    }

    public Plano criarPlanoAlimentar(
        Acompanhamento acompanhamento,
        List<ItemPlano> orientacoes) {

        if (!Boolean.TRUE.equals(getAtivo()) || !estaVerificado()) {
            throw new IllegalStateException(
                "O nutricionista precisa estar ativo e verificado."
            );
        }

        if (acompanhamento == null
            || orientacoes == null
            || orientacoes.isEmpty()) {
            throw new IllegalArgumentException(
                "O acompanhamento e as orientações são obrigatórios."
            );
        }

        if (!acompanhamento.permiteAcesso(
            this, "ALIMENTAR", "CRIAR")) {
            throw new IllegalStateException(
                "O nutricionista não tem permissão para criar este plano."
            );
        }

        Plano plano = new Plano();

        plano.setUsuario(acompanhamento.getUsuario());
        plano.setTipo("ALIMENTAR");
        plano.setTitulo("Plano alimentar");
        plano.setObjetivo(acompanhamento.getUsuario().getObjetivo());
        plano.setCriadoPor(this);
        plano.setCriadoEm(LocalDateTime.now());

        for (ItemPlano item : orientacoes) {
            if (item == null) {
                throw new IllegalArgumentException(
                    "A lista não pode conter itens nulos."
                );
            }

            plano.incluirItem(item);
        }

        return plano;
    }

    public void alterarPlanoAlimentar(
        Acompanhamento acompanhamento,
        Plano plano,
        List<ItemPlano> orientacoes) {

        if (!Boolean.TRUE.equals(getAtivo()) || !estaVerificado()) {
            throw new IllegalStateException(
                "O nutricionista precisa estar ativo e verificado."
            );
        }

        if (acompanhamento == null
            || plano == null
            || orientacoes == null
            || orientacoes.isEmpty()) {
            throw new IllegalArgumentException(
                "O acompanhamento, o plano e as orientações são obrigatórios."
            );
        }

        if (!podeGerenciarPlano(plano.getTipo())) {
            throw new IllegalArgumentException(
                "O nutricionista só pode alterar planos alimentares."
            );
        }

        if (!plano.podeSerAlteradoPor(this, acompanhamento)) {
            throw new IllegalStateException(
                "O nutricionista não tem permissão para alterar este plano."
            );
        }

        List<ItemPlano> itensAtivos = plano.getItens().stream()
            .filter(item -> Boolean.TRUE.equals(item.getAtivo()))
            .toList();

        if (itensAtivos.size() != orientacoes.size()) {
            throw new IllegalArgumentException(
                "A quantidade de orientações deve corresponder aos itens ativos do plano."
            );
        }

        for (ItemPlano orientacao : orientacoes) {
            if (orientacao == null) {
                throw new IllegalArgumentException(
                    "A lista não pode conter itens nulos."
                );
            }

            orientacao.setPlano(plano);

            if (!orientacao.validarConfiguracao()) {
                throw new IllegalArgumentException(
                    "Uma das orientações alimentares é inválida."
                );
            }
        }

        for (int i = 0; i < orientacoes.size(); i++) {
            ItemPlano itemAtual = itensAtivos.get(i);
            ItemPlano novosDados = orientacoes.get(i);

            plano.alterarItem(itemAtual, novosDados);
        }
    }

    public String getEspecialidade() {
        return especialidade;
    }
}
