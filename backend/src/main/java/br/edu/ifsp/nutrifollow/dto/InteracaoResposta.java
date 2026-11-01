
package br.edu.ifsp.nutrifollow.dto;

import java.time.LocalDateTime;

public record InteracaoResposta(
        Integer id,
        Integer acompanhamentoId,
        String tipo,
        Long autorId,
        Long destinatarioId,
        String conteudo,
        LocalDateTime prazo,
        String situacaoMeta,
        LocalDateTime criadaEm,
        LocalDateTime atualizadaEm,
        LocalDateTime lidaEm
) {
}
