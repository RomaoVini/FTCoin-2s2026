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
 * (compra e venda de moedas virtuais) e pela consulta de cotações no {@link OraculoCotacao}.
 * Valida a existência da carteira e o saldo disponível antes de registrar uma venda.
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

    /** View genérica de menus; pode ser nula quando o controlador é usado sem a CLI. */
    private MenuView menuView;

    /**
     * Constrói o controlador injetando os DAOs e o serviço de cotações necessários.
     * Pode ser usado independentemente da interface CLI.
     *
     * @param carteiraDAO DAO para validação da existência de carteiras
     * @param movimentacaoDAO DAO para persistência das movimentações
     * @param oraculo serviço para consulta da cotação diária da moeda
     * @throws NullPointerException se qualquer dependência for nula
     */
    public MovimentacaoController(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO, OraculoCotacao oraculo) {
        this.carteiraDAO = Objects.requireNonNull(carteiraDAO, () -> I18n.get("generic.error.nullParameter"));
        this.movimentacaoDAO = Objects.requireNonNull(movimentacaoDAO, () -> I18n.get("generic.error.nullParameter"));
        this.oraculo = Objects.requireNonNull(oraculo, () -> I18n.get("generic.error.nullParameter"));
    }

    /**
     * Constrói o controlador completo para integração com a interface CLI via {@link AppController}.
     *
     * @param menuView view genérica de menus da aplicação
     * @param carteiraDAO DAO de carteiras
     * @param movimentacaoDAO DAO de movimentações
     * @param oraculo serviço do oráculo de cotações
     * @throws NullPointerException se qualquer dependência for nula
     */
    public MovimentacaoController(MenuView menuView, CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO, OraculoCotacao oraculo) {
        this(carteiraDAO, movimentacaoDAO, oraculo);
        this.menuView = Objects.requireNonNull(menuView, () -> I18n.get("generic.error.nullParameter"));
    }

    /**
     * Solicita à view que exiba o submenu de movimentações na CLI,
     * com opções para comprar, vender ou consultar a cotação da moeda virtual.
     *
     * @throws IllegalStateException se o controlador tiver sido criado sem uma {@link MenuView}
     */
    public void exibirMenuMovimentacao() {
        if (this.menuView == null) {
            throw new IllegalStateException(I18n.get("generic.error.nullParameter"));
        }
        new MovimentacaoView(menuView.getConsoleUtils(), this).exibirMenu();
    }

    /**
     * Valida as regras de negócio e registra uma nova movimentação de compra ou venda.
     *
     * @param idCarteira identificador da carteira titular da operação
     * @param data data da movimentação
     * @param tipo tipo da operação ({@link TipoOperacao#COMPRA} ou
     *             {@link TipoOperacao#VENDA})
     * @param quantidade quantidade de moedas negociadas (deve ser estritamente positiva)
     * @return entidade {@link Movimentacao} registrada
     * @throws ValidacaoException se a carteira não existir, o saldo de uma venda for
     *                            insuficiente ou os dados forem inválidos
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
     * @param data data de referência para consulta
     * @return {@link Cotacao} correspondente à data
     * @throws NullPointerException se {@code data} for nula
     * @throws OraculoException se não houver cotação ou a consulta ao oráculo falhar
     */
    public Cotacao consultarCotacao(LocalDate data) {
        Objects.requireNonNull(data, () -> I18n.get("transaction.error.dateRequired"));
        return oraculo.obterCotacao(data);
    }

    /**
     * Lista todas as movimentações registradas no sistema.
     *
     * @return lista completa das movimentações cadastradas
     */
    public List<Movimentacao> listarTodas() {
        return movimentacaoDAO.consultarTodos();
    }

    /**
     * Lista o histórico de movimentações filtrado por uma carteira específica.
     *
     * @param idCarteira identificador da carteira
     * @return lista das movimentações pertencentes à carteira
     * @throws ValidacaoException se o identificador da carteira for menor ou igual a zero
     */
    public List<Movimentacao> listarPorCarteira(int idCarteira) {
        if (idCarteira <= 0) {
            throw new ValidacaoException("wallet.error.invalidId", String.valueOf(idCarteira));
        }
        return movimentacaoDAO.consultarPorCarteira(idCarteira);
    }

    /**
     * Verifica se a nova movimentação mantém o saldo da carteira não negativo em todas as datas.
     * Reconstrói o histórico cronológico com a movimentação candidata e rejeita a operação
     * caso o saldo fique negativo em qualquer ponto.
     *
     * @param idCarteira identificador da carteira
     * @param nova movimentação candidata, ainda não persistida
     * @throws ValidacaoException se a operação deixar o saldo negativo em alguma data
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
                LOG.log(Level.WARNING,
                        "Venda recusada por saldo insuficiente: carteira={0}, data={1}, quantidade={2}",
                        new Object[] { idCarteira, nova.getDataOperacao(), nova.getQuantidade() });
                throw new ValidacaoException("transaction.sell.insufficientBalance");
            }
        }
    }
}