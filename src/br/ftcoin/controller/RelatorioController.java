package br.ftcoin.controller;

import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.dao.MovimentacaoDAO;
import br.ftcoin.model.Carteira;
import br.ftcoin.service.OraculoCotacao;
import br.ftcoin.view.MenuView;
import br.ftcoin.view.RelatorioView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RelatorioController {

    private static final Comparator<Carteira> POR_ID =
            Comparator.comparingInt(Carteira::getId);

    private static final Comparator<Carteira> POR_TITULAR =
            Comparator.comparing(Carteira::getTitular, criarCollator())
                      .thenComparingInt(Carteira::getId);

    private final CarteiraDAO carteiraDAO;
    private final MovimentacaoDAO movimentacaoDAO;
    private final OraculoCotacao oraculo;
    private final RelatorioView view;

    public RelatorioController(MenuView menuView, CarteiraDAO carteiraDAO,
                               MovimentacaoDAO movimentacaoDAO, OraculoCotacao oraculo) {
        Objects.requireNonNull(menuView, "MenuView não pode ser nula.");
        this.carteiraDAO = Objects.requireNonNull(carteiraDAO, "CarteiraDAO não pode ser nulo.");
        this.movimentacaoDAO = Objects.requireNonNull(movimentacaoDAO, "MovimentacaoDAO não pode ser nulo.");
        this.oraculo = Objects.requireNonNull(oraculo, "Oráculo não pode ser nulo.");
        this.view = new RelatorioView(menuView.getConsoleUtils(), this);
    }

    public void exibirMenuRelatorios() {
        view.exibirMenu();
    }

    public List<Carteira> listarCarteirasPorId() {
        return ordenar(POR_ID);
    }

    public List<Carteira> listarCarteirasPorTitular() {
        return ordenar(POR_TITULAR);
    }

    private List<Carteira> ordenar(Comparator<Carteira> criterio) {
        List<Carteira> lista = new ArrayList<>(carteiraDAO.consultarTodos());
        lista.sort(criterio);
        return lista;
    }

    private static Collator criarCollator() {
        Collator c = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
        c.setStrength(Collator.PRIMARY);
        return c;
    }
}
