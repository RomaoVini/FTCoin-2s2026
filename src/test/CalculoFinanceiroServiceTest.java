package test;

import br.ftcoin.service.CalculoFinanceiroService;
import br.ftcoin.controller.MovimentacaoController;
import br.ftcoin.dao.CarteiraDAOMemoria;
import br.ftcoin.dao.MovimentacaoDAOMemoria;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Carteira;
import br.ftcoin.model.CarteiraFinanceira;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculoFinanceiroServiceTest {
    private static final int ID_CARTEIRA = 1;
    private static final LocalDate DATA_INICIAL = LocalDate.of(2026, 1, 1);
    private static final LocalDate DATA_VENDA = LocalDate.of(2026, 1, 2);
    private static final LocalDate DATA_FINAL = LocalDate.of(2026, 1, 3);

    @Test
    void carteiraSemMovimentacoesTemMetricasZeradas() {
        CalculoFinanceiroService service = serviceComCotacoes(Map.of(
                DATA_FINAL, new BigDecimal("10.00")));

        CarteiraFinanceira carteira = service.calcularCarteira(List.of(), DATA_FINAL);

        assertDecimalEquals("0", carteira.getSaldoQuantidade());
        assertDecimalEquals("0", carteira.getSaldoFinanceiro());
        assertDecimalEquals("0", carteira.getSaldoMercado());
        assertDecimalEquals("0", carteira.getResultadoAcumulado());
        assertDecimalEquals("0", carteira.getResultadoMercado());
    }

    @Test
    void compraCalculaCustoMedioConsiderandoSaldoECustoAnteriores() {
        CalculoFinanceiroService service = serviceComCotacoes(Map.of(
                DATA_INICIAL, new BigDecimal("12.29"),
                DATA_FINAL, new BigDecimal("13.61")));
        List<Movimentacao> historico = List.of(
                movimentacao(2, DATA_FINAL, TipoOperacao.COMPRA, "1000"),
                movimentacao(1, DATA_INICIAL, TipoOperacao.COMPRA, "500"));

        CarteiraFinanceira carteira = service.calcularCarteira(historico, DATA_FINAL);

        assertDecimalEquals("13.17", carteira.getCustoMedio());
        assertDecimalEquals("19755", carteira.getSaldoFinanceiro());
        assertDecimalEquals("0", carteira.getResultadoOperacional());
    }

    @Test
    void vendaMantemCustoMedioECalculaResultadoRealizado() {
        CalculoFinanceiroService service = serviceComCotacoes(Map.of(
                DATA_INICIAL, new BigDecimal("12.29"),
                DATA_FINAL, new BigDecimal("10.00")));
        List<Movimentacao> historico = List.of(
                movimentacao(1, DATA_INICIAL, TipoOperacao.COMPRA, "500"),
                movimentacao(2, DATA_FINAL, TipoOperacao.VENDA, "100"));

        CarteiraFinanceira carteira = service.calcularCarteira(historico, DATA_FINAL);

        assertDecimalEquals("12.29", carteira.getCustoMedio());
        assertDecimalEquals("-229", carteira.getResultadoOperacional());
        assertDecimalEquals("-229", carteira.getResultadoAcumulado());
    }

    @Test
    void resultadoMercadoCombinaVendasHistoricasSaldoAtualECompras() {
        CalculoFinanceiroService service = serviceComCotacoes(Map.of(
                DATA_INICIAL, new BigDecimal("100"),
                DATA_VENDA, new BigDecimal("120"),
                DATA_FINAL, new BigDecimal("90")));
        List<Movimentacao> historico = List.of(
                movimentacao(1, DATA_INICIAL, TipoOperacao.COMPRA, "10"),
                movimentacao(2, DATA_VENDA, TipoOperacao.VENDA, "2"));

        CarteiraFinanceira carteira = service.calcularCarteira(historico, DATA_FINAL);

        assertDecimalEquals("8", carteira.getSaldoQuantidade());
        assertDecimalEquals("720", carteira.getSaldoMercado());
        assertDecimalEquals("40", carteira.getResultadoAcumulado());
        assertDecimalEquals("-40", carteira.getResultadoMercado());
    }

    @Test
    void vendaQueDeixariaSaldoNegativoEhImpedidaPelaValidacao() {
        CarteiraDAOMemoria carteiras = new CarteiraDAOMemoria();
        carteiras.incluir(new Carteira(ID_CARTEIRA, "Titular", "Corretora"));
        MovimentacaoDAOMemoria movimentacoes = new MovimentacaoDAOMemoria();
        MovimentacaoController controller = new MovimentacaoController(
                carteiras, movimentacoes, data -> new Cotacao(data, BigDecimal.ONE));

        controller.cadastrar(ID_CARTEIRA, DATA_INICIAL, TipoOperacao.COMPRA, new BigDecimal("2"));

        assertThrows(ValidacaoException.class, () -> controller.cadastrar(
                ID_CARTEIRA, DATA_FINAL, TipoOperacao.VENDA, new BigDecimal("3")));
        assertDecimalEquals("2", CarteiraFinanceira.calcularSaldoQuantidade(
                movimentacoes.consultarPorCarteira(ID_CARTEIRA)));
    }

    @Test
    void rejeitaHistoricoCronologicoQueProduzSaldoNegativo() {
        List<Movimentacao> historico = List.of(
                movimentacao(1, DATA_INICIAL, TipoOperacao.VENDA, "1"));

        assertThrows(ValidacaoException.class, () -> CarteiraFinanceira.calcularSaldoQuantidade(historico));
    }

    private CalculoFinanceiroService serviceComCotacoes(Map<LocalDate, BigDecimal> valores) {
        Map<LocalDate, Cotacao> cotacoes = new HashMap<>();
        valores.forEach((data, valor) -> cotacoes.put(data, new Cotacao(data, valor)));
        return new CalculoFinanceiroService(data -> {
            Cotacao cotacao = cotacoes.get(data);
            if (cotacao == null) {
                throw new IllegalArgumentException("Cotação de teste ausente para " + data);
            }
            return cotacao;
        });
    }

    private Movimentacao movimentacao(int id, LocalDate data, TipoOperacao tipo, String quantidade) {
        return new Movimentacao(id, ID_CARTEIRA, data, tipo, new BigDecimal(quantidade));
    }

    private void assertDecimalEquals(String esperado, BigDecimal atual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual));
    }
}
