package br.ftcoin.controller;

import br.ftcoin.dao.CarteiraDAOMemoria;
import br.ftcoin.dao.MovimentacaoDAOMemoria;
import br.ftcoin.dto.RelatorioCarteiraDTO;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Carteira;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.service.OraculoCotacao;
import br.ftcoin.service.OraculoException;
import br.ftcoin.view.MenuView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes do {@link RelatorioController}: ordenação das carteiras, saldo atual,
 * histórico de movimentações e ganho/perda, usando DAOs em memória e um
 * Oráculo simulado (sem acesso à rede).
 */
class RelatorioControllerTest {

    private static final int ID_CARTEIRA = 1;
    private static final LocalDate DIA_COMPRA = LocalDate.of(2026, 2, 1);
    private static final LocalDate DIA_VENDA = LocalDate.of(2026, 2, 2);

    private CarteiraDAOMemoria carteiras;
    private MovimentacaoDAOMemoria movimentacoes;
    private Map<LocalDate, BigDecimal> precos;
    private boolean oraculoForaDoAr;
    private boolean oraculoDeveSerConsultado;
    private RelatorioController controller;

    @BeforeEach
    void setUp() {
        carteiras = new CarteiraDAOMemoria();
        movimentacoes = new MovimentacaoDAOMemoria();
        precos = new HashMap<>();
        oraculoForaDoAr = false;
        oraculoDeveSerConsultado = true;
        controller = new RelatorioController(
                new MenuView(new Scanner("")), carteiras, movimentacoes, oraculoSimulado());
    }

    // ---------- carteiras ordenadas ----------

    @Test
    void listaPorIdentificadorEmOrdemNumerica() {
        incluirCarteira(10, "Bruno");
        incluirCarteira(2, "Carla");
        incluirCarteira(1, "Ana");

        List<Carteira> lista = controller.listarCarteirasPorId();

        assertEquals(List.of(1, 2, 10), identificadores(lista));
    }

    @Test
    void listaPorTitularIgnoraAcentoEMaiuscula() {
        incluirCarteira(1, "Zé");
        incluirCarteira(2, "Bruno");
        incluirCarteira(3, "Álvaro");
        incluirCarteira(4, "ana");

        List<Carteira> lista = controller.listarCarteirasPorTitular();

        assertEquals(List.of("Álvaro", "ana", "Bruno", "Zé"), titulares(lista));
    }

    @Test
    void titularesRepetidosSaoDesempatadosPeloIdentificador() {
        incluirCarteira(5, "Ana");
        incluirCarteira(3, "Ana");

        List<Carteira> lista = controller.listarCarteirasPorTitular();

        assertEquals(List.of(3, 5), identificadores(lista));
    }

    @Test
    void listagemSemCarteirasRetornaListaVazia() {
        assertTrue(controller.listarCarteirasPorId().isEmpty());
        assertTrue(controller.listarCarteirasPorTitular().isEmpty());
    }

    // ---------- saldo atual ----------

    @Test
    void saldoDeCarteiraInexistenteLancaExcecaoDeDominio() {
        assertThrows(ValidacaoException.class, () -> controller.gerarSaldoAtual(99));
    }

    @Test
    void carteiraSemMovimentacoesTemSaldoZeroSemConsultarOraculo() {
        incluirCarteira(ID_CARTEIRA, "Ana");
        oraculoDeveSerConsultado = false;

        RelatorioCarteiraDTO saldo = controller.gerarSaldoAtual(ID_CARTEIRA);

        assertEquals("0", saldo.saldoQuantidade());
        assertTrue(saldo.saldoFinanceiro().contains("0,00"));
    }

    @Test
    void saldoConsideraComprasEVendasAoCustoMedio() {
        incluirCarteira(ID_CARTEIRA, "Ana");
        precos.put(DIA_COMPRA, new BigDecimal("10.00"));
        precos.put(DIA_VENDA, new BigDecimal("20.00"));
        incluirMovimentacao(DIA_COMPRA, TipoOperacao.COMPRA, "1000");
        incluirMovimentacao(DIA_VENDA, TipoOperacao.VENDA, "500");

        RelatorioCarteiraDTO saldo = controller.gerarSaldoAtual(ID_CARTEIRA);

        assertEquals("500", saldo.saldoQuantidade());
        assertTrue(saldo.saldoFinanceiro().contains("5.000,00")); // 500 x custo médio 10
    }

    // ---------- histórico ----------

