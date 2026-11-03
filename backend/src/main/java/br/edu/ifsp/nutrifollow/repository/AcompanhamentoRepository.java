package br.edu.ifsp.nutrifollow.repository;

import br.edu.ifsp.nutrifollow.entity.Acompanhamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcompanhamentoRepository
        extends JpaRepository<Acompanhamento, Integer> {

    List<Acompanhamento> findByUsuario_Id(Long usuarioId);

    List<Acompanhamento> findByProfissional_Id(Long profissionalId);
}