
package br.edu.ifsp.nutrifollow.repository;

import br.edu.ifsp.nutrifollow.entity.Acompanhamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcompanhamentoRepository
        extends JpaRepository<Acompanhamento, Long> {
}
