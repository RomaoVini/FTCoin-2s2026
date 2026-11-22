package test;

import br.ftcoin.dao.CarteiraDAOMemoria;
import br.ftcoin.dao.MovimentacaoDAOMemoria;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Carteira;
import br.ftcoin.model.CarteiraFinanceira;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.service.OraculoCotacao;
import br.ftcoin.service.OraculoException;
import br.ftcoin.controller.MovimentacaoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes do {@link MovimentacaoController}: registro de compras e vendas, regra de saldo
 * (inclusive ao longo do tempo), consulta de cotação e listagem por carteira.
 */
class MovimentacaoControllerTest {

    private static final int CARTEIRA = 1;

    private MovimentacaoDAOMemoria movimentacoes;
    private MovimentacaoController controller;

    @BeforeEach
    void setUp() {
        CarteiraDAOMemoria carteiras = new CarteiraDAOMemoria();
        carteiras.incluir(new Carteira(CARTEIRA, "Ana Lima", "Corretora X"));
        carteiras.incluir(new Carteira(2, "Bruno", "Corretora Y"));
        movimentacoes = new MovimentacaoDAOMemoria();
        controller = new MovimentacaoController(carteiras, movimentacoes, oraculoSimulado());
    }

    // ---------- cadastro ----------

    @Test
    void compraEhRegistradaEGanhaIdentificador() {
        Movimentacao m = comprar("2026-02-01", "1000");

        assertTrue(m.getIdMovimento() > 0);
        assertTrue(m.isCompra());
        assertEquals(1, movimentacoes.consultarPorCarteira(CARTEIRA).size());
    }

    @Test
    void movimentacaoParaCarteiraInexistenteLancaExcecaoDeDominio() {
        assertThrows(ValidacaoException.class,
                () -> controller.cadastrar(99, data("2026-02-01"), TipoOperacao.COMPRA, new BigDecimal("10")));
        assertTrue(movimentacoes.consultarTodos().isEmpty());
    }

    @Test
    void quantidadeInvalidaEhRejeitadaPelaMovimentacao() {
        assertThrows(ValidacaoException.class,
                () -> controller.cadastrar(CARTEIRA, data("2026-02-01"), TipoOperacao.COMPRA, BigDecimal.ZERO));
    }

    // ---------- regra de saldo na venda ----------

    @Test
    void vendaSemNenhumSaldoEhRecusadaENadaEhGravado() { // item 11 do roteiro: venda de 500 em 01/01/2026
        assertThrows(ValidacaoException.class, () -> vender("2026-01-01", "500"));

        assertTrue(movimentacoes.consultarPorCarteira(CARTEIRA).isEmpty());
    }

    @Test
    void vendaComSaldoSuficienteEhAceita() {
        comprar("2026-02-01", "1000");

        Movimentacao venda = vender("2026-02-01", "500"); // no mesmo dia da compra

        assertTrue(venda.isVenda());
        assertEquals(2, movimentacoes.consultarPorCarteira(CARTEIRA).size());
    }

    @Test
    void vendaMaiorQueOSaldoEhRecusada() {
        comprar("2026-02-01", "1000");

        assertThrows(ValidacaoException.class, () -> vender("2026-02-02", "1001"));
    }

    @Test
    void vendasSucessivasNaoPodemSomarMaisQueOSaldo() {
        comprar("2026-02-01", "1000");
        vender("2026-02-01", "500");
        vender("2026-02-02", "500");

        assertThrows(ValidacaoException.class, () -> vender("2026-02-03", "1"));
        assertEquals(3, movimentacoes.consultarPorCarteira(CARTEIRA).size());
    }

    @Test
    void vendaDatadaAntesDaCompraEhRecusadaMesmoQueOSaldoTotalSejaSuficiente() {
        comprar("2026-02-10", "1000");

        assertThrows(ValidacaoException.class, () -> vender("2026-02-01", "500"));
    }

    @Test
    void compraRetroativaLiberaVendaPosteriorDentroDoNovoSaldo() {
        comprar("2026-02-10", "1000");
        comprar("2026-02-01", "300"); // compra com data anterior

        Movimentacao venda = vender("2026-02-05", "300");

        assertTrue(venda.isVenda());
    }

