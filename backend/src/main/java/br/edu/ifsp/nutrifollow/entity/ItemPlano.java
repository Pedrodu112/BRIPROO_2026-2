
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;

import java.math.BigDecimal;

@Entity
public class ItemPlano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "plano_id", nullable = false)
    private Plano plano;

    private Integer ordem;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    private String grupoMuscular;
    private Integer seriesPrevistas;
    private Integer repeticoesPrevistas;
    private BigDecimal cargaPrevistaKg;
    private Integer descansoSegundos;
    private String horarioSugerido;

    @Column(nullable = false)
    private Boolean ativo = true;

    public ItemPlano() {
    }

    public boolean validarConfiguracao() {
        if (plano == null || titulo == null || titulo.isBlank()) {
            return false;
        }

        if ("TREINO".equalsIgnoreCase(plano.getTipo())) {
            return seriesPrevistas != null
                && seriesPrevistas > 0
                && repeticoesPrevistas != null
                && repeticoesPrevistas > 0
                && (cargaPrevistaKg == null
                    || cargaPrevistaKg.compareTo(BigDecimal.ZERO) >= 0)
                && (descansoSegundos == null || descansoSegundos >= 0);
        }

        if ("ALIMENTAR".equalsIgnoreCase(plano.getTipo())) {
            return descricao != null && !descricao.isBlank();
        }

        return false;
    }

    public void atualizarConfiguracao(ItemPlano novosDados) {
        if (novosDados == null) {
            throw new IllegalArgumentException(
                "Os novos dados do item são obrigatórios."
            );
        }

        if (plano == null || !Boolean.TRUE.equals(ativo)) {
            throw new IllegalStateException(
                "O item precisa pertencer a um plano e estar ativo."
            );
        }

        if (novosDados.titulo == null || novosDados.titulo.isBlank()) {
            throw new IllegalArgumentException(
                "O título do item é obrigatório."
            );
        }

        if ("TREINO".equalsIgnoreCase(plano.getTipo())) {
            if (novosDados.seriesPrevistas == null
                || novosDados.seriesPrevistas <= 0
                || novosDados.repeticoesPrevistas == null
                || novosDados.repeticoesPrevistas <= 0
                || (novosDados.cargaPrevistaKg != null
                    && novosDados.cargaPrevistaKg.compareTo(BigDecimal.ZERO) < 0)
                || (novosDados.descansoSegundos != null
                    && novosDados.descansoSegundos < 0)) {
                throw new IllegalArgumentException(
                    "A configuração do treino é inválida."
                );
            }
        } else if ("ALIMENTAR".equalsIgnoreCase(plano.getTipo())) {
            if (novosDados.descricao == null
                || novosDados.descricao.isBlank()) {
                throw new IllegalArgumentException(
                    "A descrição do item alimentar é obrigatória."
                );
            }
        } else {
            throw new IllegalStateException(
                "O tipo do plano é inválido."
            );
        }

        if (novosDados.descricao != null
            && novosDados.descricao.trim().length() > 2000) {
            throw new IllegalArgumentException(
                "A descrição não pode ultrapassar 2000 caracteres."
            );
        }

        this.ordem = novosDados.ordem;
        this.titulo = novosDados.titulo.trim();
        this.descricao = novosDados.descricao == null
            ? null : novosDados.descricao.trim();
        this.grupoMuscular = novosDados.grupoMuscular;
        this.seriesPrevistas = novosDados.seriesPrevistas;
        this.repeticoesPrevistas = novosDados.repeticoesPrevistas;
        this.cargaPrevistaKg = novosDados.cargaPrevistaKg;
        this.descansoSegundos = novosDados.descansoSegundos;
        this.horarioSugerido = novosDados.horarioSugerido;
    }

    public Integer getId() { return id; }
    public Plano getPlano() { return plano; }
    public void setPlano(Plano plano) { this.plano = plano; }
    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getGrupoMuscular() { return grupoMuscular; }
    public void setGrupoMuscular(String grupoMuscular) { this.grupoMuscular = grupoMuscular; }
    public Integer getSeriesPrevistas() { return seriesPrevistas; }
    public void setSeriesPrevistas(Integer seriesPrevistas) { this.seriesPrevistas = seriesPrevistas; }
    public Integer getRepeticoesPrevistas() { return repeticoesPrevistas; }
    public void setRepeticoesPrevistas(Integer repeticoesPrevistas) { this.repeticoesPrevistas = repeticoesPrevistas; }
    public BigDecimal getCargaPrevistaKg() { return cargaPrevistaKg; }
    public void setCargaPrevistaKg(BigDecimal cargaPrevistaKg) { this.cargaPrevistaKg = cargaPrevistaKg; }
    public Integer getDescansoSegundos() { return descansoSegundos; }
    public void setDescansoSegundos(Integer descansoSegundos) { this.descansoSegundos = descansoSegundos; }
    public String getHorarioSugerido() { return horarioSugerido; }
    public void setHorarioSugerido(String horarioSugerido) { this.horarioSugerido = horarioSugerido; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
