const MESI = ["Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio",
    "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"];
const GIORNI_CENTRALINO = ["domenica", "lunedi", "martedi", "mercoledi", "giovedi", "venerdi", "sabato"];

document.addEventListener("DOMContentLoaded", () => {
    popolaSelettori();
    caricaElenco();
    document.getElementById("btnAggiorna").addEventListener("click", () => {
        aggiornaUrl();
        caricaElenco();
    });
    document.getElementById("btnStampa").addEventListener("click", () => window.print());
});

function popolaSelettori() {
    const parametri = new URLSearchParams(window.location.search);
    const oggi = new Date();
    const meseCorrente = Number(parametri.get("mese")) || (oggi.getMonth() + 1);
    const annoCorrente = Number(parametri.get("anno")) || oggi.getFullYear();

    const selMese = document.getElementById("pMese");
    selMese.innerHTML = MESI.map((nome, indice) => `<option value="${indice + 1}">${nome}</option>`).join("");
    selMese.value = meseCorrente;

    const selAnno = document.getElementById("pAnno");
    let opzioniAnno = "";
    for (let anno = oggi.getFullYear() - 2; anno <= oggi.getFullYear() + 2; anno++) {
        opzioniAnno += `<option value="${anno}">${anno}</option>`;
    }
    selAnno.innerHTML = opzioniAnno;
    selAnno.value = annoCorrente;
}

function aggiornaUrl() {
    const mese = document.getElementById("pMese").value;
    const anno = document.getElementById("pAnno").value;
    history.replaceState(null, "", `/stampa?mese=${mese}&anno=${anno}`);
}

async function caricaElenco() {
    const mese = Number(document.getElementById("pMese").value);
    const anno = Number(document.getElementById("pAnno").value);

    document.getElementById("oggettoReport").textContent =
        `Oggetto: Reperibilita mese di ${MESI[mese - 1].toUpperCase()} ${anno}`;

    const dal = `${anno}-${String(mese).padStart(2, "0")}-01`;
    const ultimoGiorno = new Date(anno, mese, 0).getDate();
    const al = `${anno}-${String(mese).padStart(2, "0")}-${String(ultimoGiorno).padStart(2, "0")}`;

    try {
        const turni = await Api.get(`/api/turni?dal=${dal}&al=${al}`);
        renderizza(turni);
    } catch (e) {
        document.getElementById("corpoTabellaCentralino").innerHTML =
            `<tr><td colspan="6">Errore nel caricamento: ${e.message}</td></tr>`;
    }
}

function raggruppaPerSlot(turni) {
    const gruppi = new Map();
    for (const t of turni) {
        const chiave = t.data + "|" + t.tipoTurno.id;
        if (!gruppi.has(chiave)) {
            gruppi.set(chiave, {data: t.data, tipoTurno: t.tipoTurno, informatica: null, telefonia: null});
        }
        const gruppo = gruppi.get(chiave);
        if (t.servizio === "REPERIBILITA") {
            gruppo.informatica = t.operatore;
        } else if (t.servizio === "FONIA") {
            gruppo.telefonia = t.operatore;
        }
    }
    return Array.from(gruppi.values()).sort((a, b) => {
        if (a.data !== b.data) return a.data < b.data ? -1 : 1;
        return a.tipoTurno.oraInizio.localeCompare(b.tipoTurno.oraInizio);
    });
}

function formattaOperatore(operatore) {
    if (!operatore) return "";
    return operatore.nome.charAt(0).toUpperCase() + " " + operatore.cognome;
}

function formattaOra(orario) {
    return orario.substring(0, 5).replace(":", ".");
}

function renderizza(turni) {
    const righe = raggruppaPerSlot(turni);
    let html = "";
    let sabatoSegnato = null;

    for (const riga of righe) {
        const data = new Date(riga.data + "T00:00:00");

        if (data.getDay() === 6 && sabatoSegnato !== riga.data) {
            html += '<tr class="riga-spaziatrice"><td colspan="6">&nbsp;</td></tr>';
            sabatoSegnato = riga.data;
        }

        const classeRiga = riga.tipoTurno.festivo ? ' class="riga-festiva"' : '';
        html += `<tr${classeRiga}>
            <td>${data.toLocaleDateString("it-IT")} ${GIORNI_CENTRALINO[data.getDay()]}</td>
            <td>${riga.tipoTurno.nome.toLowerCase()}: ${formattaOra(riga.tipoTurno.oraInizio)} - ${formattaOra(riga.tipoTurno.oraFine)}</td>
            <td>${formattaOperatore(riga.informatica)}</td>
            <td>${formattaOperatore(riga.telefonia)}</td>
            <td>${riga.informatica?.telefonoAziendale ?? ""}</td>
            <td>${riga.telefonia?.telefonoAziendale ?? ""}</td>
        </tr>`;
    }

    document.getElementById("corpoTabellaCentralino").innerHTML = html ||
        '<tr><td colspan="6">Nessun turno nel periodo selezionato.</td></tr>';
}
