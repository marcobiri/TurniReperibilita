let calendar;
let tipiTurno = [];
let operatori = [];

const ETICHETTA_SERVIZIO = {REPERIBILITA: "Reperibile Informatica", FONIA: "Reperibile fonia"};

document.addEventListener("DOMContentLoaded", async () => {
    await Promise.all([caricaTipiTurno(), caricaOperatori()]);
    inizializzaCalendario();
    collegaEventi();
});

async function caricaTipiTurno() {
    try {
        tipiTurno = await Api.get("/api/tipi-turno");
        const select = document.getElementById("turnoTipo");
        select.innerHTML = tipiTurno.map(t =>
            `<option value="${t.id}">${t.nome} (${t.oraInizio}-${t.oraFine})</option>`).join("");
    } catch (e) {
        mostraErrore("errore", e);
    }
}

async function caricaOperatori() {
    try {
        operatori = await Api.get("/api/operatori?soloAttivi=true");
        const selectModale = document.getElementById("turnoOperatore");
        selectModale.innerHTML = operatori.map(o => `<option value="${o.codice}">${o.nomeCompleto}</option>`).join("");

        const selectFiltro = document.getElementById("filtroOperatore");
        selectFiltro.innerHTML = '<option value="">Tutti</option>' +
            operatori.map(o => `<option value="${o.codice}">${o.nomeCompleto}</option>`).join("");
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
            info.el.title = `${operatore?.nomeCompleto ?? ""} - ${etichetta} - ${tipo?.nome ?? ""}`;
        },
        events: caricaEventi,
        dateClick: (info) => apriModaleCreazione(info.dateStr),
        eventClick: (info) => apriModaleModifica(info.event)
    });
    calendar.render();
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

    document.getElementById("btnEsportaCsv").addEventListener("click", () => esporta("csv"));
    document.getElementById("btnEsportaIcs").addEventListener("click", () => esporta("ics"));
    document.getElementById("btnStampaPdf").addEventListener("click", apriPaginaStampa);
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
    document.getElementById("btnEliminaTurno").style.display = "none";
    document.getElementById("modaleTurno").classList.add("aperto");
}

function apriModaleModifica(evento) {
    document.getElementById("modaleTurnoTitolo").textContent = "Modifica turno";
    document.getElementById("turnoId").value = evento.id;
    document.getElementById("turnoData").value = formatoData(evento.start);
    document.getElementById("turnoServizio").value = evento.extendedProps.servizio;
    document.getElementById("turnoTipo").value = evento.extendedProps.tipoTurnoId;
    document.getElementById("turnoOperatore").value = evento.extendedProps.codiceOperatore;
    document.getElementById("btnEliminaTurno").style.display = "inline-flex";
    document.getElementById("modaleTurno").classList.add("aperto");
}

function chiudiModaleTurno() {
    document.getElementById("modaleTurno").classList.remove("aperto");
}

async function salvaTurno(evento) {
    evento.preventDefault();
    try {
        const richiesta = {
            data: document.getElementById("turnoData").value,
            tipoTurnoId: Number(document.getElementById("turnoTipo").value),
            codiceOperatore: document.getElementById("turnoOperatore").value,
            servizio: document.getElementById("turnoServizio").value
        };
        await Api.post("/api/turni", richiesta);
        chiudiModaleTurno();
        calendar.refetchEvents();
    } catch (e) {
        mostraErrore("errore", e);
    }
}

async function eliminaTurno() {
    const id = document.getElementById("turnoId").value;
    if (!id) return;
    if (!confirm("Rimuovere questo turno?")) return;
    try {
        await Api.delete(`/api/turni/${id}`);
        chiudiModaleTurno();
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
