package br.ftcoin.controller;

import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.dao.DAOFactory;
import br.ftcoin.dao.MovimentacaoDAO;
import br.ftcoin.enums.Idioma;
import br.ftcoin.enums.MenuOperacao;
import br.ftcoin.service.OraculoCotacao;
import br.ftcoin.service.OraculoService;
import br.ftcoin.util.DatabaseConnection;
import br.ftcoin.util.I18n;
import br.ftcoin.view.ConsoleUtils;
import br.ftcoin.view.MenuView;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Controlador principal (Orquestrador) do sistema FT_Coin.
 * Gerencia o ciclo de vida da aplicação, o loop do menu principal,
 * a inicialização dos sub-controladores e o encerramento dos recursos.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class AppController {

    /** View responsável por exibir menus e capturar a opção escolhida. */
    private final MenuView menuView;

    /** Utilitário central de entrada/saída e formatação da interface de linha de comando. */
    private final ConsoleUtils consoleUtils;

    /** Modo de persistência ativo, em maiúsculas ({@code "MEMORIA"} ou {@code "MARIADB"}). */
    private final String tipoPersistencia;

    /** Sub-controlador das operações de carteira. */
    private final CarteiraController carteiraController;

    /** Sub-controlador das operações de movimentação (compra, venda e cotação). */
    private final MovimentacaoController movimentacaoController;

    /** Sub-controlador dos relatórios. */
    private final RelatorioController relatorioController;

    /** Sub-controlador da ajuda e créditos. */
    private final AjudaController ajudaController;

    /** Indica se o loop do menu principal deve continuar em execução. */
    private boolean executando;

    /**
     * Inicializa a interface CLI, a fábrica de DAOs conforme o modo de persistência
     * e os controladores especializados das funcionalidades do sistema.
     *
     * @param tipoPersistencia modo de persistência informado na CLI;
     *                         {@code "MARIADB"} ativa a persistência no banco e
     *                         qualquer outro valor seleciona a persistência em memória
     */
    public AppController(String tipoPersistencia) {
        this.tipoPersistencia = (tipoPersistencia != null) ? tipoPersistencia.toUpperCase() : "MEMORIA";

        Scanner scanner = new Scanner(System.in);
        this.consoleUtils = new ConsoleUtils(scanner);
        this.menuView = new MenuView(consoleUtils);

        // Obtém a fábrica concreta via Abstract Factory
        DAOFactory daoFactory = DAOFactory.getFactory(this.tipoPersistencia);
        OraculoCotacao oraculo = new OraculoService();

        // Cada DAO é criado uma única vez e compartilhado entre os controllers:
        // na persistência em memória, cada chamada à fábrica cria um repositório novo,
        // e controllers com repositórios diferentes não enxergariam os mesmos dados.
        CarteiraDAO carteiraDAO = daoFactory.getCarteiraDAO();
        MovimentacaoDAO movimentacaoDAO = daoFactory.getMovimentacaoDAO();

        // Injeta os DAOs, o serviço de cotações e as Views nos controladores de domínio
        this.carteiraController = new CarteiraController(menuView, carteiraDAO);
        this.movimentacaoController = new MovimentacaoController(
                menuView,
                carteiraDAO,
                movimentacaoDAO,
                oraculo
        );
        this.relatorioController = new RelatorioController(
                menuView, 
                daoFactory.getCarteiraDAO(), 
                daoFactory.getMovimentacaoDAO(), 
                oraculo
        );
        this.ajudaController = new AjudaController(consoleUtils);

        this.executando = true;
    }

    /**
     * Inicia a execução contínua da interface de linha de comando (CLI)
     * até o usuário escolher a opção de saída, momento em que encerra os recursos.
     */
    public void iniciar() {
        while (executando) {
            exibirEProcessarMenuPrincipal();
        }

        encerrarRecursos();
    }

    /**
     * Renderiza as opções do menu principal e direciona a escolha do usuário
     * ao sub-controlador correspondente.
     *
     * @throws br.ftcoin.exception.ValidacaoException se a opção escolhida não existir
     */
    private void exibirEProcessarMenuPrincipal() {
        int escolha = menuView.exibirEObterOpcao(
                "mainMenu.title",
                MenuOperacao.obterChavesMenuPrincipal()
        );

        MenuOperacao operacao = MenuOperacao.fromCodigo(escolha);

        switch (operacao) {
            case CARTEIRA     -> carteiraController.exibirMenuCarteira();
            case MOVIMENTACAO -> movimentacaoController.exibirMenuMovimentacao();
            case RELATORIOS   -> relatorioController.exibirMenuRelatorios();
            case AJUDA        -> ajudaController.exibirMenuAjuda();
            case IDIOMA       -> alterarIdioma();
            case SAIR         -> this.executando = false;
        }
    }

    /**
     * Exibe o submenu de seleção de idiomas e atualiza dinamicamente
     * o idioma utilizado pelo {@link I18n}.
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