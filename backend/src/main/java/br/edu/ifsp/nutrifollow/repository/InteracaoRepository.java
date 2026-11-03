package br.edu.ifsp.nutrifollow.repository;

import br.edu.ifsp.nutrifollow.entity.Interacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteracaoRepository
        extends JpaRepository<Interacao, Integer> {

    List<Interacao> findByAcompanhamento_IdOrderByCriadaEmAsc(
            Integer acompanhamentoId
    );
}