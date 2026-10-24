
package br.edu.ifsp.nutrifollow.controller;

import br.edu.ifsp.nutrifollow.dto.PublicacaoDados;
import br.edu.ifsp.nutrifollow.dto.PublicacaoModeracaoDados;
import br.edu.ifsp.nutrifollow.dto.PublicacaoResposta;
import br.edu.ifsp.nutrifollow.entity.Publicacao;
import br.edu.ifsp.nutrifollow.service.PublicacaoService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publicacoes")
public class PublicacaoController {

    private final PublicacaoService publicacaoService;

    public PublicacaoController(PublicacaoService publicacaoService) {
        this.publicacaoService = publicacaoService;
    }

    @PostMapping
    public PublicacaoResposta publicar(
            @RequestParam Long profissionalId,
            @Valid @RequestBody PublicacaoDados dados) {

        Publicacao publicacao = publicacaoService.publicar(
                profissionalId,
                dados.titulo(),
                dados.conteudo()
        );

        return converterParaResposta(publicacao);
    }

    @GetMapping
    public List<PublicacaoResposta> listar() {
        return publicacaoService.listarExibiveis()
                .stream()
                .map(this::converterParaResposta)
                .toList();
    }

    @PutMapping("/{publicacaoId}")
    public PublicacaoResposta editar(
            @PathVariable Long publicacaoId,
            @RequestParam Long profissionalId,
            @Valid @RequestBody PublicacaoDados dados) {

        Publicacao publicacao = publicacaoService.editar(
                profissionalId,
                publicacaoId,
                dados.titulo(),
                dados.conteudo()
        );

        return converterParaResposta(publicacao);
    }

    @DeleteMapping("/{publicacaoId}")
    public void inativar(
            @PathVariable Long publicacaoId,
            @RequestParam Long administradorId,
            @Valid @RequestBody PublicacaoModeracaoDados dados) {

        publicacaoService.inativar(
                administradorId,
                publicacaoId,
                dados.motivo()
        );
    }

    private PublicacaoResposta converterParaResposta(
            Publicacao publicacao) {

        return new PublicacaoResposta(
                publicacao.getId(),
                publicacao.getAutor().getId(),
                publicacao.getTitulo(),
                publicacao.getConteudo(),
                publicacao.getAtiva(),
                publicacao.getPublicadaEm(),
                publicacao.getInativadaEm(),
                publicacao.getMotivoInativacao()

                
        );

        
    }

    
}
