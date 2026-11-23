package test;

import br.ftcoin.view.ConsoleUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleUtilsTest {

    private ByteArrayOutputStream saida;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        saida = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void construtor_scannerNulo_lancaNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ConsoleUtils(null));
    }

    @Test
    void lerString_ignoraLinhasEmBrancoERetornaTextoTrimmed() {
        ConsoleUtils console = new ConsoleUtils(new Scanner("   \n\n  FTCoin  \n"));

        String resultado = console.lerString("Digite:");

        assertEquals("FTCoin", resultado);
    }

    @Test
    void lerStringOpcional_permiteEntradaVazia() {
        ConsoleUtils console = new ConsoleUtils(new Scanner("\n"));

        String resultado = console.lerStringOpcional("Opcional:");

        assertEquals("", resultado);
    }

    @Test
    void lerInteiro_repeteAteReceberNumeroValido() {
        ConsoleUtils console = new ConsoleUtils(new Scanner("abc\n4.5\n42\n"));

        int resultado = console.lerInteiro("Numero:");

        assertEquals(42, resultado);
    }

    @Test
    void metodosDeExibicaoEPausa_imprimemMensagensFormatadas() {
        ConsoleUtils console = new ConsoleUtils(new Scanner("\n"));

        console.exibirSucesso("Ok");
        console.exibirErro("Falha");
        console.exibirAlerta("Atencao");
        console.pausar();

        String texto = saida.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("Ok"));
        assertTrue(texto.contains("Falha"));
        assertTrue(texto.contains("Atencao"));
    }
}