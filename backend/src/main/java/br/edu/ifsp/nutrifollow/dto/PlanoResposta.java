package br.edu.ifsp.nutrifollow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PlanoResposta(
        Integer id,
        Long usuarioId,
        String tipo,
        String titulo,
        String objetivo,
        List<ItemPlanoResposta> itens,
        Long criadoPorId,
        LocalDateTime criadoEm,
        Integer versao,
        String resumoUltimaAlteracao,
        Boolean ativo,
        Long atualizadoPorId,
        LocalDateTime atualizadoEm
) {
    public record ItemPlanoResposta(
            Integer id,
            Integer ordem,
            String titulo,
            String descricao,
            String grupoMuscular,
            Integer seriesPrevistas,
            Integer repeticoesPrevistas,
            BigDecimal cargaPrevistaKg,
            Integer descansoSegundos,
            String horarioSugerido,
            Boolean ativo
    ) {}
}