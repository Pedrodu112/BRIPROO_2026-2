package br.edu.ifsp.nutrifollow.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public record ItemPlanoDados(
        Integer ordem,

        @NotBlank(message = "O título do item é obrigatório.")
        @Size(max = 255, message = "O título não pode ultrapassar 255 caracteres.")
        String titulo,

        @Size(max = 2000, message = "A descrição não pode ultrapassar 2000 caracteres.")
        String descricao,

        @Size(max = 255)
        String grupoMuscular,

        @Positive(message = "As séries devem ser maiores que zero.")
        Integer seriesPrevistas,

        @Positive(message = "As repetições devem ser maiores que zero.")
        Integer repeticoesPrevistas,

        @DecimalMin(value = "0.0", message = "A carga não pode ser negativa.")
        BigDecimal cargaPrevistaKg,

        @PositiveOrZero(message = "O descanso não pode ser negativo.")
        Integer descansoSegundos,

        @Size(max = 255)
        String horarioSugerido
) {}