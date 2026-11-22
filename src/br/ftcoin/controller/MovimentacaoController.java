package br.ftcoin.controller;

import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.dao.MovimentacaoDAO;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.service.OraculoCotacao;
import br.ftcoin.service.OraculoException;
import br.ftcoin.view.ConsoleUtils;
import br.ftcoin.view.MenuView;
import br.ftcoin.view.MovimentacaoView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Controlador responsável por gerenciar as operações financeiras de {@link Movimentacao}
 * (Compra e Venda de moedas virtuais) e a consulta de cotações junto ao {@link OraculoCotacao}.
 * Garante a integridade referencial das carteiras e a validação de saldo em vendas.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class MovimentacaoController {

    private final MovimentacaoDAO movimentacaoDAO;
    private final CarteiraDAO carteiraDAO;
    private final OraculoCotacao oraculo;
    private MenuView menuView;
    private ConsoleUtils consoleUtils;

    /**
     * Constrói o controlador injetando os DAOs necessários.
     * Ideal para execução de regras de negócio e testes unitários sem interface CLI.
     *
     * @param carteiraDAO     DAO para validação da existência de carteiras.
     * @param movimentacaoDAO DAO para persistência das movimentações.
     * @param oraculo         Serviço para consulta da cotação diária da moeda.
     * @throws NullPointerException se qualquer dependência for nula.
     */
    public MovimentacaoController(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO, OraculoCotacao oraculo) {
        this.carteiraDAO = Objects.requireNonNull(carteiraDAO, "CarteiraDAO não pode ser nulo.");
        this.movimentacaoDAO = Objects.requireNonNull(movimentacaoDAO, "MovimentacaoDAO não pode ser nulo.");
        this.oraculo = Objects.requireNonNull(oraculo, "Serviço de cotações não pode ser nulo.");
    }

    /**
     * Constrói o controlador completo para integração com a interface CLI via {@link AppController}.
     *
     * @param menuView        View genérica de menus da aplicação.
     * @param carteiraDAO     DAO de carteiras.
     * @param movimentacaoDAO DAO de movimentações.
     * @param oraculo         Serviço do oráculo de cotações.
     * @throws NullPointerException se qualquer dependência for nula.
     */
    public MovimentacaoController(MenuView menuView, CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO, OraculoCotacao oraculo) {
        this(carteiraDAO, movimentacaoDAO, oraculo);
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
        new MovimentacaoView(consoleUtils, this).exibirMenu();
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
            validarSaldoAoLongoDoTempo(idCarteira, novaMovimentacao);
        }

        movimentacaoDAO.incluir(novaMovimentacao);
        return novaMovimentacao;
    }

    /**
     * Consulta a cotação da moeda virtual para a data especificada junto ao oráculo.
     *
     * @param data Data de referência para consulta.
     * @return A {@link Cotacao} correspondente à data.
     * @throws OraculoException se não houver cotação ou se a consulta ao oráculo falhar.
     */
    public Cotacao consultarCotacao(LocalDate data) {
        Objects.requireNonNull(data, "A data para consulta de cotação não pode ser nula.");
        return oraculo.obterCotacao(data);
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

    /**
     * Garante que o saldo de moedas nunca fique negativo em nenhum momento do histórico,
     * considerando as datas das operações (inclusive a que está sendo cadastrada).
     *
     * @param idCarteira identificador da carteira
     * @param nova       movimentação que será incluída
     * @throws ValidacaoException se alguma venda ficaria sem saldo suficiente na sua data
     */
    private void validarSaldoAoLongoDoTempo(int idCarteira, Movimentacao nova) {
        List<Movimentacao> todas = new ArrayList<>(movimentacaoDAO.consultarPorCarteira(idCarteira));
        todas.sort(Comparator.comparingInt(Movimentacao::getIdMovimento));
        todas.add(nova);
        todas.sort(Comparator.comparing(Movimentacao::getDataOperacao)); // estável: no mesmo dia mantém a ordem de inclusão
        BigDecimal saldo = BigDecimal.ZERO;
        for (Movimentacao m : todas) {
            saldo = m.isCompra() ? saldo.add(m.getQuantidade()) : saldo.subtract(m.getQuantidade());
            if (saldo.signum() < 0) {
                throw new ValidacaoException("transaction.sell.insufficientBalance");
            }
        }
    }

}