const GIORNI = ["Domenica", "Lunedi", "Martedi", "Mercoledi", "Giovedi", "Venerdi", "Sabato"];
const BADGE_TIPO = {FISSA: "blu", MOBILE: "viola", PERSONALIZZATA: "verde"};
const ETICHETTA_TIPO = {FISSA: "Nazionale fissa", MOBILE: "Nazionale mobile (Pasqua)", PERSONALIZZATA: "Personalizzata"};

document.addEventListener("DOMContentLoaded", () => {
    popolaSelettoreAnno();
    caricaFestivita();
    document.getElementById("selettoreAnno").addEventListener("change", caricaFestivita);
    document.getElementById("btnNuovaFestivita").addEventListener("click", apriModale);
    document.getElementById("btnAnnullaFestivita").addEventListener("click", chiudiModale);
    document.getElementById("formFestivita").addEventListener("submit", salvaFestivita);
});

function popolaSelettoreAnno() {
    const annoCorrente = new Date().getFullYear();
    const select = document.getElementById("selettoreAnno");
    for (let anno = annoCorrente - 2; anno <= annoCorrente + 3; anno++) {
        const opzione = document.createElement("option");
        opzione.value = anno;
        opzione.textContent = anno;
        if (anno === annoCorrente) opzione.selected = true;
        select.appendChild(opzione);
    }
}

async function caricaFestivita() {
    try {
        const anno = document.getElementById("selettoreAnno").value;
        const festivita = await Api.get(`/api/festivita?anno=${anno}`);
        const corpo = document.getElementById("corpoTabellaFestivita");
        corpo.innerHTML = festivita.map(f => {
            const data = new Date(f.data + "T00:00:00");
            const puoEliminare = f.tipo === "PERSONALIZZATA";
            return `
            <tr>
                <td>${data.toLocaleDateString("it-IT")}</td>
                <td>${GIORNI[data.getDay()]}</td>
                <td>${f.descrizione}</td>
                <td><span class="badge ${BADGE_TIPO[f.tipo]}">${ETICHETTA_TIPO[f.tipo]}</span></td>
                <td>${puoEliminare ? `<button class="btn pericolo piccolo" onclick="eliminaFestivita('${f.data}')">Elimina</button>` : ""}</td>
            </tr>`;
        }).join("");
    } catch (e) {
        mostraErrore("errore", e);
    }
}

function apriModale() {
    document.getElementById("formFestivita").reset();
    document.getElementById("modaleFestivita").classList.add("aperto");
}

function chiudiModale() {
    document.getElementById("modaleFestivita").classList.remove("aperto");
}

async function salvaFestivita(evento) {
    evento.preventDefault();
    try {
        const richiesta = {
            data: document.getElementById("festivitaData").value,
            descrizione: document.getElementById("festivitaDescrizione").value
        };
        await Api.post("/api/festivita", richiesta);
        chiudiModale();
        caricaFestivita();
    } catch (e) {
        mostraErrore("errore", e);
    }
}

async function eliminaFestivita(data) {
    if (!confirm("Rimuovere questa festivita?")) return;
    try {
        await Api.delete(`/api/festivita/${data}`);
        caricaFestivita();
    } catch (e) {
        mostraErrore("errore", e);
    }
}
