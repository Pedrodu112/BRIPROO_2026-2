
package br.edu.ifsp.nutrifollow.repository;

import br.edu.ifsp.nutrifollow.entity.Registro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroRepository
        extends JpaRepository<Registro, Integer> {

    List<Registro> findByUsuario_IdAndAtivoTrue(Long usuarioId);
}
