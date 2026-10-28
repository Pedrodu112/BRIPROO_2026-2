
package br.edu.ifsp.nutrifollow.dto;

import java.time.LocalDateTime;

public record AcompanhamentoResposta(
        Integer id,
        Long usuarioId,
        Long profissionalId,
        String finalidade,
        String status,
        LocalDateTime solicitadoEm,
        LocalDateTime respondidoEm,
        String motivoRecusa,
        LocalDateTime autorizadoEm,
        LocalDateTime iniciadoEm,
        LocalDateTime encerradoEm,
        Long encerradoPorId,
        String motivoEncerramento
) {}
