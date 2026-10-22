
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
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;

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
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(length = 150, nullable = false)
    private String email;

    @NotBlank
    @Column(name = "senha_hash", length = 60, nullable = false)
    private String senhaHash;

    @Column(nullable = false)
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
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(
                "O nome é obrigatório."
            );
        }

        nome = nome.trim();

        if (nome.length() > 100) {
            throw new IllegalArgumentException(
                "O nome deve ter no máximo 100 caracteres."
            );
        }

        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                "O e-mail é obrigatório."
            );
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        if (email.length() > 150
            || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                "Informe um e-mail válido com até 150 caracteres."
            );
        }

        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException(
                "O hash da senha é obrigatório."
            );
        }

        this.senhaHash = senhaHash;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        if (ativo == null) {
            throw new IllegalArgumentException(
                "O estado da conta é obrigatório."
            );
        }

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

        int tamanhoEmBytes =
            novaSenha.getBytes(StandardCharsets.UTF_8).length;

        if (tamanhoEmBytes > 72) {
            throw new IllegalArgumentException(
                "A senha excede o limite de 72 bytes do BCrypt."
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

        nome = nome.trim();

        if (nome.length() > 100) {
            throw new IllegalArgumentException(
                "O nome deve ter no máximo 100 caracteres."
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                "O e-mail é obrigatório."
            );
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        if (email.length() > 150
            || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                "Informe um e-mail válido com até 150 caracteres."
            );
        }

        this.nome = nome;
        this.email = email;
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
