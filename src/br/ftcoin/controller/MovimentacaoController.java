package br.ftcoin.controller;

import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.dao.MovimentacaoDAO;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.service.OraculoCotacao;
import br.ftcoin.service.OraculoException;
import br.ftcoin.util.I18n;
import br.ftcoin.view.MenuView;
import br.ftcoin.view.MovimentacaoView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador responsável por gerenciar as operações financeiras de {@link Movimentacao}
 * (Compra e Venda de moedas virtuais) e a consulta de cotações junto ao {@link OraculoCotacao}.
 * Garante a integridade referencial das carteiras e a validação de saldo em vendas.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class MovimentacaoController {

    private static final Logger LOG = Logger.getLogger(MovimentacaoController.class.getName());

    /** Repositório de acesso a dados das movimentações. */
    private final MovimentacaoDAO movimentacaoDAO;

    /** Repositório de acesso a dados das carteiras, usado para validar sua existência. */
    private final CarteiraDAO carteiraDAO;

    /** Serviço de consulta de cotações da moeda virtual. */
    private final OraculoCotacao oraculo;

    /** View genérica de menus; permanece {@code null} quando o controlador é usado sem CLI (ex.: testes). */
    private MenuView menuView;

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
        this.carteiraDAO = Objects.requireNonNull(carteiraDAO, () -> I18n.get("generic.error.nullParameter"));
        this.movimentacaoDAO = Objects.requireNonNull(movimentacaoDAO, () -> I18n.get("generic.error.nullParameter"));
        this.oraculo = Objects.requireNonNull(oraculo, () -> I18n.get("generic.error.nullParameter"));
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
        this.menuView = Objects.requireNonNull(menuView, () -> I18n.get("generic.error.nullParameter"));
    }

    /**
     * Inicia o loop de navegação do submenu de Movimentações na CLI
     * (Comprar, Vender e Cotar moeda virtual).
     *
     * @throws IllegalStateException se o controlador tiver sido instanciado sem uma {@link MenuView}.
     */
    public void exibirMenuMovimentacao() {
        if (this.menuView == null) {
            throw new IllegalStateException(I18n.get("generic.error.nullParameter"));
        }
        new MovimentacaoView(menuView.getConsoleUtils(), this).exibirMenu();
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
        LOG.log(Level.INFO, "Movimentação cadastrada: carteira={0}, tipo={1}, quantidade={2}",
                new Object[] { idCarteira, tipo, quantidade });
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
        Objects.requireNonNull(data, () -> I18n.get("transaction.error.dateRequired"));
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
     * Verifica se a nova movimentação mantém o saldo de moedas da carteira não negativo em todas as datas.
     * Reconstrói a linha do tempo (histórico existente + nova movimentação) e acumula o saldo em ordem
     * cronológica; se o saldo ficar negativo em qualquer ponto, a operação é rejeitada.
     *
     * @param idCarteira Identificador da carteira.
     * @param nova       Movimentação candidata, ainda não persistida.
     * @throws ValidacaoException se a operação deixar o saldo negativo em alguma data.
     */
    private void validarSaldoAoLongoDoTempo(int idCarteira, Movimentacao nova) {
        // Cópia mutável: a lista devolvida pelo DAO pode ser imutável (ex.: List.of em testes com mock).
        List<Movimentacao> todas = new ArrayList<>(movimentacaoDAO.consultarPorCarteira(idCarteira));

        // Uniformiza a ordem por id (ordem de inclusão), pois o DAO MariaDB devolve DESC e o de memória por data.
        todas.sort(Comparator.comparingInt(Movimentacao::getIdMovimento));

        // A nova movimentação entra no fim (ainda tem id = 0) para ficar depois das antigas em caso de empate de data.
        todas.add(nova);

        // Ordena por data; o sort é estável, então no mesmo dia mantém a ordem de inclusão
        // (compra e venda no mesmo dia continuam válidas).
        todas.sort(Comparator.comparing(Movimentacao::getDataOperacao));

        // Percorre a linha do tempo acumulando o saldo; saldo negativo em qualquer ponto invalida a venda.
        BigDecimal saldo = BigDecimal.ZERO;
        for (Movimentacao m : todas) {
            saldo = m.isCompra() ? saldo.add(m.getQuantidade()) : saldo.subtract(m.getQuantidade());
            if (saldo.signum() < 0) {
                throw new ValidacaoException("transaction.sell.insufficientBalance");
            }
        }
    }
}