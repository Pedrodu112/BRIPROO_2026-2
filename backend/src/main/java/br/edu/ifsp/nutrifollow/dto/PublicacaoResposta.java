
package br.edu.ifsp.nutrifollow.dto;

import java.time.LocalDateTime;

public record PublicacaoResposta(
        Long id,
        Long autorId,
        String titulo,
        String conteudo,
        Boolean ativa,
        LocalDateTime publicadaEm,
        LocalDateTime inativadaEm,
        String motivoInativacao
) {
}
