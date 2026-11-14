package br.ftcoin.controller;

import br.ftcoin.dao.DAOFactory;
import br.ftcoin.enums.Idioma;
import br.ftcoin.util.DatabaseConnection;
import br.ftcoin.util.I18n;
import br.ftcoin.view.ConsoleUtils;
import br.ftcoin.view.MenuView;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Controlador principal (Orquestrador) do sistema FT_Coin.
 * Gerencia o ciclo de vida da aplicação, o loop do Menu Principal,
 * a injeção de dependências nos sub-controllers e o encerramento seguro de recursos.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class AppController {

    private final MenuView menuView;
    private final ConsoleUtils consoleUtils;
    private final String tipoPersistencia;

    private final CarteiraController carteiraController;
    private final MovimentacaoController movimentacaoController;
    private final RelatorioController relatorioController;
    private final AjudaController ajudaController;

    private boolean executando;

    /**
     * Inicializa a aplicação configurando a interface CLI, a fábrica de DAOs
     * conforme o modo de persistência e injetando as dependências nos sub-controllers.
     *
     * @param tipoPersistencia Tipo de armazenamento ("MEMORIA" ou "MARIADB") informado na CLI.
     */
    public AppController(String tipoPersistencia) {
        this.tipoPersistencia = (tipoPersistencia != null) ? tipoPersistencia.toUpperCase() : "MEMORIA";
        
        Scanner scanner = new Scanner(System.in);
        this.consoleUtils = new ConsoleUtils(scanner);
        this.menuView = new MenuView(consoleUtils);

        // Obtém a fábrica concreta via Abstract Factory
        DAOFactory daoFactory = DAOFactory.getFactory(this.tipoPersistencia);

        // Injeta os DAOs e Views nos respectivos controladores de domínio
        this.carteiraController = new CarteiraController(menuView, daoFactory.getCarteiraDAO());
        this.movimentacaoController = new MovimentacaoController(
                menuView, 
                daoFactory.getCarteiraDAO(), 
                daoFactory.getMovimentacaoDAO(), 
                daoFactory.getOraculoDAO()
        );
        this.relatorioController = new RelatorioController(
                menuView, 
                daoFactory.getCarteiraDAO(), 
                daoFactory.getMovimentacaoDAO(), 
                daoFactory.getOraculoDAO()
        );
        this.ajudaController = new AjudaController(consoleUtils);

        this.executando = true;
    }

    /**
     * Inicia a execução contínua da interface de linha de comando (CLI)
     * e garante o encerramento limpo das conexões ao finalizar o loop.
     */
    public void iniciar() {
        while (executando) {
            exibirEProcessarMenuPrincipal();
        }

        encerrarRecursos();
    }

    /**
     * Renderiza as opções do Menu Principal e direciona o fluxo
     * para o sub-controller correspondente à escolha do usuário.
     */
    private void exibirEProcessarMenuPrincipal() {
        List<String> opcoes = List.of(
            "mainMenu.wallet",       // 1. Carteira
            "mainMenu.transaction",  // 2. Movimentação
            "mainMenu.reports",      // 3. Relatórios
            "mainMenu.help",         // 4. Ajuda
            "mainMenu.language"      // 5. Idioma
        );

        int escolha = menuView.exibirEObterOpcao("mainMenu.title", opcoes);

        // Guard Clause para saída imediata (Opção 0: Sair)
        if (escolha == 0) {
            this.executando = false;
            return;
        }

        switch (escolha) {
            case 1 -> carteiraController.exibirMenuCarteira();
            case 2 -> movimentacaoController.exibirMenuMovimentacao();
            case 3 -> relatorioController.exibirMenuRelatorios();
            case 4 -> ajudaController.exibirMenuAjuda();
            case 5 -> alterarIdioma();
            default -> consoleUtils.exibirErro(I18n.get("generic.confirmInvalid"));
        }
    }

    /**
     * Exibe o submenu de seleção de idiomas e atualiza dinamicamente
     * o {@link java.util.ResourceBundle} ativo do sistema.
     */
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
        consoleUtils.exibirSucesso(I18n.get("language.change.success"));
    }

    /**
     * Fecha a conexão ativa com o banco de dados MariaDB (caso esteja em uso)
     * e exibe a mensagem final de despedida no console.
     */
    private void encerrarRecursos() {
        if ("MARIADB".equals(this.tipoPersistencia)) {
            try {
                DatabaseConnection.getInstancia().fechar();
            } catch (SQLException ignored) {
                // Caso a conexão nem tenha sido aberta durante a sessão, ignora silenciosamente
            }
        }
        consoleUtils.exibirSucesso(I18n.get("mainMenu.exit"));
    }
}