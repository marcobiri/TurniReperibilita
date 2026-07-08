package it.reperibilita.repository;

import it.reperibilita.domain.Tariffa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TariffaRepository extends JpaRepository<Tariffa, Long> {
}
