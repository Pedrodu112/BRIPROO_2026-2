
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@Entity
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioComum usuario;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    private LocalDateTime registradoEm;

    @Column(nullable = false, length = 2000)
    private String descricao;

    private String tipoRefeicao;
    private Integer series;
    private Integer repeticoes;
    private BigDecimal cargaKg;
    private BigDecimal quantidadeMl;
    private BigDecimal pesoKg;

    @ManyToOne
    @JoinColumn(name = "plano_origem_id")
    private Plano planoOrigem;

    private Integer versaoPlanoOrigem;

    @Column(length = 2000)
    private String observacoes;

    @Column(nullable = false)
    private Boolean ativo = true;

    public Registro() {
    }

    public boolean validarDados() {
        if (tipo == null || tipo.isBlank()
            || descricao == null || descricao.isBlank()
            || descricao.trim().length() > 2000) {
            return false;
        }

        String tipoNormalizado = tipo.trim().toUpperCase(Locale.ROOT);

        if ("TREINO".equals(tipoNormalizado)) {
            return series != null && series > 0
                && repeticoes != null && repeticoes > 0
                && cargaKg != null
                && cargaKg.compareTo(BigDecimal.ZERO) >= 0;
        }

        if ("REFEICAO".equals(tipoNormalizado)) {
            return tipoRefeicao != null
                && !tipoRefeicao.isBlank();
        }

        if ("AGUA".equals(tipoNormalizado)) {
            return quantidadeMl != null
                && quantidadeMl.compareTo(BigDecimal.ZERO) > 0;
        }

        if ("PESO".equals(tipoNormalizado)) {
            return pesoKg != null
                && pesoKg.compareTo(BigDecimal.ZERO) > 0;
        }

        return false;
    }

    public void corrigirRegistro(Registro novosDados) {
        if (!Boolean.TRUE.equals(this.ativo)) {
            throw new IllegalStateException(
                "Não é possível corrigir um registro inativo."
            );
        }

        if (novosDados == null) {
            throw new IllegalArgumentException(
                "Os novos dados são obrigatórios."
            );
        }

        if (!novosDados.validarDados()) {
            throw new IllegalArgumentException(
                "Os dados corrigidos são inválidos."
            );
        }

        if (this.tipo == null
            || !this.tipo.trim().equalsIgnoreCase(
                novosDados.tipo.trim()
            )) {
            throw new IllegalArgumentException(
                "O tipo do registro não pode ser alterado."
            );
        }

        if (novosDados.observacoes != null
            && novosDados.observacoes.trim().length() > 2000) {
            throw new IllegalArgumentException(
                "As observações não podem ultrapassar 2000 caracteres."
            );
        }

        this.descricao = novosDados.descricao.trim();
        this.tipoRefeicao = novosDados.tipoRefeicao;
        this.series = novosDados.series;
        this.repeticoes = novosDados.repeticoes;
        this.cargaKg = novosDados.cargaKg;
        this.quantidadeMl = novosDados.quantidadeMl;
        this.pesoKg = novosDados.pesoKg;
        this.planoOrigem = novosDados.planoOrigem;
        this.versaoPlanoOrigem = novosDados.versaoPlanoOrigem;
        this.observacoes = novosDados.observacoes == null
            ? null
            : novosDados.observacoes.trim();
    }

    public void inativar() {
        if (!Boolean.TRUE.equals(this.ativo)) {
            throw new IllegalStateException(
                "O registro já está inativo."
            );
        }

        this.ativo = false;
    }

    public Integer getId() {
        return id;
    }

    public UsuarioComum getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioComum usuario) {
        this.usuario = usuario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        if (tipo != null && tipo.trim().length() > 20) {
            throw new IllegalArgumentException(
                "O tipo não pode ultrapassar 20 caracteres."
            );
        }

        this.tipo = tipo == null ? null : tipo.trim();
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }

    public void setRegistradoEm(LocalDateTime registradoEm) {
        this.registradoEm = registradoEm;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        if (descricao != null && descricao.trim().length() > 2000) {
            throw new IllegalArgumentException(
                "A descrição não pode ultrapassar 2000 caracteres."
            );
        }

        this.descricao = descricao == null ? null : descricao.trim();
    }

    public String getTipoRefeicao() {
        return tipoRefeicao;
    }

    public void setTipoRefeicao(String tipoRefeicao) {
        this.tipoRefeicao = tipoRefeicao;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public Integer getRepeticoes() {
        return repeticoes;
    }

    public void setRepeticoes(Integer repeticoes) {
        this.repeticoes = repeticoes;
    }

    public BigDecimal getCargaKg() {
        return cargaKg;
    }

    public void setCargaKg(BigDecimal cargaKg) {
        this.cargaKg = cargaKg;
    }

    public BigDecimal getQuantidadeMl() {
        return quantidadeMl;
    }

    public void setQuantidadeMl(BigDecimal quantidadeMl) {
        this.quantidadeMl = quantidadeMl;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public Plano getPlanoOrigem() {
        return planoOrigem;
    }

    public void setPlanoOrigem(Plano planoOrigem) {
        this.planoOrigem = planoOrigem;
    }

    public Integer getVersaoPlanoOrigem() {
        return versaoPlanoOrigem;
    }

    public void setVersaoPlanoOrigem(Integer versaoPlanoOrigem) {
        this.versaoPlanoOrigem = versaoPlanoOrigem;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        if (observacoes != null && observacoes.trim().length() > 2000) {
            throw new IllegalArgumentException(
                "As observações não podem ultrapassar 2000 caracteres."
            );
        }

        this.observacoes = observacoes == null
            ? null
            : observacoes.trim();
    }

    public Boolean getAtivo() {
        return ativo;
    }
}
