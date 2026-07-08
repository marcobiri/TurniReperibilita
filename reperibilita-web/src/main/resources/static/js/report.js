const NOME_SERVIZIO = {REPERIBILITA: "Reperibilita generale", FONIA: "Reperibilita fonia"};

document.addEventListener("DOMContentLoaded", async () => {
    impostaDateDefault();
    await Promise.all([caricaOperatoriSelect(), caricaTariffa(), caricaAbbinamenti()]);
    document.getElementById("btnCalcolaReport").addEventListener("click", caricaReport);
    document.getElementById("formTariffa").addEventListener("submit", salvaTariffa);
    document.getElementById("btnAggiungiAbbinamento").addEventListener("click", aggiungiAbbinamento);
    caricaReport();
});

function impostaDateDefault() {
    const oggi = new Date();
    const primoGiorno = new Date(oggi.getFullYear(), oggi.getMonth(), 1);
    const ultimoGiorno = new Date(oggi.getFullYear(), oggi.getMonth() + 1, 0);
    document.getElementById("repDal").value = formatoData(primoGiorno);
    document.getElementById("repAl").value = formatoData(ultimoGiorno);
}

async function caricaOperatoriSelect() {
    const operatori = await Api.get("/api/operatori");
    const opzioni = operatori.map(o => `<option value="${o.codice}">${o.nomeCompleto}</option>`).join("");
    document.getElementById("repOperatore").innerHTML = '<option value="">Tutti</option>' + opzioni;
    document.getElementById("abbPrimo").innerHTML = opzioni;
    document.getElementById("abbSecondo").innerHTML = opzioni;
}

async function caricaReport() {
    try {
        const dal = document.getElementById("repDal").value;
        const al = document.getElementById("repAl").value;
        const operatore = document.getElementById("repOperatore").value;

        let urlRighe = `/api/compensi?dal=${dal}&al=${al}`;
        if (operatore) urlRighe += `&operatore=${operatore}`;

        const [righe, totali] = await Promise.all([
            Api.get(urlRighe),
            Api.get(`/api/compensi/totali?dal=${dal}&al=${al}`)
        ]);

        document.getElementById("corpoTabellaCompenso").innerHTML = righe.map(r => `
            <tr>
                <td>${new Date(r.data + "T00:00:00").toLocaleDateString("it-IT")}</td>
                <td>${r.tipoTurno}</td>
                <td>${r.operatoreNome}</td>
                <td>${NOME_SERVIZIO[r.servizio] ?? r.servizio}</td>
                <td>&euro; ${Number(r.importo).toFixed(2)}</td>
            </tr>`).join("");

        document.getElementById("totaliOperatori").innerHTML = Object.entries(totali).map(([codice, importo]) => `
            <div class="totale-card">
                <div class="testo-tenue">${codice}</div>
                <div class="valore">&euro; ${Number(importo).toFixed(2)}</div>
            </div>`).join("") || '<span class="testo-tenue">Nessun turno nel periodo selezionato.</span>';
    } catch (e) {
        mostraErrore("errore", e);
    }
}

async function caricaTariffa() {
    try {
        const tariffa = await Api.get("/api/tariffa");
        document.getElementById("tariffaImporto").value = tariffa.importoOrario;
        document.getElementById("tariffaFestiva").value = tariffa.percentualeMaggiorazioneFestiva;
        document.getElementById("tariffaNotturna").value = tariffa.percentualeMaggiorazioneNotturna;
    } catch (e) {
        mostraErrore("errore", e);
    }
}

async function salvaTariffa(evento) {
    evento.preventDefault();
    try {
        const richiesta = {
            importoOrario: Number(document.getElementById("tariffaImporto").value),
            percentualeMaggiorazioneFestiva: Number(document.getElementById("tariffaFestiva").value),
            percentualeMaggiorazioneNotturna: Number(document.getElementById("tariffaNotturna").value)
        };
        await Api.put("/api/tariffa", richiesta);
        caricaReport();
    } catch (e) {
        mostraErrore("errore", e);
    }
}

async function caricaAbbinamenti() {
    try {
        const abbinamenti = await Api.get("/api/abbinamenti");
        document.getElementById("corpoTabellaAbbinamenti").innerHTML = abbinamenti.map(a => `
            <tr>
                <td>${a.primoOperatoreNome}</td>
                <td>${a.secondoOperatoreNome}</td>
                <td><button class="btn pericolo piccolo" onclick="eliminaAbbinamento(${a.id})">Elimina</button></td>
            </tr>`).join("");
    } catch (e) {
        mostraErrore("errore", e);
    }
}

async function aggiungiAbbinamento() {
    try {
        const richiesta = {
            primoOperatoreCodice: document.getElementById("abbPrimo").value,
            secondoOperatoreCodice: document.getElementById("abbSecondo").value
        };
        await Api.post("/api/abbinamenti", richiesta);
        caricaAbbinamenti();
    } catch (e) {
        mostraErrore("errore", e);
    }
}

async function eliminaAbbinamento(id) {
    if (!confirm("Rimuovere questo abbinamento?")) return;
    try {
        await Api.delete(`/api/abbinamenti/${id}`);
        caricaAbbinamenti();
    } catch (e) {
        mostraErrore("errore", e);
    }
}
