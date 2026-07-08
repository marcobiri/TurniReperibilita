package it.reperibilita.repository;

import it.reperibilita.domain.AbbinamentoOperatori;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbbinamentoOperatoriRepository extends JpaRepository<AbbinamentoOperatori, Long> {

    List<AbbinamentoOperatori> findByPrimoOperatoreCodice(String codiceOperatore);
}
