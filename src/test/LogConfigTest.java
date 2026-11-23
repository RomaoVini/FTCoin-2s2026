package test;

import br.ftcoin.util.LogConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class LogConfigTest {

    @AfterEach
    void tearDown() {
        Logger raiz = Logger.getLogger("");
        for (Handler h : raiz.getHandlers()) {
            if (h instanceof FileHandler) {
                h.close();
                raiz.removeHandler(h);
            }
        }
        LogConfig.iniciarApenasConsole();
    }

    @Test
    void iniciar_configuraConsoleEFileHandlerECriaDiretorioLogs() throws IOException {
        LogConfig.iniciar();

        Logger raiz = Logger.getLogger("");
        Handler[] handlers = raiz.getHandlers();

        assertEquals(Level.INFO, raiz.getLevel());
        assertTrue(Arrays.stream(handlers).anyMatch(h -> h instanceof ConsoleHandler));
        assertTrue(Arrays.stream(handlers).anyMatch(h -> h instanceof FileHandler));
        assertTrue(Files.exists(Path.of("logs")));
    }

    @Test
    void iniciarApenasConsole_mantemSomenteConsoleHandler() {
        LogConfig.iniciarApenasConsole();

        Logger raiz = Logger.getLogger("");
        Handler[] handlers = raiz.getHandlers();

        assertEquals(1, handlers.length);
        assertInstanceOf(ConsoleHandler.class, handlers[0]);
        assertEquals(Level.INFO, raiz.getLevel());
    }
}