
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Entity
public class Publicacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional autor;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 5000)
    private String conteudo;

    private Boolean ativa = true;

    private LocalDateTime publicadaEm;
    private LocalDateTime inativadaEm;

    @ManyToOne
    @JoinColumn(name = "responsavel_inativacao_id")
    private Usuario responsavelInativacao;

    @Column(length = 500)
    private String motivoInativacao;

    public Publicacao() {
    }

    public Publicacao(
        Profissional autor,
        String titulo,
        String conteudo) {

        if (autor == null || !autor.estaVerificado()) {
            throw new IllegalArgumentException(
                "A publicação precisa de um profissional verificado."
            );
        }

        validarConteudo(titulo, conteudo);

        this.autor = autor;
        this.titulo = titulo.trim();
        this.conteudo = conteudo.trim();
        this.ativa = true;
        this.publicadaEm = LocalDateTime.now();
    }

    public void editar(String titulo, String conteudo) {
        if (!Boolean.TRUE.equals(this.ativa)) {
            throw new IllegalStateException(
                "Não é possível editar uma publicação inativa."
            );
        }

        validarConteudo(titulo, conteudo);

        this.titulo = titulo.trim();
        this.conteudo = conteudo.trim();
    }

    public void inativar(Usuario responsavel, String motivo) {
        if (responsavel == null
            || !Boolean.TRUE.equals(responsavel.getAtivo())) {
            throw new IllegalArgumentException(
                "O responsável pela inativação precisa estar ativo."
            );
        }

        if (!Boolean.TRUE.equals(this.ativa)) {
            throw new IllegalStateException(
                "A publicação já está inativa."
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                "O motivo da inativação é obrigatório."
            );
        }

        this.ativa = false;
        this.inativadaEm = LocalDateTime.now();
        this.responsavelInativacao = responsavel;
        this.motivoInativacao = motivo.trim();
    }

    public boolean podeSerExibida() {
        return Boolean.TRUE.equals(this.ativa)
            && this.autor != null
            && Boolean.TRUE.equals(this.autor.getAtivo())
            && this.autor.estaVerificado();
    }

    private void validarConteudo(String titulo, String conteudo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException(
                "O título da publicação é obrigatório."
            );
        }

        if (conteudo == null || conteudo.isBlank()) {
            throw new IllegalArgumentException(
                "O conteúdo da publicação é obrigatório."
            );
        }

        if (titulo.trim().length() > 255) {
            throw new IllegalArgumentException(
                "O título não pode ultrapassar 255 caracteres."
            );
        }

        if (conteudo.trim().length() > 5000) {
            throw new IllegalArgumentException(
                "O conteúdo não pode ultrapassar 5000 caracteres."
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Profissional getAutor() {
    return autor;
}

    public LocalDateTime getPublicadaEm() {
        return publicadaEm;
    }

    public LocalDateTime getInativadaEm() {
        return inativadaEm;
    }

    
    public String getTitulo() {
        return titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public String getMotivoInativacao() {
        return motivoInativacao;
    }

}
