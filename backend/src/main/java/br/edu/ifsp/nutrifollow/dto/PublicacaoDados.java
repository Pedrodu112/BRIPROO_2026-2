
package br.edu.ifsp.nutrifollow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublicacaoDados(

        @NotBlank(message = "O título é obrigatório.")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres.")
        String titulo,

        @NotBlank(message = "O conteúdo é obrigatório.")
        @Size(max = 5000, message = "O conteúdo deve ter no máximo 5000 caracteres.")
        String conteudo
) {
}
