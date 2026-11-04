
package br.ftcoin.controller;

import br.ftcoin.dao.DAOFactory;
import br.ftcoin.enums.Idioma;
import br.ftcoin.util.I18n;
import br.ftcoin.view.ConsoleUtils;
import br.ftcoin.view.MenuView;

import java.util.List;
import java.util.Scanner;


 //Controlador principal do sistema FT_Coin.
 //Gerencia o loop principal, navegação entre submódulos e configurações de sessão.
public class AppController {
    private final MenuView menuView;
    private final ConsoleUtils consoleUtils;
    private final DAOFactory daoFactory;

    // Sub-controllers que cuidam da regra de cada menu
    private final CarteiraController carteiraController;
    private final MovimentacaoController movimentacaoController;
    private final RelatorioController relatorioController;
    private final AjudaController ajudaController;

    private boolean executando;

    public AppController(String tipoPersistencia) {
        Scanner scanner = new Scanner(System.in);
        this.consoleUtils = new ConsoleUtils(scanner);
        this.menuView = new MenuView(consoleUtils);

        // 1. Usa o Padrão Factory para obter a família de DAOs (Memória ou MariaDB)
        this.daoFactory = DAOFactory.getFactory(tipoPersistencia);

        // 2. Instancia os Sub-Controllers injetando os DAOs e Views necessários
        this.carteiraController = new CarteiraController(menuView, daoFactory.getCarteiraDAO());
        this.movimentacaoController = new MovimentacaoController(menuView, daoFactory.getCarteiraDAO(), daoFactory.getMovimentacaoDAO(), daoFactory.getOraculoDAO());
        this.relatorioController = new RelatorioController(menuView, daoFactory.getCarteiraDAO(), daoFactory.getMovimentacaoDAO(), daoFactory.getOraculoDAO());
        this.ajudaController = new AjudaController(consoleUtils);

        this.executando = true;
    }

    public void iniciar() {
        while (executando) {
            exibirEProcessarMenuPrincipal();
        }
        
        // Fechamento limpo de recursos ao sair
        consoleUtils.exibirSucesso(I18n.get("mainMenu.exit"));
    }

    private void exibirEProcessarMenuPrincipal() {
        List<String> opcoes = List.of(
            "mainMenu.wallet",       // 1. Carteira
            "mainMenu.transaction",  // 2. Movimentação
            "mainMenu.reports",      // 3. Relatórios
            "mainMenu.help",         // 4. Ajuda
            "mainMenu.language"      // 5. Idioma
        );

        int escolha = menuView.exibirEObterOpcao("mainMenu.title", opcoes);

        // Guard Clause para encerramento (Opção 0: Sair)
        if (escolha == 0) {
            this.executando = false;
            return;
        }

        // Delega a responsabilidade para o sub-controller correto
        switch (escolha) {
            case 1 -> carteiraController.exibirMenuCarteira();
            case 2 -> movimentacaoController.exibirMenuMovimentacao();
            case 3 -> relatorioController.exibirMenuRelatorios();
            case 4 -> ajudaController.exibirMenuAjuda();
            case 5 -> alterarIdioma();
            default -> consoleUtils.exibirErro(I18n.get("generic.confirmInvalid"));
        }
    }

    private void alterarIdioma() {
        List<String> opcoesIdioma = List.of(
            "languageMenu.pt", // 1. Português
            "languageMenu.en", // 2. Inglês
            "languageMenu.es"  // 3. Espanhol
        );

        int escolha = menuView.exibirEObterOpcao("language.title", opcoesIdioma);

        if (escolha == 0) {
            return;
        }

        Idioma novoIdioma = Idioma.porOpcaoMenu(escolha);
        I18n.setIdioma(novoIdioma);
        consoleUtils.exibirSucesso(I18n.get("success.message.database"));
    }
}
