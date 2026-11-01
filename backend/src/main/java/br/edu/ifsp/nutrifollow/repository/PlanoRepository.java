package br.edu.ifsp.nutrifollow.repository;

import br.edu.ifsp.nutrifollow.entity.Plano;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanoRepository extends JpaRepository<Plano, Integer> {

    List<Plano> findByUsuario_IdAndAtivoTrue(Long usuarioId);
}