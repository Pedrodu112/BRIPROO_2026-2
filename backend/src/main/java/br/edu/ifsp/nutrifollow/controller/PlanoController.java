
package br.edu.ifsp.nutrifollow.controller;

import jakarta.validation.constraints.Size;
import br.edu.ifsp.nutrifollow.dto.PlanoResposta;
import br.edu.ifsp.nutrifollow.dto.ItemPlanoDados;
import br.edu.ifsp.nutrifollow.entity.ItemPlano;
import br.edu.ifsp.nutrifollow.entity.Plano;
import br.edu.ifsp.nutrifollow.service.PlanoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/planos")
@Validated
public class PlanoController {

    private final PlanoService planoService;

    public PlanoController(PlanoService planoService) {
        this.planoService = planoService;
    }

    @PostMapping("/treino")
    public PlanoResposta criarTreino(
            @RequestParam Long profissionalId,
            @RequestParam Integer acompanhamentoId,
            @RequestParam @NotBlank String titulo,
            @Valid @RequestBody ItensDados dados) {

        return converterParaResposta(
                planoService.criarTreino(
                        profissionalId,
                        acompanhamentoId,
                        titulo,
                        converterItens(dados.itens())
                )
        );
    }

    @PostMapping("/treino/usuario")
    public PlanoResposta criarTreinoProprio(
            @RequestParam @NotNull Long usuarioId,
            @RequestParam @NotBlank @Size(max = 255) String titulo,
            @Valid @RequestBody ItensDados dados) {

        return converterParaResposta(
            planoService.criarTreinoProprio(
                usuarioId,
                titulo,
                converterItens(dados.itens())
            )
        );
    }

    @PostMapping("/alimentar")
    public PlanoResposta criarAlimentar(
            @RequestParam Long profissionalId,
            @RequestParam Integer acompanhamentoId,
            @Valid @RequestBody ItensDados dados) {

        return converterParaResposta(
                planoService.criarAlimentar(
                        profissionalId,
                        acompanhamentoId,
                        converterItens(dados.itens())
                )
        );
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<PlanoResposta> listarPorUsuario(
            @PathVariable Long usuarioId) {

        return planoService.listarPorUsuario(usuarioId)
                .stream()
                .map(this::converterParaResposta)
                .toList();
    }

    @PutMapping("/{planoId}")
    public PlanoResposta alterar(
            @PathVariable Integer planoId,
            @RequestParam Long profissionalId,
            @RequestParam Integer acompanhamentoId,
            @Valid @RequestBody ItensDados dados) {

        return converterParaResposta(
                planoService.alterar(
                        profissionalId,
                        acompanhamentoId,
                        planoId,
                        converterItens(dados.itens())
                )
        );
    }

    @PutMapping("/{planoId}/usuario")
    public PlanoResposta alterarTreinoProprio(
            @PathVariable Integer planoId,
            @RequestParam @NotNull Long usuarioId,
            @Valid @RequestBody ItensDados dados) {

        return converterParaResposta(
            planoService.alterarProprio(
                usuarioId,
                planoId,
                converterItens(dados.itens())
            )
        );
    }

    @DeleteMapping("/{planoId}")
    public PlanoResposta inativar(
            @PathVariable Integer planoId,
            @RequestParam Long autorId) {

        return converterParaResposta(
                planoService.inativar(planoId, autorId)
        );
    }

    private List<ItemPlano> converterItens(List<ItemPlanoDados> dados) {
        return dados.stream().map(item -> {
            ItemPlano novo = new ItemPlano();

            novo.setOrdem(item.ordem());
            novo.setTitulo(item.titulo());
            novo.setDescricao(item.descricao());
            novo.setGrupoMuscular(item.grupoMuscular());
            novo.setSeriesPrevistas(item.seriesPrevistas());
            novo.setRepeticoesPrevistas(item.repeticoesPrevistas());
            novo.setCargaPrevistaKg(item.cargaPrevistaKg());
            novo.setDescansoSegundos(item.descansoSegundos());
            novo.setHorarioSugerido(item.horarioSugerido());

            return novo;
        }).toList();
    }

    private PlanoResposta converterParaResposta(Plano plano) {
        List<PlanoResposta.ItemPlanoResposta> itens =
                plano.getItens().stream()
                        .map(item -> new PlanoResposta.ItemPlanoResposta(
                                item.getId(),
                                item.getOrdem(),
                                item.getTitulo(),
                                item.getDescricao(),
                                item.getGrupoMuscular(),
                                item.getSeriesPrevistas(),
                                item.getRepeticoesPrevistas(),
                                item.getCargaPrevistaKg() == null
                                        ? null
                                        : item.getCargaPrevistaKg().toString(),
                                item.getDescansoSegundos(),
                                item.getHorarioSugerido(),
                                item.getAtivo()
                        ))
                        .toList();

            return new PlanoResposta(
            plano.getId(),
            plano.getUsuario().getId(),
            plano.getTipo(),
            plano.getTitulo(),
            plano.getObjetivo(),
            itens,
            plano.getCriadoPor().getId(),
            plano.getCriadoEm(),
            plano.getVersao(),
            plano.getResumoUltimaAlteracao(),
            plano.getAtivo(),
            plano.getAtualizadoPor() == null
                    ? null
                    : plano.getAtualizadoPor().getId(),
            plano.getAtualizadoEm()
    );
        
    }
     public record ItensDados(
        @NotEmpty List<@NotNull @Valid ItemPlanoDados> itens
    ) {}

}