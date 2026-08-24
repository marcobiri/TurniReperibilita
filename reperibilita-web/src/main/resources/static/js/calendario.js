let calendar;
let tipiTurno = [];
let operatori = [];
// id dell'altro turno (notturno festivo) quando il modale sta modificando un evento "giornata festiva"
// unificato (diurno+notturno festivo dello stesso giorno, stesso operatore); null altrimenti.
let turnoIdSecondario = null;
// Data/servizio con cui la coppia esisteva PRIMA della modifica: servono per eliminare la coppia
// originale se l'utente sposta il turno su un'altra data o un altro servizio, invece di lasciarla
// orfana nel database (la creazione di un nuovo turno usa data+tipo+servizio come chiave, quindi
// cambiandone uno si finisce su uno slot diverso da quello originale).
let turnoDataOriginale = null;
let turnoServizioOriginale = null;

const ETICHETTA_SERVIZIO = {REPERIBILITA: "Reperibile Informatica", FONIA: "Reperibile fonia"};
const ETICHETTA_STATO_BATCH = {
    CREATO: '<span class="badge verde">Creato</span>',
    AGGIORNATO: '<span class="badge blu">Aggiornato</span>',
    SALTATO: '<span class="badge grigio">Saltato</span>',
    ERRORE: '<span class="badge rosso">Errore</span>'
};

document.addEventListener("DOMContentLoaded", async () => {
    await Promise.all([caricaTipiTurno(), caricaOperatori()]);
    inizializzaCalendario();
    collegaEventi();
});

async function caricaTipiTurno() {
    try {
        tipiTurno = await Api.get("/api/tipi-turno");
        const opzioni = tipiTurno.map(t =>
            `<option value="${t.id}">${t.nome} (${t.oraInizio}-${t.oraFine})</option>`).join("");
        document.getElementById("turnoTipo").innerHTML = opzioni;
        document.getElementById("multiploTipo").innerHTML = opzioni;
    } catch (e) {
        mostraErrore("errore", e);
    }
}

async function caricaOperatori() {
    try {
        operatori = await Api.get("/api/operatori?soloAttivi=true");
        const opzioni = operatori.map(o => `<option value="${o.codice}">${o.nomeCompleto}</option>`).join("");
        document.getElementById("turnoOperatore").innerHTML = opzioni;
        document.getElementById("multiploOperatore").innerHTML = opzioni;

        const selectFiltro = document.getElementById("filtroOperatore");
        selectFiltro.innerHTML = '<option value="">Tutti</option>' + opzioni;
    } catch (e) {
        mostraErrore("errore", e);
    }
}

function inizializzaCalendario() {
    const el = document.getElementById("calendar");
    calendar = new FullCalendar.Calendar(el, {
        initialView: "dayGridMonth",
        locale: "it",
        firstDay: 1,
        height: "auto",
        headerToolbar: {left: "prev,next today", center: "title", right: "dayGridMonth,timeGridWeek"},
        // I turni notturni finiscono alle 08:00 del giorno dopo: senza questa soglia FullCalendar
        // li tratterebbe come eventi multi-giorno, disegnandoli come barre che si estendono nella
        // cella successiva e, con notti consecutive, si incatenano ripetendo il nome del turno.
        nextDayThreshold: "09:00:00",
        // Confinare i turni notturni al proprio giorno li farebbe passare allo stile "list-item"
        // (pallino + testo, senza sfondo) che FullCalendar usa di default per gli eventi di un solo
        // giorno: forziamo lo stile "block" per mantenere la barra colorata anche in vista mese.
        eventDisplay: "block",
        eventOrder: (a, b) => (a.servizio === b.servizio ? 0 : a.servizio === "REPERIBILITA" ? -1 : 1),
        // Il testo dell'evento viene troncato quando la cella e' stretta: il title nativo del
        // browser mostra per intero servizio, turno e operatore al passaggio del mouse.
        eventDidMount: (info) => {
            const etichetta = ETICHETTA_SERVIZIO[info.event.extendedProps.servizio] ?? "";
            const operatore = operatori.find(o => o.codice === info.event.extendedProps.codiceOperatore);
            const tipo = tipiTurno.find(t => t.id === info.event.extendedProps.tipoTurnoId);
            const giornataIntera = info.event.extendedProps.idSecondario ? " (giornata festiva: diurno + notturno, un solo turno)" : "";
            info.el.title = `${operatore?.nomeCompleto ?? ""} - ${etichetta} - ${tipo?.nome ?? ""}${giornataIntera}`;
            if (info.event.extendedProps.idSecondario) {
                info.el.classList.add("fc-event-giornata");
            }
        },
        events: caricaEventi,
        dateClick: (info) => apriModaleCreazione(info.dateStr),
        eventClick: (info) => apriModaleModifica(info.event),
        // Selezione trascinata su piu' giorni per l'assegnazione multipla (inserimento veloce).
        // Un singolo giorno selezionato (nessun trascinamento) e' gia' gestito da dateClick,
        // quindi qui viene ignorato per non aprire due modali insieme.
        selectable: true,
        select: (info) => {
            const giorniSelezionati = Math.round((info.end - info.start) / 86400000);
            if (giorniSelezionati < 2) {
                calendar.unselect();
                return;
            }
            const alInclusivo = new Date(info.end.getTime() - 86400000);
            apriModaleAssegnazioneMultipla(formatoData(info.start), formatoData(alInclusivo));
        }
    });
    calendar.render();
    osservaSelezioneTrascinata();
}

