
package br.edu.ifsp.nutrifollow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RegistroDados(

        @NotBlank(message = "O tipo do registro é obrigatório.")
        @Size(max = 20, message = "O tipo deve ter no máximo 20 caracteres.")
        String tipo,

        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
        String descricao,

        String tipoRefeicao,
        Integer series,
        Integer repeticoes,
        BigDecimal cargaKg,
        BigDecimal quantidadeMl,
        BigDecimal pesoKg,

        @Size(max = 2000, message = "As observações devem ter no máximo 2000 caracteres.")
        String observacoes
) {
}
