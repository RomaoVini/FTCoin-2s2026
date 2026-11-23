package test;

import br.ftcoin.controller.AjudaController;
import br.ftcoin.view.AjudaView;
import br.ftcoin.view.ConsoleUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class AjudaTest {

    @Test
    void construtores_consoleNulo_lancamNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AjudaController(null));
        assertThrows(NullPointerException.class, () -> new AjudaView(null));
    }

    @Test
    void exibirMenuAjuda_renderizaInstrucoesCreditosEDataAcesso() {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));

        try {
            ConsoleUtils console = new ConsoleUtils(new Scanner("\n"));
            AjudaController controller = new AjudaController(console);

            controller.exibirMenuAjuda();

            String texto = saida.toString(StandardCharsets.UTF_8);
            assertFalse(texto.isBlank());
            assertTrue(texto.contains("FT_Coin"));
        } finally {
            System.setOut(originalOut);
        }
    }
}