/**
 * Affievolisce il numero di ogni giorno coinvolto nel trascinamento per l'assegnazione multipla
 * (vedi .giorno-in-selezione in style.css). Calcolato in autonomia dalle date sotto il cursore
 * (data del giorno di partenza e data del giorno corrente), non affidandosi all'evidenziazione
 * ".fc-highlight" di FullCalendar ne' a event.target/elementFromPoint: FullCalendar disegna i
 * turni in un livello separato dalla cella del giorno (non annidato nel suo <td>), quindi quando
 * il cursore passa sopra un turno ne' event.target.closest(...) ne' elementFromPoint(...).closest(...)
 * risalgono alla cella giusta. Si individua invece geometricamente quale cella contiene il punto,
 * confrontando le sue coordinate con il rettangolo di ciascuna cella: funziona a prescindere da
 * cosa sia disegnato sopra in quel punto.
 */
function osservaSelezioneTrascinata() {
    const el = document.getElementById("calendar");
    let dataInizio = null;
    let dataCorrente = null;
    let aggiornamentoPianificato = false;

    const pianificaAggiornamento = () => {
        if (aggiornamentoPianificato) return;
        aggiornamentoPianificato = true;
        requestAnimationFrame(() => {
            aggiornamentoPianificato = false;
            evidenziaIntervalloSelezione(dataInizio, dataCorrente);
        });
    };

    el.addEventListener("mousedown", (evento) => {
        const cella = trovaCellaGiornoAlPunto(evento.clientX, evento.clientY);
        dataInizio = cella ? cella.dataset.date : null;
        dataCorrente = dataInizio;
    });
    el.addEventListener("mousemove", (evento) => {
        if (evento.buttons !== 1 || !dataInizio) return;
        const cella = trovaCellaGiornoAlPunto(evento.clientX, evento.clientY);
        if (!cella) return;
        dataCorrente = cella.dataset.date;
        pianificaAggiornamento();
    });
    document.addEventListener("mouseup", () => {
        dataInizio = null;
        dataCorrente = null;
        evidenziaIntervalloSelezione(null, null);
    });
}

function trovaCellaGiornoAlPunto(x, y) {
    const celle = document.querySelectorAll("#calendar .fc-daygrid-day[data-date]");
    for (const cella of celle) {
        const r = cella.getBoundingClientRect();
        if (x >= r.left && x <= r.right && y >= r.top && y <= r.bottom) {
            return cella;
        }
    }
    return null;
}

function evidenziaIntervalloSelezione(dal, al) {
    const dataMin = dal && al ? (dal < al ? dal : al) : null;
    const dataMax = dal && al ? (dal < al ? al : dal) : null;
    document.querySelectorAll("#calendar .fc-daygrid-day-number").forEach(numero => {
        const cella = numero.closest(".fc-daygrid-day[data-date]");
        const dentroIntervallo = dataMin !== null && cella.dataset.date >= dataMin && cella.dataset.date <= dataMax;
        numero.classList.toggle("giorno-in-selezione", dentroIntervallo);
    });
}

async function caricaEventi(fetchInfo, successCallback, failureCallback) {
    try {
        const dal = formatoData(fetchInfo.start);
        const al = formatoData(fetchInfo.end);
        const servizio = document.getElementById("filtroServizio").value;
        const operatore = document.getElementById("filtroOperatore").value;
        let url = `/api/turni/eventi?dal=${dal}&al=${al}`;
        if (servizio) url += `&servizio=${servizio}`;
        if (operatore) url += `&operatore=${operatore}`;
        const eventi = await Api.get(url);
        successCallback(eventi);
    } catch (e) {
        mostraErrore("errore", e);
        failureCallback(e);
    }
}

