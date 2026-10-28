
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
public class Acompanhamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioComum usuario;

    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Column(nullable = false)
    private String finalidade;

    @Column(nullable = false)
    private String status = "PENDENTE";

    private LocalDateTime solicitadoEm;
    private LocalDateTime respondidoEm;

    @Column(length = 500)
    private String motivoRecusa;

    @ElementCollection
    @CollectionTable(
        name = "ACOMPANHAMENTO_PERMISSAO",
        joinColumns = @JoinColumn(name = "acompanhamento_id")
    )
    @Column(name = "permissao", nullable = false)
    private List<String> permissoesConcedidas = new ArrayList<>();

    private LocalDateTime autorizadoEm;
    private LocalDateTime iniciadoEm;
    private LocalDateTime encerradoEm;

    @ManyToOne
    @JoinColumn(name = "encerrado_por_id")
    private Usuario encerradoPor;

    @Column(length = 500)
    private String motivoEncerramento;

    public Acompanhamento() {
    }

    public Acompanhamento(
        UsuarioComum usuario,
        Profissional profissional,
        String finalidade) {

        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new IllegalArgumentException(
                "Informe um usuário ativo."
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

        this.usuario = usuario;
        this.profissional = profissional;
        this.finalidade = finalidade.trim();
        this.status = "PENDENTE";
        this.solicitadoEm = LocalDateTime.now();
    }

    public void aceitar(Profissional profissional) {
        validarProfissional(profissional);

        if (!"PENDENTE".equals(this.status)) {
            throw new IllegalStateException(
                "A solicitação não está pendente."
            );
        }

        this.status = "ACEITO";
        this.respondidoEm = LocalDateTime.now();
    }

    public void recusar(Profissional profissional, String motivo) {
        validarProfissional(profissional);

        if (!"PENDENTE".equals(this.status)) {
            throw new IllegalStateException(
                "A solicitação não está pendente."
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                "O motivo da recusa é obrigatório."
            );
        }

        this.status = "RECUSADO";
        this.motivoRecusa = motivo.trim();
        this.respondidoEm = LocalDateTime.now();
    }

    public void autorizarCompartilhamento(
        UsuarioComum usuario,
        List<String> permissoes) {

        if (usuario == null || !this.usuario.equals(usuario)) {
            throw new IllegalArgumentException(
                "Este usuário não pertence ao acompanhamento."
            );
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new IllegalStateException(
                "O usuário precisa estar ativo."
            );
        }

        if (!Boolean.TRUE.equals(this.profissional.getAtivo())
            || !this.profissional.estaVerificado()) {
            throw new IllegalStateException(
                "O profissional precisa estar ativo e verificado."
            );
        }

        if (!"ACEITO".equals(this.status)) {
            throw new IllegalStateException(
                "O acompanhamento precisa estar aceito."
            );
        }

        if (permissoes == null || permissoes.isEmpty()) {
            throw new IllegalArgumentException(
                "Informe ao menos uma permissão."
            );
        }

        List<String> permissoesValidadas = new ArrayList<>();

        for (String permissao : permissoes) {
            if (permissao == null || permissao.isBlank()) {
                throw new IllegalArgumentException(
                    "As permissões não podem estar vazias."
                );
            }

            String[] partes = permissao.trim().split(":", -1);

            if (partes.length != 2
                || partes[0].isBlank()
                || partes[1].isBlank()) {
                throw new IllegalArgumentException(
                    "Use o formato CATEGORIA:OPERACAO."
                );
            }

            String permissaoNormalizada =
                partes[0].trim().toUpperCase(Locale.ROOT)
                + ":"
                + partes[1].trim().toUpperCase(Locale.ROOT);

            if (!permissoesValidadas.contains(permissaoNormalizada)) {
                permissoesValidadas.add(permissaoNormalizada);
            }
        }

        this.permissoesConcedidas = permissoesValidadas;
        this.autorizadoEm = LocalDateTime.now();
        this.iniciadoEm = this.autorizadoEm;
        this.status = "ATIVO";
    }

    public boolean permiteAcesso(
        Profissional profissional,
        String categoria,
        String operacao) {

        if (profissional == null
            || categoria == null || categoria.isBlank()
            || operacao == null || operacao.isBlank()) {
            return false;
        }

        if (!Boolean.TRUE.equals(profissional.getAtivo())
            || !profissional.estaVerificado()
            || !"ATIVO".equals(this.status)) {
            return false;
        }

        if (!this.profissional.equals(profissional)) {
            return false;
        }

        String permissaoSolicitada =
            categoria.trim().toUpperCase(Locale.ROOT)
            + ":"
            + operacao.trim().toUpperCase(Locale.ROOT);

        return this.permissoesConcedidas.stream()
            .anyMatch(permissao ->
                permissao.equalsIgnoreCase(permissaoSolicitada)
            );
    }

    public void encerrar(Usuario autor) {
        encerrar(autor, "Encerramento sem motivo informado.");
    }

    public void encerrar(Usuario autor, String motivo) {
        if (autor == null) {
            throw new IllegalArgumentException(
                "O autor do encerramento é obrigatório."
            );
        }

        if (!Boolean.TRUE.equals(autor.getAtivo())) {
            throw new IllegalStateException(
                "O autor precisa estar ativo."
            );
        }

        boolean usuarioDoAcompanhamento =
            this.usuario.equals(autor);

        boolean profissionalDoAcompanhamento =
            this.profissional.equals(autor);

        boolean administrador = autor instanceof Administrador;

        if (!usuarioDoAcompanhamento
            && !profissionalDoAcompanhamento
            && !administrador) {
            throw new IllegalArgumentException(
                "O autor não pode encerrar este acompanhamento."
            );
        }

        if ("ENCERRADO".equals(this.status)) {
            throw new IllegalStateException(
                "O acompanhamento já está encerrado."
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                "O motivo do encerramento é obrigatório."
            );
        }

        this.status = "ENCERRADO";
        this.encerradoPor = autor;
        this.encerradoEm = LocalDateTime.now();
        this.motivoEncerramento = motivo.trim();
    }

    private void validarProfissional(Profissional profissional) {
        if (profissional == null
            || !this.profissional.equals(profissional)) {
            throw new IllegalArgumentException(
                "Este profissional não pertence à solicitação."
            );
        }

        if (!Boolean.TRUE.equals(profissional.getAtivo())
            || !profissional.estaVerificado()) {
            throw new IllegalStateException(
                "O profissional precisa estar ativo e verificado."
            );
        }
    }

    public UsuarioComum getUsuario() {
        return usuario;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getSolicitadoEm() {
        return solicitadoEm;
    }

    public LocalDateTime getRespondidoEm() {
        return respondidoEm;
    }

    public String getMotivoRecusa() {
        return motivoRecusa;
    }

    public LocalDateTime getIniciadoEm() {
        return iniciadoEm;
    }

    public LocalDateTime getEncerradoEm() {
        return encerradoEm;
    }

    public String getMotivoEncerramento() {
        return motivoEncerramento;
    }

    public Integer getId() {
    return id;
    }

    public String getFinalidade() {
    return finalidade;
    }

    public LocalDateTime getAutorizadoEm() {
        return autorizadoEm;
    }

    public Usuario getEncerradoPor() {
        return encerradoPor;
    }
}
