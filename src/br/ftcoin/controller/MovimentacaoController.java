package br.ftcoin.controller;

import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.dao.MovimentacaoDAO;
import br.ftcoin.dao.OraculoDAO;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.util.I18n;
import br.ftcoin.view.ConsoleUtils;
import br.ftcoin.view.MenuView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Controlador responsável por gerenciar as operações financeiras de {@link Movimentacao}
 * (Compra e Venda de moedas virtuais) e a consulta de cotações junto ao {@link OraculoDAO}.
 * Garante a integridade referencial das carteiras e a validação de saldo em vendas.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class MovimentacaoController {

    private final MovimentacaoDAO movimentacaoDAO;
    private final CarteiraDAO carteiraDAO;
    private final OraculoDAO oraculoDAO;
    private MenuView menuView;
    private ConsoleUtils consoleUtils;

    /**
     * Constrói o controlador injetando os DAOs necessários.
     * Ideal para execução de regras de negócio e testes unitários sem interface CLI.
     *
     * @param carteiraDAO     DAO para validação da existência de carteiras.
     * @param movimentacaoDAO DAO para persistência das movimentações.
     * @param oraculoDAO      DAO para consulta da cotação diária da moeda.
     * @throws NullPointerException se qualquer um dos DAOs for nulo.
     */
    public MovimentacaoController(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO, OraculoDAO oraculoDAO) {
        this.carteiraDAO = Objects.requireNonNull(carteiraDAO, "CarteiraDAO não pode ser nulo.");
        this.movimentacaoDAO = Objects.requireNonNull(movimentacaoDAO, "MovimentacaoDAO não pode ser nulo.");
        this.oraculoDAO = Objects.requireNonNull(oraculoDAO, "OraculoDAO não pode ser nulo.");
    }

    /**
     * Constrói o controlador completo para integração com a interface CLI via {@link AppController}.
     *
     * @param menuView        View genérica de menus da aplicação.
     * @param carteiraDAO     DAO de carteiras.
     * @param movimentacaoDAO DAO de movimentações.
     * @param oraculoDAO      DAO do oráculo de cotações.
     * @throws NullPointerException se {@code menuView} ou algum DAO for nulo.
     */
    public MovimentacaoController(MenuView menuView, CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO, OraculoDAO oraculoDAO) {
        this(carteiraDAO, movimentacaoDAO, oraculoDAO);
        this.menuView = Objects.requireNonNull(menuView, "MenuView não pode ser nula.");
        this.consoleUtils = menuView.getConsoleUtils();
    }

    /**
     * Inicia o loop de navegação do submenu de Movimentações na CLI
     * (Comprar, Vender e Cotar moeda virtual).
     *
     * @throws IllegalStateException se o controlador tiver sido instanciado sem uma {@link MenuView}.
     */
    public void exibirMenuMovimentacao() {
        if (this.menuView == null) {
            throw new IllegalStateException("MenuView não foi inicializada neste controlador.");
        }

        List<String> opcoes = List.of(
            "transactionMenu.buyCoin",   // 1. Comprar moeda virtual
            "transactionMenu.sellCoin",  // 2. Vender moeda virtual
            "transactionMenu.quoteCoin"  // 3. Cotar moeda virtual
        );

        while (true) {
            int escolha = menuView.exibirEObterOpcao("transactionMenu.title", opcoes);
            if (escolha == 0) {
                return;
            }

            switch (escolha) {
                case 1 -> processarOperacaoCLI(TipoOperacao.COMPRA);
                case 2 -> processarOperacaoCLI(TipoOperacao.VENDA);
                case 3 -> exibirCotacaoAtualCLI();
            }
        }
    }

    /**
     * Valida as regras de negócio e registra uma nova movimentação (Compra ou Venda).
     *
     * @param idCarteira Identificador da carteira titular da operação.
     * @param data       Data da movimentação.
     * @param tipo       Tipo da operação ({@link TipoOperacao#COMPRA} ou {@link TipoOperacao#VENDA}).
     * @param quantidade Quantidade de moedas negociadas (deve ser estritamente positiva).
     * @return A entidade {@link Movimentacao} registrada.
     * @throws ValidacaoException se a carteira não existir, se o saldo for insuficiente na venda ou se os dados forem inválidos.
     */
    public Movimentacao cadastrar(int idCarteira, LocalDate data, TipoOperacao tipo, BigDecimal quantidade) {
        if (carteiraDAO.consultar(idCarteira).isEmpty()) {
            throw new ValidacaoException("transaction.wallet.notFound", String.valueOf(idCarteira));
        }

        // Instancia primeiro para acionar as validações estruturais do Model (data nula, qtd <= 0, etc.)
        Movimentacao novaMovimentacao = new Movimentacao(idCarteira, data, tipo, quantidade);

        if (tipo == TipoOperacao.VENDA) {
            validarSaldoParaVenda(idCarteira, quantidade);
        }

        movimentacaoDAO.incluir(novaMovimentacao);
        return novaMovimentacao;
    }

    /**
     * Consulta a cotação da moeda virtual para a data especificada junto ao Oráculo.
     *
     * @param data Data de referência para consulta.
     * @return A {@link Cotacao} correspondente à data.
     * @throws ValidacaoException se não houver cotação disponível para a data informada.
     */
    public Cotacao consultarCotacao(LocalDate data) {
        Objects.requireNonNull(data, "A data para consulta de cotação não pode ser nula.");
        return oraculoDAO.consultar(data)
                .orElseThrow(() -> new ValidacaoException("report.oracle.error.notFound", data.toString()));
    }

    /**
     * Lista todas as movimentações registradas no sistema.
     *
     * @return Lista completa de movimentações.
     */
    public List<Movimentacao> listarTodas() {
        return movimentacaoDAO.consultarTodos();
    }

    /**
     * Lista o histórico de movimentações filtrado por uma carteira específica.
     *
     * @param idCarteira Identificador da carteira.
     * @return Lista de movimentações pertencentes à carteira.
     * @throws ValidacaoException se o identificador da carteira for inválido.
     */
    public List<Movimentacao> listarPorCarteira(int idCarteira) {
        if (idCarteira <= 0) {
            throw new ValidacaoException("wallet.error.invalidId", String.valueOf(idCarteira));
        }
        return movimentacaoDAO.consultarPorCarteira(idCarteira);
    }

    private void validarSaldoParaVenda(int idCarteira, BigDecimal quantidadeVenda) {
        BigDecimal saldoAtual = calcularSaldoMoedas(idCarteira);
        if (saldoAtual.compareTo(quantidadeVenda) < 0) {
            throw new ValidacaoException("transaction.sell.error.insufficientBalance", saldoAtual.toPlainString());
        }
    }

    private BigDecimal calcularSaldoMoedas(int idCarteira) {
        return movimentacaoDAO.consultarPorCarteira(idCarteira).stream()
                .map(m -> m.isCompra() ? m.getQuantidade() : m.getQuantidade().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void processarOperacaoCLI(TipoOperacao tipo) {
        try {
            int idCarteira = consoleUtils.lerInteiro(I18n.get("wallet.view.prompt.id"));
            String qtdTexto = consoleUtils.lerString(I18n.get("transaction.view.prompt.quantity"));
            BigDecimal quantidade = new BigDecimal(qtdTexto.replace(",", "."));

            cadastrar(idCarteira, LocalDate.now(), tipo, quantidade);

            String chaveSucesso = (tipo == TipoOperacao.COMPRA) 
                    ? "transaction.buy.success" 
                    : "transaction.sell.success";
            consoleUtils.exibirSucesso(I18n.get(chaveSucesso));
        } catch (NumberFormatException e) {
            consoleUtils.exibirErro(I18n.get("generic.confirmInvalid"));
        } catch (ValidacaoException e) {
            consoleUtils.exibirErro(e.getMessage());
        }
    }

    private void exibirCotacaoAtualCLI() {
        try {
            Cotacao cotacao = consultarCotacao(LocalDate.now());
            System.out.println(ConsoleUtils.NEGRITO + I18n.get("report.oracle.display.title") + ConsoleUtils.RESET);
            System.out.println(I18n.get("report.oracle.display.date") + " " + cotacao.getData());
            System.out.println(I18n.get("report.oracle.display.price") + " R$ " + cotacao.getValorCotacao());
            consoleUtils.pausar();
        } catch (ValidacaoException e) {
            consoleUtils.exibirAlerta(e.getMessage());
            consoleUtils.pausar();
        }
    }
}