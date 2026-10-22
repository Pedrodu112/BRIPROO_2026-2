
package br.edu.ifsp.nutrifollow.service;

import br.edu.ifsp.nutrifollow.entity.Usuario;
import br.edu.ifsp.nutrifollow.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario cadastrar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado.");
        }

        usuario.setAtivo(true);
        usuario.alterarSenha(usuario.getSenhaHash());

        return usuarioRepository.save(usuario);
    }

    
}