function collegaEventi() {
    document.getElementById("filtroServizio").addEventListener("change", () => calendar.refetchEvents());
    document.getElementById("filtroOperatore").addEventListener("change", () => calendar.refetchEvents());

    document.getElementById("btnAnnullaTurno").addEventListener("click", chiudiModaleTurno);
    document.getElementById("formTurno").addEventListener("submit", salvaTurno);
    document.getElementById("btnEliminaTurno").addEventListener("click", eliminaTurno);
    document.getElementById("turnoTipo").addEventListener("change", aggiornaRigaGiornata);

    document.getElementById("btnEsportaCsv").addEventListener("click", () => esporta("csv"));
    document.getElementById("btnEsportaIcs").addEventListener("click", () => esporta("ics"));
    document.getElementById("btnStampaPdf").addEventListener("click", apriPaginaStampa);

    document.getElementById("btnGeneraAnno").addEventListener("click", apriModaleGeneraAnno);
    document.getElementById("btnAnnullaGeneraAnno").addEventListener("click", chiudiModaleGeneraAnno);
    document.getElementById("btnAnteprimaGeneraAnno").addEventListener("click", caricaAnteprimaGenerazione);
    document.getElementById("btnConfermaGeneraAnno").addEventListener("click", confermaGenerazione);

    document.getElementById("btnAssegnazioneMultipla").addEventListener("click", () => apriModaleAssegnazioneMultipla());
    document.getElementById("btnAnnullaMultiplo").addEventListener("click", chiudiModaleAssegnazioneMultipla);
    document.getElementById("btnConfermaMultiplo").addEventListener("click", confermaAssegnazioneMultipla);
}

function apriPaginaStampa() {
    const dataRiferimento = calendar.getDate();
    const mese = dataRiferimento.getMonth() + 1;
    const anno = dataRiferimento.getFullYear();
    window.open(`/stampa?mese=${mese}&anno=${anno}`, "_blank");
}

function apriModaleCreazione(dataStr) {
    document.getElementById("modaleTurnoTitolo").textContent = "Assegna turno";
    document.getElementById("turnoId").value = "";
    document.getElementById("turnoData").value = dataStr;
    document.getElementById("turnoServizio").value = "REPERIBILITA";
    document.getElementById("turnoTipo").disabled = false;
    document.getElementById("btnEliminaTurno").style.display = "none";
    turnoIdSecondario = null;
    turnoDataOriginale = null;
    turnoServizioOriginale = null;
    document.getElementById("modaleTurno").classList.add("aperto");
    aggiornaRigaGiornata(true);
}

function apriModaleModifica(evento) {
    document.getElementById("turnoId").value = evento.id;
    document.getElementById("turnoData").value = formatoData(evento.start);
    document.getElementById("turnoServizio").value = evento.extendedProps.servizio;
    document.getElementById("turnoTipo").value = evento.extendedProps.tipoTurnoId;
    document.getElementById("turnoOperatore").value = evento.extendedProps.codiceOperatore;
    document.getElementById("btnEliminaTurno").style.display = "inline-flex";
    turnoIdSecondario = evento.extendedProps.idSecondario ?? null;
    turnoDataOriginale = formatoData(evento.start);
    turnoServizioOriginale = evento.extendedProps.servizio;
    document.getElementById("turnoTipo").disabled = !!turnoIdSecondario;
    document.getElementById("modaleTurnoTitolo").textContent = turnoIdSecondario ? "Modifica turno festivo (giornata intera)" : "Modifica turno";
    document.getElementById("modaleTurno").classList.add("aperto");
    aggiornaRigaGiornata(false);
}

function chiudiModaleTurno() {
    document.getElementById("modaleTurno").classList.remove("aperto");
}

/**
 * Mostra/nasconde il checkbox "applica anche all'altro turno dello stesso giorno": visibile solo
 * quando il tipo turno scelto e' festivo (diurno o notturno), il solo caso in cui i turni diurno+
 * notturno festivo dello stesso giorno vengono trattati come un'unica riga (vedi TurnoMapper.toEventi
 * lato server). Se si sta gia' modificando un evento "giornata festiva" unificato, il checkbox si
 * nasconde e i campi tipo/servizio restano disabilitati perche' si applica sempre a entrambi i turni.
 */
