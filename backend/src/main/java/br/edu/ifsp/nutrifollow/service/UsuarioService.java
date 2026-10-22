
package br.edu.ifsp.nutrifollow.service;

import br.edu.ifsp.nutrifollow.dto.UsuarioCadastro;
import br.edu.ifsp.nutrifollow.entity.UsuarioComum;
import br.edu.ifsp.nutrifollow.repository.UsuarioRepository;

import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioComum cadastrar(UsuarioCadastro dados) {
        if (dados == null) {
            throw new IllegalArgumentException(
                "Os dados do cadastro são obrigatórios."
            );
        }

        String nome = dados.nome().trim();
        String email = dados.email().trim().toLowerCase(Locale.ROOT);

        if (nome.isBlank()) {
            throw new IllegalArgumentException(
                "O nome é obrigatório."
            );
        }

        if (email.isBlank()) {
            throw new IllegalArgumentException(
                "O e-mail é obrigatório."
            );
        }

        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                "Este e-mail já está cadastrado."
            );
        }

        UsuarioComum usuario = new UsuarioComum();

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.alterarSenha(dados.senha());
        usuario.setAtivo(true);

        return usuarioRepository.save(usuario);
    }
}
