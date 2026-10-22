
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "USUARIO")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", length = 20)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String senhaHash;

    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "autor_inativacao_id")
    private Usuario autorInativacao;

    private LocalDateTime dataInativacao;

    @Column(length = 500)
    private String motivoInativacao;

    public Usuario() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataInativacao() {
        return dataInativacao;
    }

    public Usuario getAutorInativacao() {
        return autorInativacao;
    }

    public String getMotivoInativacao() {
        return motivoInativacao;
    }

    public void alterarSenha(String novaSenha) {
        if (novaSenha == null || novaSenha.isBlank()) {
            throw new IllegalArgumentException(
                "A nova senha é obrigatória."
            );
        }

        if (novaSenha.length() < 8) {
            throw new IllegalArgumentException(
                "A senha deve ter pelo menos 8 caracteres."
            );
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        this.senhaHash = passwordEncoder.encode(novaSenha);
    }

    public boolean verificarSenha(String senha) {
        if (senha == null || this.senhaHash == null) {
            return false;
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(senha, this.senhaHash);
    }

    public void atualizarDados(String nome, String email) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(
                "O nome é obrigatório."
            );
        }

        if (email == null || email.isBlank()
            || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                "Informe um e-mail válido."
            );
        }

        this.nome = nome.trim();
        this.email = email.trim();
    }

    public void inativar(Usuario autor, String motivo) {
        if (autor == null) {
            throw new IllegalArgumentException(
                "O autor da inativação é obrigatório."
            );
        }

        if (!Boolean.TRUE.equals(autor.getAtivo())) {
            throw new IllegalStateException(
                "O autor da inativação precisa estar ativo."
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                "O motivo da inativação é obrigatório."
            );
        }

        if (!Boolean.TRUE.equals(this.ativo)) {
            throw new IllegalStateException(
                "Esta conta já está inativa."
            );
        }

        this.ativo = false;
        this.autorInativacao = autor;
        this.dataInativacao = LocalDateTime.now();
        this.motivoInativacao = motivo.trim();
    }
}
