
package br.edu.ifsp.nutrifollow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record InteracaoDados(

        @NotBlank(message = "O tipo da interação é obrigatório.")
        @Size(max = 20, message = "O tipo deve ter no máximo 20 caracteres.")
        String tipo,

        @NotBlank(message = "O conteúdo é obrigatório.")
        @Size(max = 2000, message = "O conteúdo deve ter no máximo 2000 caracteres.")
        String conteudo,

        LocalDateTime prazo,

        @Size(max = 50, message = "A situação da meta deve ter no máximo 50 caracteres.")
        String situacaoMeta
) {
}
