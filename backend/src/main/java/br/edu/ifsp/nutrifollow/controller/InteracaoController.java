
package br.edu.ifsp.nutrifollow.controller;

import br.edu.ifsp.nutrifollow.dto.InteracaoDados;
import br.edu.ifsp.nutrifollow.dto.InteracaoResposta;
import br.edu.ifsp.nutrifollow.entity.Interacao;
import br.edu.ifsp.nutrifollow.service.InteracaoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/interacoes")
@Validated
public class InteracaoController {

    private final InteracaoService interacaoService;

    public InteracaoController(InteracaoService interacaoService) {
        this.interacaoService = interacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InteracaoResposta enviar(
            @RequestParam Integer acompanhamentoId,
            @RequestParam Long autorId,
            @RequestParam(required = false) Long destinatarioId,
            @Valid @RequestBody InteracaoDados dados) {

        Interacao interacao = interacaoService.enviar(
                acompanhamentoId,
                autorId,
                destinatarioId,
                dados.tipo(),
                dados.conteudo(),
                dados.prazo(),
                dados.situacaoMeta()
        );

        return converterResposta(interacao);
    }

    @GetMapping("/acompanhamento/{acompanhamentoId}")
    public List<InteracaoResposta> listarPorAcompanhamento(
            @PathVariable Integer acompanhamentoId,
            @RequestParam Long usuarioId) {

        return interacaoService.listarPorAcompanhamento(
                acompanhamentoId,
                usuarioId
        ).stream()
                .map(this::converterResposta)
                .toList();
    }

    @PutMapping("/{interacaoId}")
    public InteracaoResposta atualizar(
            @PathVariable Integer interacaoId,
            @RequestParam Long usuarioId,
            @Valid @RequestBody AtualizacaoDados dados) {

        Interacao interacao = interacaoService.atualizar(
                interacaoId,
                usuarioId,
                dados.conteudo(),
                dados.prazo()
        );

        return converterResposta(interacao);
    }

    @PatchMapping("/{interacaoId}/situacao-meta")
    public InteracaoResposta atualizarSituacaoMeta(
            @PathVariable Integer interacaoId,
            @RequestParam Long usuarioId,
            @Valid @RequestBody SituacaoMetaDados dados) {

        Interacao interacao = interacaoService.atualizarSituacaoMeta(
                interacaoId,
                usuarioId,
                dados.situacao()
        );

        return converterResposta(interacao);
    }

    @PatchMapping("/{interacaoId}/lida")
    public InteracaoResposta marcarComoLida(
            @PathVariable Integer interacaoId,
            @RequestParam Long destinatarioId) {

        Interacao interacao = interacaoService.marcarComoLida(
                interacaoId,
                destinatarioId
        );

        return converterResposta(interacao);
    }

    private InteracaoResposta converterResposta(Interacao interacao) {

        return new InteracaoResposta(
                interacao.getId(),
                interacao.getAcompanhamento().getId(),
                interacao.getTipo(),
                interacao.getAutor().getId(),
                interacao.getDestinatario() == null
                        ? null
                        : interacao.getDestinatario().getId(),
                interacao.getConteudo(),
                interacao.getPrazo(),
                interacao.getSituacaoMeta(),
                interacao.getCriadaEm(),
                interacao.getAtualizadaEm(),
                interacao.getLidaEm()
        );
    }

    public record AtualizacaoDados(

            @NotBlank(message = "O conteúdo é obrigatório.")
            @Size(max = 2000, message = "O conteúdo deve ter no máximo 2000 caracteres.")
            String conteudo,

            LocalDateTime prazo
    ) {
    }

    public record SituacaoMetaDados(

            @NotBlank(message = "A situação da meta é obrigatória.")
            @Size(max = 50, message = "A situação deve ter no máximo 50 caracteres.")
            String situacao
    ) {
    }
}
