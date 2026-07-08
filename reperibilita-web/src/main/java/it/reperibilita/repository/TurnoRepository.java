package it.reperibilita.repository;

import it.reperibilita.domain.Servizio;
import it.reperibilita.domain.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByDataBetweenAndServizioOrderByDataAscTipoTurnoIdAsc(LocalDate dal, LocalDate al, Servizio servizio);

    List<Turno> findByDataBetweenOrderByDataAscTipoTurnoIdAsc(LocalDate dal, LocalDate al);

    List<Turno> findByServizio(Servizio servizio);

    Optional<Turno> findByDataAndTipoTurnoIdAndServizio(LocalDate data, Long tipoTurnoId, Servizio servizio);

    @Query("select t from Turno t where t.data = :data and t.operatore.codice = :codiceOperatore")
    List<Turno> findByDataAndOperatore(@Param("data") LocalDate data, @Param("codiceOperatore") String codiceOperatore);

    @Query("select t from Turno t where t.operatore.codice = :codiceOperatore and t.data between :dal and :al order by t.data")
    List<Turno> findByOperatoreAndPeriodo(@Param("codiceOperatore") String codiceOperatore,
                                           @Param("dal") LocalDate dal, @Param("al") LocalDate al);
}
