package br.ftcoin.view;

import br.ftcoin.controller.RelatorioController;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Carteira;
import br.ftcoin.service.OraculoException;
import br.ftcoin.util.I18n;

import java.util.List;
import java.util.Objects;

public class RelatorioView {

    private static final List<String> OPCOES = List.of(
            "reportMenu.sortedWalletsById",
            "reportMenu.sortedWalletsByHolder"
            // as outras opções entram conforme as tasks forem saindo
    );

    private final RelatorioController controller;
    private final ConsoleUtils console;
    private final MenuView menu;

    public RelatorioView(ConsoleUtils console, RelatorioController controller) {
        this.console = Objects.requireNonNull(console);
        this.controller = Objects.requireNonNull(controller);
        this.menu = new MenuView(this.console);
    }

    public void exibirMenu() {
        int opcao;
        do {
            opcao = menu.exibirEObterOpcao("reportMenu.title", OPCOES);
            try {
                switch (opcao) {
                    case 1 -> exibirCarteiras("report.wallets.byId.title", controller.listarCarteirasPorId());
                    case 2 -> exibirCarteiras("report.wallets.byHolder.title", controller.listarCarteirasPorTitular());
                    default -> { }
                }
            } catch (ValidacaoException | OraculoException e) {
                console.exibirErro(e.getMessage());
            }
            if (opcao != 0) console.pausar();
        } while (opcao != 0);
    }

    private void exibirCarteiras(String chaveTitulo, List<Carteira> carteiras) {
        if (carteiras.isEmpty()) {
            console.exibirErro(I18n.get("report.wallets.empty"));
            return;
        }
        System.out.println(ConsoleUtils.NEGRITO + I18n.get(chaveTitulo) + ConsoleUtils.RESET);
        System.out.printf("%-6s %-30s %-20s%n",
                I18n.get("report.wallet.table.id"),
                I18n.get("report.wallet.table.holder"),
                I18n.get("report.wallet.table.broker"));
        for (Carteira c : carteiras) {
            System.out.printf("%-6d %-30s %-20s%n", c.getId(), c.getTitular(), c.getCorretora());
        }
    }
}
