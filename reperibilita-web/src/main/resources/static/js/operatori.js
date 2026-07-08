document.addEventListener("DOMContentLoaded", () => {
    caricaOperatori();
    document.getElementById("btnNuovoOperatore").addEventListener("click", () => apriModale(null));
    document.getElementById("btnAnnullaOperatore").addEventListener("click", chiudiModale);
    document.getElementById("formOperatore").addEventListener("submit", salvaOperatore);
});

async function caricaOperatori() {
    try {
        const operatori = await Api.get("/api/operatori");
        const corpo = document.getElementById("corpoTabellaOperatori");
        corpo.innerHTML = operatori.map(o => `
            <tr>
                <td><strong>${o.codice}</strong></td>
                <td>${o.cognome}</td>
                <td>${o.nome}</td>
                <td>${o.telefonoAziendale ?? ""}</td>
                <td>${o.telefonoCasa ?? ""}</td>
                <td>${o.attivo ? '<span class="badge verde">Attivo</span>' : '<span class="badge grigio">Non attivo</span>'}</td>
                <td>
                    <button class="btn secondario piccolo" onclick='apriModale(${JSON.stringify(o)})'>Modifica</button>
                    <button class="btn pericolo piccolo" onclick="eliminaOperatore('${o.codice}')">Elimina</button>
                </td>
            </tr>`).join("");
    } catch (e) {
        mostraErrore("errore", e);
    }
}

function apriModale(operatore) {
    const modifica = operatore !== null;
    document.getElementById("modaleOperatoreTitolo").textContent = modifica ? "Modifica operatore" : "Nuovo operatore";
    document.getElementById("operatoreModifica").value = modifica ? operatore.codice : "";
    document.getElementById("operatoreCodice").value = modifica ? operatore.codice : "";
    document.getElementById("operatoreCodice").disabled = modifica;
    document.getElementById("operatoreCognome").value = modifica ? operatore.cognome : "";
    document.getElementById("operatoreNome").value = modifica ? operatore.nome : "";
    document.getElementById("operatoreTelAz").value = modifica ? (operatore.telefonoAziendale ?? "") : "";
    document.getElementById("operatoreTelCasa").value = modifica ? (operatore.telefonoCasa ?? "") : "";
    document.getElementById("operatoreAttivo").checked = modifica ? operatore.attivo : true;
    document.getElementById("modaleOperatore").classList.add("aperto");
}

function chiudiModale() {
    document.getElementById("modaleOperatore").classList.remove("aperto");
}

async function salvaOperatore(evento) {
    evento.preventDefault();
    const codiceEsistente = document.getElementById("operatoreModifica").value;
    const richiesta = {
        codice: document.getElementById("operatoreCodice").value.toUpperCase(),
        cognome: document.getElementById("operatoreCognome").value,
        nome: document.getElementById("operatoreNome").value,
        telefonoAziendale: document.getElementById("operatoreTelAz").value,
        telefonoCasa: document.getElementById("operatoreTelCasa").value,
        attivo: document.getElementById("operatoreAttivo").checked
    };
    try {
        if (codiceEsistente) {
            await Api.put(`/api/operatori/${codiceEsistente}`, richiesta);
        } else {
            await Api.post("/api/operatori", richiesta);
        }
        chiudiModale();
        caricaOperatori();
    } catch (e) {
        mostraErrore("errore", e);
    }
}

async function eliminaOperatore(codice) {
    if (!confirm(`Eliminare l'operatore ${codice}? Questa azione non puo' essere annullata.`)) return;
    try {
        await Api.delete(`/api/operatori/${codice}`);
        caricaOperatori();
    } catch (e) {
        mostraErrore("errore", e);
    }
}
