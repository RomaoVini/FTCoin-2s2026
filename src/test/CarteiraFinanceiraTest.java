package test;

import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.CarteiraFinanceira;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes das regras de cálculo da {@link CarteiraFinanceira}: custo médio, resultado
 * acumulado, saldo financeiro e valoração a mercado.
 */
class CarteiraFinanceiraTest {

    private static final LocalDate DIA = LocalDate.of(2026, 2, 1);

    private CarteiraFinanceira carteira;

    @BeforeEach
    void setUp() {
        carteira = new CarteiraFinanceira();
    }

    @Test
    void carteiraNovaComecaZerada() {
        assertIgual("0", carteira.getSaldoQuantidade());
        assertIgual("0", carteira.getCustoMedio());
        assertIgual("0", carteira.getSaldoFinanceiro());
        assertIgual("0", carteira.getResultadoAcumulado());
    }

    @Test
    void primeiraCompraDefineSaldoCustoMedioESaldoFinanceiro() {
        registrar(TipoOperacao.COMPRA, "1000", "10");

        assertIgual("1000", carteira.getSaldoQuantidade());
        assertIgual("10", carteira.getCustoMedio());
        assertIgual("10000", carteira.getSaldoFinanceiro());
    }

    @Test
    void novaCompraRecalculaOCustoMedioPonderado() {
        registrar(TipoOperacao.COMPRA, "1000", "10");
        registrar(TipoOperacao.COMPRA, "1000", "20");

        assertIgual("2000", carteira.getSaldoQuantidade());
        assertIgual("15", carteira.getCustoMedio());
        assertIgual("30000", carteira.getSaldoFinanceiro());
    }

    @Test
    void vendaGeraResultadoSobreOCustoMedioEMantemOCustoMedio() {
        registrar(TipoOperacao.COMPRA, "1000", "10");

        BigDecimal resultado = registrar(TipoOperacao.VENDA, "400", "15");

        assertIgual("2000", resultado);               // 400 x (15 - 10)
        assertIgual("600", carteira.getSaldoQuantidade());
        assertIgual("10", carteira.getCustoMedio());
        assertIgual("6000", carteira.getSaldoFinanceiro());
        assertIgual("2000", carteira.getResultadoAcumulado());
    }

    @Test
    void vendaComPrejuizoGeraResultadoNegativo() {
        registrar(TipoOperacao.COMPRA, "100", "10");

        assertIgual("-300", registrar(TipoOperacao.VENDA, "100", "7"));
        assertIgual("-300", carteira.getResultadoAcumulado());
    }

    @Test
    void resultadoAcumuladoSomaAsVendas() {
        registrar(TipoOperacao.COMPRA, "1000", "10");
        registrar(TipoOperacao.VENDA, "100", "12"); // +200
        registrar(TipoOperacao.VENDA, "100", "9");  // -100

        assertIgual("100", carteira.getResultadoAcumulado());
    }

    @Test
    void vendaAcimaDoSaldoLancaExcecaoDeDominio() {
        registrar(TipoOperacao.COMPRA, "100", "10");

        assertThrows(ValidacaoException.class, () -> registrar(TipoOperacao.VENDA, "101", "10"));
    }

    @Test
    void valoracaoAMercadoUsaACotacaoAtualSobreOSaldo() {
        registrar(TipoOperacao.COMPRA, "1000", "10");
        registrar(TipoOperacao.VENDA, "400", "15");

        carteira.atualizarCotacaoAtual(new Cotacao(DIA, new BigDecimal("20")));

        assertIgual("12000", carteira.getSaldoMercado());       // 600 x 20
        // vendas (6000) + saldo a mercado (12000) - compras (10000)
        assertIgual("8000", carteira.getResultadoMercado());
    }

    @Test
    void resultadoAMercadoSemCotacaoAtualLancaExcecao() {
        registrar(TipoOperacao.COMPRA, "10", "10");

        assertThrows(IllegalStateException.class, () -> carteira.getResultadoMercado());
    }

    @Test
    void saldoDeQuantidadeSomaComprasESubtraiVendas() {
        List<Movimentacao> lista = new ArrayList<>();
        lista.add(new Movimentacao(1, DIA, TipoOperacao.COMPRA, new BigDecimal("1000")));
        lista.add(new Movimentacao(1, DIA.plusDays(1), TipoOperacao.VENDA, new BigDecimal("300")));

        assertIgual("700", CarteiraFinanceira.calcularSaldoQuantidade(lista));
    }

    @Test
    void saldoDeQuantidadeDeListaVaziaEhZero() {
        assertIgual("0", CarteiraFinanceira.calcularSaldoQuantidade(new ArrayList<>()));
    }

    @Test
    void planilhaDoProfessorComPrecoIgualACemMaisODiaApuraOsValoresEsperados() {
        int[][] ops = { // {dia de setembro/2026, tipo (1 = compra, 2 = venda), quantidade}
                {1, 1, 1000}, {4, 1, 120}, {5, 2, 500}, {6, 1, 500}, {8, 2, 80}, {9, 2, 500},
                {10, 2, 500}, {11, 1, 1000}, {13, 2, 500}, {15, 2, 500}, {16, 1, 1000}, {18, 2, 500},
                {20, 2, 500}, {21, 1, 1000}, {23, 2, 500}, {25, 2, 500}, {26, 1, 1000}, {28, 2, 500}};

        for (int[] op : ops) {
            LocalDate dia = LocalDate.of(2026, 9, op[0]);
            Movimentacao m = new Movimentacao(1, dia,
                    op[1] == 1 ? TipoOperacao.COMPRA : TipoOperacao.VENDA, new BigDecimal(op[2]));
            carteira.registrarMovimentacao(m, new Cotacao(dia, BigDecimal.valueOf(100 + op[0])));
        }
        carteira.atualizarCotacaoAtual(new Cotacao(LocalDate.of(2026, 10, 1), new BigDecimal("130")));

        // valores conferidos à parte, por um cálculo independente de custo médio móvel
        // (o custo médio é guardado com 20 casas; comparamos em reais, com 2 casas)
        assertEmReais("540", carteira.getSaldoQuantidade());
        assertEmReais("125.80", carteira.getCustoMedio());
        assertEmReais("67932.00", carteira.getSaldoFinanceiro());
        assertEmReais("19092.00", carteira.getResultadoAcumulado());
        assertEmReais("70200.00", carteira.getSaldoMercado());
        assertEmReais("21360.00", carteira.getResultadoMercado());
    }

    private BigDecimal registrar(TipoOperacao tipo, String quantidade, String preco) {
        Movimentacao m = new Movimentacao(1, DIA, tipo, new BigDecimal(quantidade));
        return carteira.registrarMovimentacao(m, new Cotacao(DIA, new BigDecimal(preco)));
    }

    private static void assertEmReais(String esperado, BigDecimal obtido) {
        BigDecimal arredondado = obtido.setScale(2, java.math.RoundingMode.HALF_EVEN);
        assertEquals(0, new BigDecimal(esperado).compareTo(arredondado),
                "esperado " + esperado + " mas foi " + arredondado);
    }

    private static void assertIgual(String esperado, BigDecimal obtido) {
        assertEquals(0, new BigDecimal(esperado).compareTo(obtido),
                "esperado " + esperado + " mas foi " + obtido);
    }
}