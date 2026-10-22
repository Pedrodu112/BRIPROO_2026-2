
package br.edu.ifsp.nutrifollow.controller;

import br.edu.ifsp.nutrifollow.dto.UsuarioCadastro;
import br.edu.ifsp.nutrifollow.dto.UsuarioResposta;
import br.edu.ifsp.nutrifollow.entity.UsuarioComum;
import br.edu.ifsp.nutrifollow.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public UsuarioResposta cadastrar(
        @Valid @RequestBody UsuarioCadastro dados) {

        UsuarioComum usuarioSalvo = usuarioService.cadastrar(dados);

        return new UsuarioResposta(
            usuarioSalvo.getId(),
            usuarioSalvo.getNome(),
            usuarioSalvo.getEmail(),
            usuarioSalvo.getAtivo()
        );
    }
}