function aggiornaRigaGiornata(modalitaCreazione) {
    const riga = document.getElementById("turnoGiornataRiga");
    const checkbox = document.getElementById("turnoApplicaGiornata");
    if (turnoIdSecondario) {
        riga.style.display = "none";
        checkbox.checked = false;
        return;
    }
    const tipoId = Number(document.getElementById("turnoTipo").value);
    const tipo = tipiTurno.find(t => t.id === tipoId);

    if (tipo && tipo.festivo) {
        riga.style.display = "flex";
        document.getElementById("turnoGiornataEtichetta").textContent =
            tipo.notturno ? "Applica anche al turno diurno dello stesso giorno" : "Applica anche al turno notturno dello stesso giorno";
        checkbox.checked = !!modalitaCreazione;
    } else {
        riga.style.display = "none";
        checkbox.checked = false;
    }
}

/** Trova, fra i tipi turno festivi caricati, quello diurno e quello notturno (vedi DataInitializer). */
function tipiFestiviDiurnoENotturno() {
    return {
        diurno: tipiTurno.find(t => t.festivo && !t.notturno),
        notturno: tipiTurno.find(t => t.festivo && t.notturno)
    };
}

async function salvaTurno(evento) {
    evento.preventDefault();
    try {
        const dataStr = document.getElementById("turnoData").value;
        const applicaGiornata = turnoIdSecondario !== null || document.getElementById("turnoApplicaGiornata").checked;

        if (applicaGiornata) {
            const {diurno, notturno} = tipiFestiviDiurnoENotturno();
            const nuovoServizio = document.getElementById("turnoServizio").value;
            const richiesta = {
                data: dataStr,
                tipoTurnoDiurnoId: diurno.id,
                tipoTurnoNotturnoId: notturno.id,
                codiceOperatore: document.getElementById("turnoOperatore").value,
                servizio: nuovoServizio
            };
            // Prima si crea/aggiorna la nuova coppia (cosi' se la data scelta non e' festiva, o altro
            // non va bene, la validazione blocca qui senza aver perso nulla): solo se la data o il
            // servizio sono cambiati rispetto all'originale, poi si elimina la coppia di partenza,
            // altrimenti resterebbe orfana nel database (la creazione agisce sullo slot data+tipo+
            // servizio corrente, diverso da quello originale se una delle due chiavi e' cambiata).
            await Api.post("/api/turni/giornata", richiesta);
            if (turnoIdSecondario && (dataStr !== turnoDataOriginale || nuovoServizio !== turnoServizioOriginale)) {
                await Api.delete(`/api/turni/giornata?data=${turnoDataOriginale}&tipoTurnoDiurnoId=${diurno.id}&tipoTurnoNotturnoId=${notturno.id}&servizio=${turnoServizioOriginale}`);
            }
        } else {
            const richiesta = {
                data: dataStr,
                tipoTurnoId: Number(document.getElementById("turnoTipo").value),
                codiceOperatore: document.getElementById("turnoOperatore").value,
                servizio: document.getElementById("turnoServizio").value
            };
            await Api.post("/api/turni", richiesta);
        }
        chiudiModaleTurno();
        calendar.refetchEvents();
    } catch (e) {
        mostraErrore("errore", e);
    }
}

async function eliminaTurno() {
    const id = document.getElementById("turnoId").value;
    if (!id) return;
    const conferma = turnoIdSecondario ? "Rimuovere il turno diurno e notturno di questo giorno?" : "Rimuovere questo turno?";
    if (!confirm(conferma)) return;
    try {
        if (turnoIdSecondario) {
            const {diurno, notturno} = tipiFestiviDiurnoENotturno();
            // Elimina sempre la coppia com'era salvata in origine, non gli eventuali data/servizio
            // appena digitati nel form ma non ancora salvati.
            await Api.delete(`/api/turni/giornata?data=${turnoDataOriginale}&tipoTurnoDiurnoId=${diurno.id}&tipoTurnoNotturnoId=${notturno.id}&servizio=${turnoServizioOriginale}`);
        } else {
            await Api.delete(`/api/turni/${id}`);
        }
        chiudiModaleTurno();
        calendar.refetchEvents();
    } catch (e) {
        mostraErrore("errore", e);
    }
}

let ultimaAnteprimaGenerazione = {anno: null, servizio: ""};

function apriModaleGeneraAnno() {
    document.getElementById("generaAnno").value = calendar.getDate().getFullYear() + 1;
    document.getElementById("generaServizio").value = "";
    document.getElementById("generaAnnoRisultato").style.display = "none";
    document.getElementById("generaAnnoTbody").innerHTML = "";
    document.getElementById("btnConfermaGeneraAnno").disabled = true;
    document.getElementById("modaleGeneraAnno").classList.add("aperto");
}

function chiudiModaleGeneraAnno() {
    document.getElementById("modaleGeneraAnno").classList.remove("aperto");
}

