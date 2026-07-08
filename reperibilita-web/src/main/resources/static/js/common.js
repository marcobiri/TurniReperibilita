/** Helper condivisi per chiamare le API REST da tutte le pagine. */
const Api = {
    async get(url) {
        const risposta = await fetch(url);
        return Api._gestisci(risposta);
    },
    async post(url, corpo) {
        const risposta = await fetch(url, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(corpo)
        });
        return Api._gestisci(risposta);
    },
    async put(url, corpo) {
        const risposta = await fetch(url, {
            method: "PUT",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(corpo)
        });
        return Api._gestisci(risposta);
    },
    async delete(url) {
        const risposta = await fetch(url, {method: "DELETE"});
        return Api._gestisci(risposta);
    },
    async _gestisci(risposta) {
        if (risposta.status === 204) {
            return null;
        }
        const testo = await risposta.text();
        const dati = testo ? JSON.parse(testo) : null;
        if (!risposta.ok) {
            const messaggio = (dati && dati.messaggio) ? dati.messaggio : "Si e' verificato un errore";
            throw new Error(messaggio);
        }
        return dati;
    }
};

function mostraErrore(elementoId, errore) {
    const el = document.getElementById(elementoId);
    if (!el) return;
    el.textContent = errore.message || String(errore);
    el.classList.add("visibile");
    setTimeout(() => el.classList.remove("visibile"), 6000);
}

function formatoData(date) {
    const anno = date.getFullYear();
    const mese = String(date.getMonth() + 1).padStart(2, "0");
    const giorno = String(date.getDate()).padStart(2, "0");
    return `${anno}-${mese}-${giorno}`;
}
