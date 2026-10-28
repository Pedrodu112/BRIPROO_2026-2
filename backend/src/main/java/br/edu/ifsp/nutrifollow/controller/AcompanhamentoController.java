
package br.edu.ifsp.nutrifollow.controller;

import br.edu.ifsp.nutrifollow.dto.AcompanhamentoResposta;
import br.edu.ifsp.nutrifollow.entity.Acompanhamento;
import br.edu.ifsp.nutrifollow.service.AcompanhamentoService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/acompanhamentos")
public class AcompanhamentoController {

    private final AcompanhamentoService acompanhamentoService;

    public AcompanhamentoController(
            AcompanhamentoService acompanhamentoService) {
        this.acompanhamentoService = acompanhamentoService;
    }

    @PostMapping
    public AcompanhamentoResposta solicitar(
            @RequestParam Long usuarioId,
            @RequestParam Long profissionalId,
            @Valid @RequestBody SolicitacaoDados dados) {

        return converterParaResposta(
                acompanhamentoService.solicitar(
                        usuarioId, profissionalId, dados.finalidade()));
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<AcompanhamentoResposta> listarPorUsuario(
            @PathVariable Long usuarioId) {

        return acompanhamentoService.listarPorUsuario(usuarioId)
                .stream().map(this::converterParaResposta).toList();
    }

    @GetMapping("/profissional/{profissionalId}")
    public List<AcompanhamentoResposta> listarPorProfissional(
            @PathVariable Long profissionalId) {

        return acompanhamentoService.listarPorProfissional(profissionalId)
                .stream().map(this::converterParaResposta).toList();
    }

    @PutMapping("/{id}/aceitar")
    public AcompanhamentoResposta aceitar(
            @PathVariable Integer id,
            @RequestParam Long profissionalId) {

        return converterParaResposta(
                acompanhamentoService.aceitar(id, profissionalId));
    }

    @PutMapping("/{id}/recusar")
    public AcompanhamentoResposta recusar(
            @PathVariable Integer id,
            @RequestParam Long profissionalId,
            @Valid @RequestBody MotivoDados dados) {

        return converterParaResposta(
                acompanhamentoService.recusar(id, profissionalId, dados.motivo()));
    }

    @PutMapping("/{id}/autorizar")
    public AcompanhamentoResposta autorizar(
            @PathVariable Integer id,
            @RequestParam Long usuarioId,
            @Valid @RequestBody PermissoesDados dados) {

        return converterParaResposta(
                acompanhamentoService.autorizarCompartilhamento(
                        id, usuarioId, dados.permissoes()));
    }

    @PutMapping("/{id}/encerrar")
    public AcompanhamentoResposta encerrar(
            @PathVariable Integer id,
            @RequestParam Long autorId,
            @Valid @RequestBody MotivoDados dados) {

        return converterParaResposta(
                acompanhamentoService.encerrar(id, autorId, dados.motivo()));
    }

    private AcompanhamentoResposta converterParaResposta(
            Acompanhamento acompanhamento) {

        return new AcompanhamentoResposta(
                acompanhamento.getId(),
                acompanhamento.getUsuario().getId(),
                acompanhamento.getProfissional().getId(),
                acompanhamento.getFinalidade(),
                acompanhamento.getStatus(),
                acompanhamento.getSolicitadoEm(),
                acompanhamento.getRespondidoEm(),
                acompanhamento.getMotivoRecusa(),
                acompanhamento.getAutorizadoEm(),
                acompanhamento.getIniciadoEm(),
                acompanhamento.getEncerradoEm(),
                acompanhamento.getEncerradoPor() == null
                        ? null : acompanhamento.getEncerradoPor().getId(),
                acompanhamento.getMotivoEncerramento()
        );
    }

    public record SolicitacaoDados(
            @NotBlank @Size(max = 500) String finalidade) {}

    public record MotivoDados(
            @NotBlank @Size(max = 500) String motivo) {}

    public record PermissoesDados(
            @NotEmpty List<@NotBlank String> permissoes) {}
}
