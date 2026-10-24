
package br.edu.ifsp.nutrifollow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublicacaoModeracaoDados(

        @NotBlank(message = "O motivo da moderação é obrigatório.")
        @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres.")
        String motivo
) {
}