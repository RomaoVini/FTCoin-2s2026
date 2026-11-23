package test;

import br.ftcoin.controller.CarteiraController;
import br.ftcoin.dao.CarteiraDAOMemoria;
import br.ftcoin.view.CarteiraView;
import br.ftcoin.view.ConsoleUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class CarteiraViewTest {

    private ByteArrayOutputStream saida;
    private PrintStream originalOut;
    private CarteiraController controller;

    @BeforeEach
    void setUp() {
        saida = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));

        controller = new CarteiraController(new CarteiraDAOMemoria());
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void construtor_parametrosNulos_lancaNullPointerException() {
        ConsoleUtils console = new ConsoleUtils(new Scanner("0\n"));
        assertThrows(NullPointerException.class, () -> new CarteiraView(null, controller));
        assertThrows(NullPointerException.class, () -> new CarteiraView(console, null));
    }

    @Test
    void fluxoCompleto_cadastrarListarConsultarEditarEExcluir() {
        String entradas = String.join("\n",
                "1", "1", "Alice", "Binance", "",
                "2", "",
                "5", "1", "",
                "3", "1", "Alice Silva", "Kraken", "",
                "4", "1", "n", "",
                "4", "1", "s", "",
                "0"
        ) + "\n";

        CarteiraView view = new CarteiraView(new ConsoleUtils(new Scanner(entradas)), controller);
        view.exibirMenu();

        assertTrue(controller.listarTodas().isEmpty());
        String texto = saida.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("Alice"));
        assertTrue(texto.contains("Alice Silva"));
        assertTrue(texto.contains("Kraken"));
    }

    @Test
    void fluxo_listarVaziaEConsultarInexistente_trataAlertasEErrosSemQuebrar() {
        String entradas = "2\n\n5\n99\n\n0\n";
        CarteiraView view = new CarteiraView(new ConsoleUtils(new Scanner(entradas)), controller);

        assertDoesNotThrow(view::exibirMenu);
        assertFalse(saida.toString(StandardCharsets.UTF_8).isBlank());
    }
}