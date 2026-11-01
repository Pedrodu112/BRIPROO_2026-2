
package br.edu.ifsp.nutrifollow.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ItemPlanoDados(
        Integer ordem,

        @NotBlank
        String titulo,

        String descricao,
        String grupoMuscular,

        @Positive
        Integer seriesPrevistas,

        @Positive
        Integer repeticoesPrevistas,

        BigDecimal cargaPrevistaKg,
        Integer descansoSegundos,
        String horarioSugerido
) {}