async function caricaAnteprimaGenerazione() {
    try {
        const anno = Number(document.getElementById("generaAnno").value);
        const servizio = document.getElementById("generaServizio").value;
        let url = `/api/turni/genera-anno/anteprima?anno=${anno}`;
        if (servizio) url += `&servizio=${servizio}`;
        const proposte = await Api.get(url);

        const daCreare = proposte.filter(p => !p.giaEsistente);
        document.getElementById("generaAnnoRiepilogo").textContent =
            `${daCreare.length} turni da creare, ${proposte.length - daCreare.length} gia' presenti (non verranno modificati).`;

        document.getElementById("generaAnnoTbody").innerHTML = proposte.map(p => `
            <tr>
                <td>${p.data}</td>
                <td>${ETICHETTA_SERVIZIO[p.servizio] ?? p.servizio}</td>
                <td>${p.nomeTipoTurno}</td>
                <td>${p.nomeOperatore}</td>
                <td>${p.giaEsistente ? '<span class="badge grigio">Gia\' presente</span>' : '<span class="badge verde">Nuovo</span>'}</td>
            </tr>`).join("");

        document.getElementById("generaAnnoRisultato").style.display = "block";
        document.getElementById("btnConfermaGeneraAnno").disabled = daCreare.length === 0;
        ultimaAnteprimaGenerazione = {anno, servizio};
    } catch (e) {
        mostraErrore("errore", e);
    }
}

async function confermaGenerazione() {
    try {
        const {anno, servizio} = ultimaAnteprimaGenerazione;
        let url = `/api/turni/genera-anno?anno=${anno}`;
        if (servizio) url += `&servizio=${servizio}`;
        const risultato = await Api.post(url, {});
        chiudiModaleGeneraAnno();
        calendar.refetchEvents();
        alert(`Generati ${risultato.creati} turni per il ${risultato.anno} (${risultato.saltati} gia' presenti, non modificati).`);
    } catch (e) {
        mostraErrore("errore", e);
    }
}

function apriModaleAssegnazioneMultipla(dalStr, alStr) {
    document.getElementById("multiploDal").value = dalStr ?? formatoData(calendar.getDate());
    document.getElementById("multiploAl").value = alStr ?? dalStr ?? formatoData(calendar.getDate());
    document.getElementById("multiploServizio").value = "REPERIBILITA";
    document.getElementById("multiploSovrascrivi").checked = false;
    document.getElementById("multiploRisultato").style.display = "none";
    document.getElementById("multiploTbody").innerHTML = "";
    document.getElementById("btnAnnullaMultiplo").textContent = "Annulla";
    document.getElementById("modaleAssegnazioneMultipla").classList.add("aperto");
}

function chiudiModaleAssegnazioneMultipla() {
    document.getElementById("modaleAssegnazioneMultipla").classList.remove("aperto");
    calendar.unselect();
}

async function confermaAssegnazioneMultipla() {
    try {
        const richiesta = {
            dal: document.getElementById("multiploDal").value,
            al: document.getElementById("multiploAl").value,
            tipoTurnoId: Number(document.getElementById("multiploTipo").value),
            codiceOperatore: document.getElementById("multiploOperatore").value,
            servizio: document.getElementById("multiploServizio").value,
            sovrascrivi: document.getElementById("multiploSovrascrivi").checked
        };
        const esiti = await Api.post("/api/turni/batch", richiesta);

        const creati = esiti.filter(e => e.stato === "CREATO" || e.stato === "AGGIORNATO").length;
        const saltati = esiti.filter(e => e.stato === "SALTATO").length;
        const errori = esiti.filter(e => e.stato === "ERRORE").length;
        document.getElementById("multiploRiepilogo").textContent =
            `${creati} turni assegnati, ${saltati} saltati (gia' assegnati), ${errori} non validi.`;
        document.getElementById("multiploTbody").innerHTML = esiti.map(e => `
            <tr>
                <td>${e.data}</td>
                <td>${ETICHETTA_STATO_BATCH[e.stato] ?? e.stato}</td>
                <td class="testo-tenue">${e.messaggio ?? ""}</td>
            </tr>`).join("");
        document.getElementById("multiploRisultato").style.display = "block";
        document.getElementById("btnAnnullaMultiplo").textContent = "Chiudi";

        calendar.refetchEvents();
    } catch (e) {
        mostraErrore("errore", e);
    }
}

function esporta(formato) {
    const vista = calendar.view;
    const dal = formatoData(vista.activeStart);
    const al = formatoData(vista.activeEnd);
    const servizio = document.getElementById("filtroServizio").value;
    let url = `/api/turni/export.${formato}?dal=${dal}&al=${al}`;
    if (servizio) url += `&servizio=${servizio}`;
    window.location.href = url;
}
