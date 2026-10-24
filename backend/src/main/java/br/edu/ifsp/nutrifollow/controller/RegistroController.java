
package br.edu.ifsp.nutrifollow.controller;

import br.edu.ifsp.nutrifollow.dto.RegistroDados;
import br.edu.ifsp.nutrifollow.dto.RegistroResposta;
import br.edu.ifsp.nutrifollow.entity.Registro;
import br.edu.ifsp.nutrifollow.service.RegistroService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios/{usuarioId}/registros")
public class RegistroController {

    private final RegistroService registroService;

    public RegistroController(RegistroService registroService) {
        this.registroService = registroService;
    }

    @PostMapping
    public RegistroResposta registrar(
            @PathVariable Long usuarioId,
            @Valid @RequestBody RegistroDados dados) {

        Registro registro = converterParaEntidade(dados);

        return converterParaResposta(
                registroService.registrar(usuarioId, registro)
        );
    }

    @GetMapping
    public List<RegistroResposta> listar(
            @PathVariable Long usuarioId) {

        return registroService.listarPorUsuario(usuarioId)
                .stream()
                .map(this::converterParaResposta)
                .toList();
    }

    @GetMapping("/{registroId}")
    public RegistroResposta consultar(
            @PathVariable Long usuarioId,
            @PathVariable Integer registroId) {

        return converterParaResposta(
                registroService.consultar(usuarioId, registroId)
        );
    }

    @PutMapping("/{registroId}")
    public RegistroResposta corrigir(
            @PathVariable Long usuarioId,
            @PathVariable Integer registroId,
            @Valid @RequestBody RegistroDados dados) {

        Registro novosDados = converterParaEntidade(dados);

        return converterParaResposta(
                registroService.corrigir(
                        usuarioId, registroId, novosDados
                )
        );
    }

    @DeleteMapping("/{registroId}")
    public void inativar(
            @PathVariable Long usuarioId,
            @PathVariable Integer registroId) {

        registroService.inativar(usuarioId, registroId);
    }

    private Registro converterParaEntidade(RegistroDados dados) {
        Registro registro = new Registro();

        registro.setTipo(dados.tipo());
        registro.setDescricao(dados.descricao());
        registro.setTipoRefeicao(dados.tipoRefeicao());
        registro.setSeries(dados.series());
        registro.setRepeticoes(dados.repeticoes());
        registro.setCargaKg(dados.cargaKg());
        registro.setQuantidadeMl(dados.quantidadeMl());
        registro.setPesoKg(dados.pesoKg());
        registro.setObservacoes(dados.observacoes());

        return registro;
    }

    private RegistroResposta converterParaResposta(Registro registro) {
        return new RegistroResposta(
                registro.getId(),
                registro.getTipo(),
                registro.getRegistradoEm(),
                registro.getDescricao(),
                registro.getTipoRefeicao(),
                registro.getSeries(),
                registro.getRepeticoes(),
                registro.getCargaKg(),
                registro.getQuantidadeMl(),
                registro.getPesoKg(),
                registro.getObservacoes(),
                registro.getAtivo()
        );
    }
}
