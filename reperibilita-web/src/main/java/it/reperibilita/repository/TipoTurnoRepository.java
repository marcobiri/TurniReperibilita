package it.reperibilita.repository;

import it.reperibilita.domain.TipoTurno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoTurnoRepository extends JpaRepository<TipoTurno, Long> {

    Optional<TipoTurno> findByNomeIgnoreCase(String nome);
}
