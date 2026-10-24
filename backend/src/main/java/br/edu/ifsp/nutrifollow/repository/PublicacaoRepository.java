
package br.edu.ifsp.nutrifollow.repository;

import br.edu.ifsp.nutrifollow.entity.Publicacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicacaoRepository
        extends JpaRepository<Publicacao, Long> {
}