    @Test
    void saldoDeUmaCarteiraNaoLiberaVendaEmOutra() {
        comprar("2026-02-01", "1000");

        assertThrows(ValidacaoException.class,
                () -> controller.cadastrar(2, data("2026-02-02"), TipoOperacao.VENDA, new BigDecimal("10")));
    }

    @Test
    void planilhaDoProfessorComAsDezoitoOperacoesEhTodaAceitaESaldoFinalEh540() {
        int[][] ops = { // {dia de setembro/2026, tipo (1 = compra, 2 = venda), quantidade}
                {1, 1, 1000}, {4, 1, 120}, {5, 2, 500}, {6, 1, 500}, {8, 2, 80}, {9, 2, 500},
                {10, 2, 500}, {11, 1, 1000}, {13, 2, 500}, {15, 2, 500}, {16, 1, 1000}, {18, 2, 500},
                {20, 2, 500}, {21, 1, 1000}, {23, 2, 500}, {25, 2, 500}, {26, 1, 1000}, {28, 2, 500}};

        for (int[] op : ops) {
            controller.cadastrar(CARTEIRA, LocalDate.of(2026, 9, op[0]),
                    op[1] == 1 ? TipoOperacao.COMPRA : TipoOperacao.VENDA, new BigDecimal(op[2]));
        }

        List<Movimentacao> todas = movimentacoes.consultarPorCarteira(CARTEIRA);
        assertEquals(18, todas.size());
        assertEquals(0, new BigDecimal("540").compareTo(CarteiraFinanceira.calcularSaldoQuantidade(todas)));
    }

    // ---------- cotação ----------

    @Test
    void consultaDeCotacaoDevolveAQuotacaoDoOraculoParaAData() {
        Cotacao cotacao = controller.consultarCotacao(data("2026-03-05"));

        assertEquals(data("2026-03-05"), cotacao.getData());
        assertEquals(0, new BigDecimal("105").compareTo(cotacao.getValor()));
    }

    @Test
    void dataSemCotacaoPropagaOErroDoOraculo() {
        assertThrows(OraculoException.class, () -> controller.consultarCotacao(data("2000-06-01")));
        assertThrows(OraculoException.class, () -> controller.consultarCotacao(data("2050-11-15")));
    }

    @Test
    void consultaDeCotacaoComDataNulaEhRecusada() {
        assertThrows(NullPointerException.class, () -> controller.consultarCotacao(null));
    }

    // ---------- listagens ----------

    @Test
    void listagemPorCarteiraTrazSomenteAsMovimentacoesDaquelaCarteira() {
        comprar("2026-02-01", "1000");
        controller.cadastrar(2, data("2026-02-01"), TipoOperacao.COMPRA, new BigDecimal("5"));

        assertEquals(1, controller.listarPorCarteira(CARTEIRA).size());
        assertEquals(1, controller.listarPorCarteira(2).size());
        assertEquals(2, controller.listarTodas().size());
    }

    @Test
    void listagemPorCarteiraComIdentificadorInvalidoLancaExcecao() {
        assertThrows(ValidacaoException.class, () -> controller.listarPorCarteira(0));
    }

    // ---------- apoio ----------

    /** Oráculo simulado: preço = 100 + dia do mês, e sem cotação antes de 2025 ou depois de hoje. */
    private static OraculoCotacao oraculoSimulado() {
        return d -> {
            if (d.isBefore(LocalDate.of(2025, 1, 1)) || d.isAfter(LocalDate.now())) {
                throw new OraculoException("Não há cotação disponível para " + d + ".");
            }
            return new Cotacao(d, BigDecimal.valueOf(100 + d.getDayOfMonth()));
        };
    }

    private Movimentacao comprar(String data, String quantidade) {
        return controller.cadastrar(CARTEIRA, data(data), TipoOperacao.COMPRA, new BigDecimal(quantidade));
    }

    private Movimentacao vender(String data, String quantidade) {
        return controller.cadastrar(CARTEIRA, data(data), TipoOperacao.VENDA, new BigDecimal(quantidade));
    }

    private static LocalDate data(String texto) {
        return LocalDate.parse(texto);
    }
}