    @Test
    void historicoSaiEmOrdemCronologica() {
        incluirCarteira(ID_CARTEIRA, "Ana");
        incluirMovimentacao(LocalDate.of(2026, 2, 5), TipoOperacao.VENDA, "100");
        incluirMovimentacao(LocalDate.of(2026, 2, 1), TipoOperacao.COMPRA, "1000");
        incluirMovimentacao(LocalDate.of(2026, 2, 3), TipoOperacao.COMPRA, "200");

        RelatorioCarteiraDTO.Historico historico = controller.gerarHistorico(ID_CARTEIRA);

        List<String> datas = historico.movimentos().stream()
                .map(RelatorioCarteiraDTO.Movimento::data).toList();
        assertEquals(List.of("01/02/2026", "03/02/2026", "05/02/2026"), datas);
        assertEquals("report.wallet.history.type.buy", historico.movimentos().get(0).tipoChave());
        assertEquals("report.wallet.history.type.sell", historico.movimentos().get(2).tipoChave());
    }

    @Test
    void historicoDeCarteiraSemMovimentacoesEhListaVazia() {
        incluirCarteira(ID_CARTEIRA, "Ana");

        RelatorioCarteiraDTO.Historico historico = controller.gerarHistorico(ID_CARTEIRA);

        assertTrue(historico.movimentos().isEmpty());
    }

    @Test
    void historicoDeCarteiraInexistenteLancaExcecaoDeDominio() {
        assertThrows(ValidacaoException.class, () -> controller.gerarHistorico(99));
    }

    // ---------- ganho/perda ----------

    @Test
    void ganhoPerdaSemCarteirasLancaExcecaoDeDominio() {
        assertThrows(ValidacaoException.class, () -> controller.gerarGanhoPerdaTodas());
    }

    @Test
    void ganhoPerdaApuraResultadoRealizadoEValorDeMercado() {
        incluirCarteira(ID_CARTEIRA, "Ana");
        precos.put(DIA_COMPRA, new BigDecimal("12.00"));
        precos.put(DIA_VENDA, new BigDecimal("14.00"));
        precos.put(LocalDate.now(), new BigDecimal("15.00"));
        incluirMovimentacao(DIA_COMPRA, TipoOperacao.COMPRA, "1000");
        incluirMovimentacao(DIA_VENDA, TipoOperacao.VENDA, "500");

        RelatorioCarteiraDTO.GanhoPerda relatorio = controller.gerarGanhoPerdaTodas().get(0);

        assertEquals(2, relatorio.operacoes().size());
        assertEquals("500", relatorio.quantidade());
        assertTrue(relatorio.custoMedio().contains("12,00"));
        assertTrue(relatorio.resultadoAcumulado().contains("1.000,00")); // 500 x (14 - 12)
        assertTrue(relatorio.saldoMercado().contains("7.500,00"));       // 500 x 15
        assertTrue(relatorio.resultadoPotencial().contains("2.500,00")); // 7.000 + 7.500 - 12.000
        assertFalse(relatorio.cotacaoDesatualizada());
    }

    @Test
    void ganhoPerdaUsaUltimaCotacaoQuandoOraculoFalhaEAvisa() {
        incluirCarteira(ID_CARTEIRA, "Ana");
        incluirMovimentacao(DIA_COMPRA, TipoOperacao.COMPRA, "1000");
        controller.gerarGanhoPerdaTodas(); // guarda a cotação atual em cache
        oraculoForaDoAr = true;

        RelatorioCarteiraDTO.GanhoPerda relatorio = controller.gerarGanhoPerdaTodas().get(0);

        assertTrue(relatorio.cotacaoDesatualizada());
    }

    @Test
    void ganhoPerdaSemOraculoESemCotacaoAnteriorLancaExcecao() {
        incluirCarteira(ID_CARTEIRA, "Ana");
        oraculoForaDoAr = true;

        assertThrows(OraculoException.class, () -> controller.gerarGanhoPerdaTodas());
    }

    // ---------- apoio ----------

    /** Oráculo simulado: preço 10,00 por padrão, com exceções definidas em {@code precos}. */
    private OraculoCotacao oraculoSimulado() {
        return data -> {
            if (!oraculoDeveSerConsultado) {
                throw new IllegalStateException("O Oráculo não deveria ter sido consultado.");
            }
            if (oraculoForaDoAr && data.equals(LocalDate.now())) {
                throw new OraculoException("Oráculo indisponível.");
            }
            return new Cotacao(data, precos.getOrDefault(data, new BigDecimal("10.00")));
        };
    }

    private void incluirCarteira(int id, String titular) {
        carteiras.incluir(new Carteira(id, titular, "Corretora"));
    }

    private void incluirMovimentacao(LocalDate data, TipoOperacao tipo, String quantidade) {
        movimentacoes.incluir(new Movimentacao(ID_CARTEIRA, data, tipo, new BigDecimal(quantidade)));
    }

    private List<Integer> identificadores(List<Carteira> lista) {
        return lista.stream().map(Carteira::getId).toList();
    }

    private List<String> titulares(List<Carteira> lista) {
        return lista.stream().map(Carteira::getTitular).toList();
    }
}
