
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
@DiscriminatorValue("PROFISSIONAL")
public abstract class Profissional extends Usuario {

    private String descricaoProfissional;

    private String registroProfissional;

    @ElementCollection
    @CollectionTable(
        name = "PROFISSIONAL_COMPROVANTE",
        joinColumns = @JoinColumn(name = "profissional_id")
    )
    @Column(name = "comprovante")
    private List<String> comprovantes = new ArrayList<>();

    @Column(nullable = false, length = 20)
    private String statusVerificacao = "PENDENTE";

    @ManyToOne
    @JoinColumn(name = "verificado_por_id")
    private Administrador verificadoPor;

    private LocalDateTime verificadoEm;

    @Column(length = 500)
    private String motivoDecisao;

    public Profissional() {
    }

    public void solicitarVerificacao(
        String registro,
        List<String> comprovantes) {

        if (!Boolean.TRUE.equals(getAtivo())) {
            throw new IllegalStateException(
                "A conta precisa estar ativa."
            );
        }

        if (registro == null || registro.isBlank()) {
            throw new IllegalArgumentException(
                "O registro profissional é obrigatório."
            );
        }

        if (comprovantes == null || comprovantes.isEmpty()) {
            throw new IllegalArgumentException(
                "Informe ao menos um comprovante."
            );
        }

        List<String> comprovantesValidados = new ArrayList<>();

        for (String comprovante : comprovantes) {
            if (comprovante == null || comprovante.isBlank()) {
                throw new IllegalArgumentException(
                    "Os comprovantes não podem estar vazios."
                );
            }

            comprovantesValidados.add(comprovante.trim());
        }

        this.registroProfissional = registro.trim();
        this.comprovantes = comprovantesValidados;
        this.statusVerificacao = "PENDENTE";
        this.verificadoPor = null;
        this.verificadoEm = null;
        this.motivoDecisao = null;
    }

    public void registrarDecisao(
        Administrador administrador,
        String decisao,
        String motivo) {

                String decisaoNormalizada =
            decisao.trim().toUpperCase(Locale.ROOT);

        if (!"APROVADA".equals(decisaoNormalizada)
            && !"REJEITADA".equals(decisaoNormalizada)) {

            throw new IllegalArgumentException(
                "A decisão deve ser APROVADA ou REJEITADA."
            );
        }

        if ("REJEITADA".equals(decisaoNormalizada)
            && (motivo == null || motivo.isBlank())) {

            throw new IllegalArgumentException(
                "Informe o motivo da rejeição."
            );
        }

        String motivoNormalizado =
            motivo == null || motivo.isBlank()
                ? null
                : motivo.trim();

        if (motivoNormalizado != null
            && motivoNormalizado.length() > 500) {

            throw new IllegalArgumentException(
                "O motivo da decisão não pode ultrapassar 500 caracteres."
            );
        }

        this.statusVerificacao = decisaoNormalizada;
        this.verificadoPor = administrador;
        this.verificadoEm = LocalDateTime.now();
        this.motivoDecisao = motivoNormalizado;
    }

    public boolean estaVerificado() {
        return Boolean.TRUE.equals(getAtivo())
            && "APROVADA".equals(this.statusVerificacao);
    }

    public Publicacao publicarConteudo(String titulo, String texto) {
        if (!estaVerificado()) {
            throw new IllegalStateException(
                "Somente profissionais verificados podem publicar."
            );
        }

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException(
                "O título da publicação é obrigatório."
            );
        }

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                "O conteúdo da publicação é obrigatório."
            );
        }

        return new Publicacao(this, titulo.trim(), texto.trim());
    }

    public String getDescricaoProfissional() {
        return descricaoProfissional;
    }

    public void setDescricaoProfissional(String descricaoProfissional) {
        this.descricaoProfissional = descricaoProfissional;
    }

    public String getRegistroProfissional() {
        return registroProfissional;
    }

    public LocalDateTime getVerificadoEm() {
        return verificadoEm;
    }

    public String getStatusVerificacao() {
        return statusVerificacao;
    }

    public Administrador getVerificadoPor() {
        return verificadoPor;
    }

    public String getMotivoDecisao() {
        return motivoDecisao;
    }

    public List<String> getComprovantes() {
        return List.copyOf(comprovantes);
    }

    public abstract boolean podeGerenciarPlano(String tipoPlano);
}
