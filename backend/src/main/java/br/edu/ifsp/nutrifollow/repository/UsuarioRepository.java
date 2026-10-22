
package br.edu.ifsp.nutrifollow.repository;

import br.edu.ifsp.nutrifollow.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

}
