package it.reperibilita.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controller delle pagine Thymeleaf. Le pagine sono "gusci" pressoche' vuoti:
 * tutti i dati sono caricati lato client via fetch() verso le API REST in
 * it.reperibilita.web.api, cosi' da avere un'interazione fluida (aggiungi/modifica/
 * cancella senza ricaricare la pagina) senza bisogno di un framework SPA separato.
 */
@Controller
public class PageController {

    // Timestamp di avvio, usato come query param di cache-busting sui link a /css e /js (vedi
    // assetVersion() sotto): cambia ad ogni riavvio del server, cosi' il browser scarica sempre
    // l'ultima versione di style.css/*.js dopo un aggiornamento, senza dover fare un refresh forzato.
    private final long avvio = System.currentTimeMillis();

    @Value("${report.logo}")
    private String reportLogo;

    @Value("${report.organizzazione}")
    private String reportOrganizzazione;

    @Value("${report.servizio}")
    private String reportServizio;

    @ModelAttribute("assetVersion")
    public long assetVersion() {
        return avvio;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/calendario";
    }

    @GetMapping("/calendario")
    public String calendario(Model model) {
        model.addAttribute("pageTitle", "Calendario turni");
        model.addAttribute("activePage", "calendario");
        return "calendario";
    }

    @GetMapping("/operatori")
    public String operatori(Model model) {
        model.addAttribute("pageTitle", "Operatori");
        model.addAttribute("activePage", "operatori");
        return "operatori";
    }

    @GetMapping("/festivita")
    public String festivita(Model model) {
        model.addAttribute("pageTitle", "Festivita");
        model.addAttribute("activePage", "festivita");
        return "festivita";
    }

    @GetMapping("/report")
    public String report(Model model) {
        model.addAttribute("pageTitle", "Report compensi");
        model.addAttribute("activePage", "report");
        return "report";
    }

    @GetMapping("/stampa")
    public String stampa(Model model) {
        model.addAttribute("pageTitle", "Stampa turni");
        model.addAttribute("reportLogo", reportLogo);
        model.addAttribute("reportOrganizzazione", reportOrganizzazione);
        model.addAttribute("reportServizio", reportServizio);
        return "stampa";
    }
}
