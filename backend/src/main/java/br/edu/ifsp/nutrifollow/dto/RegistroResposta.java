
package br.edu.ifsp.nutrifollow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RegistroResposta(
        Integer id,
        String tipo,
        LocalDateTime registradoEm,
        String descricao,
        String tipoRefeicao,
        Integer series,
        Integer repeticoes,
        BigDecimal cargaKg,
        BigDecimal quantidadeMl,
        BigDecimal pesoKg,
        String observacoes,
        Boolean ativo
) {
}
