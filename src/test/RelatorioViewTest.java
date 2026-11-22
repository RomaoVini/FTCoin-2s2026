package test;

import br.ftcoin.controller.RelatorioController;
import br.ftcoin.dao.CarteiraDAOMemoria;
import br.ftcoin.dao.MovimentacaoDAOMemoria;
import br.ftcoin.enums.Idioma;
import br.ftcoin.model.Carteira;
import br.ftcoin.model.Cotacao;
import br.ftcoin.service.OraculoCotacao;
import br.ftcoin.service.OraculoException;
import br.ftcoin.util.I18n;
import br.ftcoin.view.MenuView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes das telas de relatório ({@link RelatorioView}): simulam a digitação do usuário e
 * conferem o que aparece no console, incluindo as mensagens de erro tratadas.
 */
class RelatorioViewTest {

    private final PrintStream saidaOriginal = System.out;
    private ByteArrayOutputStream capturada;
    private CarteiraDAOMemoria carteiras;
    private MovimentacaoDAOMemoria movimentacoes;

    @BeforeEach
    void setUp() {
        I18n.setIdioma(Idioma.PORTUGUES);
        carteiras = new CarteiraDAOMemoria();
        movimentacoes = new MovimentacaoDAOMemoria();
        capturada = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturada, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restaurarSaida() {
        System.setOut(saidaOriginal);
    }

    @Test
    void semCarteirasAsListagensMostramMensagemDeErroTratada() {
        String tela = executar("1\n\n2\n\n0\n");

        assertEquals(2, ocorrencias(tela, I18n.get("report.wallets.empty")));
    }

    @Test
    void listagemPorIdentificadorMostraAsCarteirasEmOrdemNumerica() {
        carteiras.incluir(new Carteira(10, "Bruno", "X"));
        carteiras.incluir(new Carteira(2, "Carla", "X"));
        carteiras.incluir(new Carteira(1, "Ana", "X"));

        String tela = executar("1\n\n0\n");

        assertTrue(tela.contains(I18n.get("report.wallets.byId.title")));
        assertTrue(tela.indexOf("Ana") < tela.indexOf("Carla"));
        assertTrue(tela.indexOf("Carla") < tela.indexOf("Bruno")); // 1, 2, 10
    }

    @Test
    void listagemPorTitularIgnoraAcentosEMaiusculas() {
        carteiras.incluir(new Carteira(1, "Zé", "X"));
        carteiras.incluir(new Carteira(2, "ana", "X"));
        carteiras.incluir(new Carteira(3, "Álvaro", "X"));

        String tela = executar("2\n\n0\n");

        assertTrue(tela.contains(I18n.get("report.wallets.byHolder.title")));
        assertTrue(tela.indexOf("Álvaro") < tela.indexOf("ana"));
        assertTrue(tela.indexOf("ana") < tela.indexOf("Zé"));
    }

    @Test
    void saldoDeCarteiraInexistenteMostraErroTratadoSemEncerrarAMenu() {
        String tela = executar("3\n99\n\n0\n");

        assertTrue(tela.contains(I18n.getComParametros("wallet.error.notFound", "99")));
    }

    @Test
    void saldoDeCarteiraSemMovimentacoesMostraZero() {
        carteiras.incluir(new Carteira(1, "Ana", "X"));

        String tela = executar("3\n1\n\n0\n");

        assertTrue(tela.contains(I18n.get("report.wallet.currentBalance.title")));
        assertTrue(tela.contains("Ana"));
        assertTrue(tela.contains(I18n.get("report.wallet.currentBalance.balance") + " 0"));
    }

    @Test
    void identificadorNaoNumericoFazAViewPerguntarDeNovo() {
        carteiras.incluir(new Carteira(1, "Ana", "X"));

        String tela = executar("3\nabc\n1\n\n0\n");

        assertTrue(tela.contains(I18n.get("generic.confirmInvalid")));
        assertTrue(tela.contains(I18n.get("report.wallet.currentBalance.title")));
    }

    @Test
    void historicoDeCarteiraSemMovimentacoesAvisaQueEstaVazio() {
        carteiras.incluir(new Carteira(1, "Ana", "X"));

        String tela = executar("4\n1\n\n0\n");

        assertTrue(tela.contains(I18n.get("report.wallet.history.empty")));
    }

    @Test
    void ganhoPerdaSemCarteirasMostraMensagemDeErroTratada() {
        String tela = executar("5\n\n0\n");

        assertTrue(tela.contains(I18n.get("report.wallets.empty")));
    }

    @Test
    void oraculoForaDoArMostraMensagemAmigavelEmVezDeErroTecnico() {
        carteiras.incluir(new Carteira(1, "Ana", "X"));
        movimentacoes.incluir(new br.ftcoin.model.Movimentacao(
                1, java.time.LocalDate.of(2026, 2, 1), br.ftcoin.enums.TipoOperacao.COMPRA, new BigDecimal("10")));
        OraculoCotacao foraDoAr = data -> {
            throw new OraculoException("Não foi possível consultar o oráculo de cotações.");
        };

        String tela = executar("3\n1\n\n0\n", foraDoAr);

        assertTrue(tela.contains("Não foi possível consultar o oráculo de cotações."));
        assertTrue(!tela.contains("Exception"));
    }

    // ---------- apoio ----------

    private String executar(String entrada) {
        OraculoCotacao oraculo = data -> new Cotacao(data, BigDecimal.valueOf(100));
        return executar(entrada, oraculo);
    }

    private String executar(String entrada, OraculoCotacao oraculo) {
        MenuView menuView = new MenuView(new Scanner(entrada));
        new RelatorioController(menuView, carteiras, movimentacoes, oraculo).exibirMenuRelatorios();
        return capturada.toString(StandardCharsets.UTF_8);
    }

    private static int ocorrencias(String texto, String trecho) {
        int total = 0;
        for (int i = texto.indexOf(trecho); i >= 0; i = texto.indexOf(trecho, i + trecho.length())) {
            total++;
        }
        return total;
    }
}