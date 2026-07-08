package it.reperibilita.repository;

import it.reperibilita.domain.Festivita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FestivitaRepository extends JpaRepository<Festivita, LocalDate> {

    List<Festivita> findByDataBetweenOrderByData(LocalDate dal, LocalDate al);

    boolean existsByData(LocalDate data);
}
