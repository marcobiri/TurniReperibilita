package it.reperibilita.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller delle pagine Thymeleaf. Le pagine sono "gusci" pressoche' vuoti:
 * tutti i dati sono caricati lato client via fetch() verso le API REST in
 * it.reperibilita.web.api, cosi' da avere un'interazione fluida (aggiungi/modifica/
 * cancella senza ricaricare la pagina) senza bisogno di un framework SPA separato.
 */
@Controller
public class PageController {

    @Value("${report.logo}")
    private String reportLogo;

    @Value("${report.organizzazione}")
    private String reportOrganizzazione;

    @Value("${report.servizio}")
    private String reportServizio;

    @Value("${report.firma-riga1}")
    private String reportFirmaRiga1;

    @Value("${report.firma-riga2}")
    private String reportFirmaRiga2;

    @Value("${report.firma-riga3}")
    private String reportFirmaRiga3;

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
        model.addAttribute("reportFirmaRiga1", reportFirmaRiga1);
        model.addAttribute("reportFirmaRiga2", reportFirmaRiga2);
        model.addAttribute("reportFirmaRiga3", reportFirmaRiga3);
        return "stampa";
    }
}
