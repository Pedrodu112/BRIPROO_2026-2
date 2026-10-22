
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("COMUM")
public class UsuarioComum extends Usuario {

    private LocalDate dataNascimento;

    private BigDecimal alturaCm;

    private String objetivo;

    @ManyToMany
    @JoinTable(
        name = "USUARIO_COMUM_PUBLICACAO_SALVA",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "publicacao_id")
    )
    private Set<Publicacao> publicacoesSalvas = new HashSet<>();

    public UsuarioComum() {
    }

    public void atualizarPerfil(
        LocalDate dataNascimento,
        BigDecimal alturaCm,
        String objetivo) {

        if (!Boolean.TRUE.equals(getAtivo())) {
            throw new IllegalStateException(
                "A conta precisa estar ativa."
            );
        }

        if (dataNascimento == null
            || dataNascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "A data de nascimento é inválida."
            );
        }

        if (alturaCm == null
            || alturaCm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "A altura precisa ser maior que zero."
            );
        }

        if (objetivo == null || objetivo.isBlank()) {
            throw new IllegalArgumentException(
                "O objetivo é obrigatório."
            );
        }

        this.dataNascimento = dataNascimento;
        this.alturaCm = alturaCm;
        this.objetivo = objetivo.trim();
    }

    public Acompanhamento solicitarAcompanhamento(
        Profissional profissional,
        String finalidade) {

        if (!Boolean.TRUE.equals(getAtivo())) {
            throw new IllegalStateException(
                "A conta precisa estar ativa."
            );
        }

        if (profissional == null
            || !Boolean.TRUE.equals(profissional.getAtivo())
            || !profissional.estaVerificado()) {
            throw new IllegalArgumentException(
                "Informe um profissional ativo e verificado."
            );
        }

        if (finalidade == null || finalidade.isBlank()) {
            throw new IllegalArgumentException(
                "A finalidade do acompanhamento é obrigatória."
            );
        }

        return new Acompanhamento(
            this,
            profissional,
            finalidade.trim()
        );
    }

    public void salvarPublicacao(Publicacao publicacao) {
        if (!Boolean.TRUE.equals(getAtivo())) {
            throw new IllegalStateException(
                "A conta precisa estar ativa."
            );
        }

        if (publicacao == null) {
            throw new IllegalArgumentException(
                "A publicação é obrigatória."
            );
        }

        if (!publicacao.podeSerExibida()) {
            throw new IllegalStateException(
                "Não é possível salvar uma publicação indisponível."
            );
        }

        boolean jaSalva = publicacoesSalvas.stream()
            .anyMatch(salva ->
                salva == publicacao
                || (salva.getId() != null
                    && salva.getId().equals(publicacao.getId()))
            );

        if (!jaSalva) {
            publicacoesSalvas.add(publicacao);
        }
    }

    public void removerPublicacaoSalva(Publicacao publicacao) {
        if (publicacao == null) {
            throw new IllegalArgumentException(
                "A publicação é obrigatória."
            );
        }

        publicacoesSalvas.removeIf(salva ->
            salva == publicacao
            || (salva.getId() != null
                && salva.getId().equals(publicacao.getId()))
        );
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public BigDecimal getAlturaCm() {
        return alturaCm;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public Set<Publicacao> getPublicacoesSalvas() {
        return Collections.unmodifiableSet(publicacoesSalvas);
    }
}
