
package br.edu.ifsp.nutrifollow.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AcompanhamentoResposta(
        Integer id,
        Long usuarioId,
        Long profissionalId,
        String finalidade,
        String status,
        List<String> permissoesConcedidas,
        LocalDateTime solicitadoEm,
        LocalDateTime respondidoEm,
        String motivoRecusa,
        LocalDateTime autorizadoEm,
        LocalDateTime iniciadoEm,
        LocalDateTime encerradoEm,
        Long encerradoPorId,
        String motivoEncerramento
) {}
