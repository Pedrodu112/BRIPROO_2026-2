
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Column;

@Entity
@DiscriminatorValue("ADMINISTRADOR")
public class Administrador extends Usuario {

    @Column(length = 100)
    private String identificacaoAdministrativa;

    public Administrador() {
    }

    public void analisarVerificacao(
        Profissional profissional,
        String decisao,
        String motivo) {

        if (!Boolean.TRUE.equals(getAtivo())) {
            throw new IllegalStateException(
                "O administrador precisa estar ativo."
            );
        }

        if (profissional == null) {
            throw new IllegalArgumentException(
                "O profissional é obrigatório."
            );
        }

        profissional.registrarDecisao(this, decisao, motivo);
    }

    public void inativarConta(Usuario conta, String motivo) {
        if (!Boolean.TRUE.equals(getAtivo())) {
            throw new IllegalStateException(
                "O administrador precisa estar ativo."
            );
        }

        if (conta == null) {
            throw new IllegalArgumentException(
                "A conta é obrigatória."
            );
        }

        if (conta == this) {
            throw new IllegalArgumentException(
                "O administrador não pode inativar a própria conta."
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                "O motivo da inativação é obrigatório."
            );
        }

        conta.inativar(this, motivo.trim());
    }

    public void moderarPublicacao(
        Publicacao publicacao,
        String motivo) {

        if (!Boolean.TRUE.equals(getAtivo())) {
            throw new IllegalStateException(
                "O administrador precisa estar ativo."
            );
        }

        if (publicacao == null) {
            throw new IllegalArgumentException(
                "A publicação é obrigatória."
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                "O motivo da moderação é obrigatório."
            );
        }

        if (!publicacao.podeSerExibida()) {
            throw new IllegalStateException(
                "A publicação já está indisponível ou seu autor não está verificado."
            );
        }

        publicacao.inativar(this, motivo.trim());
    }

    public String getIdentificacaoAdministrativa() {
        return identificacaoAdministrativa;
    }

    public void setIdentificacaoAdministrativa(
        String identificacaoAdministrativa) {

        if (identificacaoAdministrativa != null
            && identificacaoAdministrativa.length() > 100) {
            throw new IllegalArgumentException(
                "A identificação administrativa não pode ultrapassar 100 caracteres."
            );
        }

        this.identificacaoAdministrativa =
            identificacaoAdministrativa == null
                ? null
                : identificacaoAdministrativa.trim();
    }
}
