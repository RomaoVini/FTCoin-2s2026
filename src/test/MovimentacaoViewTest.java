package test;

import br.ftcoin.controller.MovimentacaoController;
import br.ftcoin.dao.CarteiraDAOMemoria;
import br.ftcoin.dao.MovimentacaoDAOMemoria;
import br.ftcoin.model.Carteira;
import br.ftcoin.model.Cotacao;
import br.ftcoin.service.OraculoCotacao;
import br.ftcoin.view.ConsoleUtils;
import br.ftcoin.view.MovimentacaoView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class MovimentacaoViewTest {

    private ByteArrayOutputStream saida;
    private PrintStream originalOut;
    private CarteiraDAOMemoria carteiraDAO;
    private MovimentacaoDAOMemoria movimentacaoDAO;
    private MovimentacaoController controller;

    @BeforeEach
    void setUp() {
        saida = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));

        carteiraDAO = new CarteiraDAOMemoria();
        carteiraDAO.incluir(new Carteira(1, "Alice", "Binance"));
        movimentacaoDAO = new MovimentacaoDAOMemoria();

        OraculoCotacao oraculoFake = data -> new Cotacao(data, new BigDecimal("500000.00"));
        controller = new MovimentacaoController(carteiraDAO, movimentacaoDAO, oraculoFake);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void construtor_parametrosNulos_lancaNullPointerException() {
        ConsoleUtils console = new ConsoleUtils(new Scanner("0\n"));
        assertThrows(NullPointerException.class, () -> new MovimentacaoView(null, controller));
        assertThrows(NullPointerException.class, () -> new MovimentacaoView(console, null));
    }

    @Test
    void fluxoCompleto_compraVendaECotacaoComEntradasInvalidas() {
        String entradas = String.join("\n",
                "1", "1", "data-errada", "10/05/2026", "abc", "2,5", "",
                "2", "1", "2026-05-11", "1.0", "",
                "3", "", "",
                "0"
        ) + "\n";

        MovimentacaoView view = new MovimentacaoView(new ConsoleUtils(new Scanner(entradas)), controller);
        view.exibirMenu();

        assertEquals(2, movimentacaoDAO.consultarTodos().size());
        String texto = saida.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("500000.00"));
    }

    @Test
    void fluxo_vendaSemSaldo_exibeErroSemEncerrarMenu() {
        String entradas = "2\n1\n\n10\n\n0\n";
        MovimentacaoView view = new MovimentacaoView(new ConsoleUtils(new Scanner(entradas)), controller);

        assertDoesNotThrow(view::exibirMenu);
        assertTrue(movimentacaoDAO.consultarTodos().isEmpty());
    }
}