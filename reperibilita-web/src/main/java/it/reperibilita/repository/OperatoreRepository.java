package it.reperibilita.repository;

import it.reperibilita.domain.Operatore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperatoreRepository extends JpaRepository<Operatore, String> {

    List<Operatore> findByAttivoTrueOrderByCognomeAscNomeAsc();

    List<Operatore> findAllByOrderByCognomeAscNomeAsc();
}
