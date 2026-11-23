package br.ftcoin.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Configura o java.util.logging usado pela aplicação FT_Coin.
 */
public final class LogConfig {

    private static final Logger LOG = Logger.getLogger(LogConfig.class.getName());
    private static final String FORMATO =
            "%1$tF %1$tT.%1$tL [%4$s] %3$s - %5$s%6$s%n";

    private LogConfig() {
    }

    /**
     * Inicializa logs no console e em arquivos rotativos.
     *
     * @throws IOException se o diretório ou os arquivos de log não puderem ser criados
     */
    public static void iniciar() throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", FORMATO);
        Files.createDirectories(Path.of("logs"));

        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(Level.INFO);
        try {
            console.setEncoding("UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            // UTF-8 é suportado pelas instalações Java; mantém o encoding padrão se não estiver disponível.
        }
        console.setFormatter(new SimpleFormatter());

        FileHandler arquivo = new FileHandler(
                "logs/ftcoin-%g.log", 1_000_000, 5, true);
        arquivo.setLevel(Level.ALL);
        arquivo.setEncoding("UTF-8");
        arquivo.setFormatter(new SimpleFormatter());

        Logger raiz = Logger.getLogger("");
        for (Handler handler : raiz.getHandlers()) {
            raiz.removeHandler(handler);
        }
        raiz.setLevel(Level.INFO);
        raiz.addHandler(console);
        raiz.addHandler(arquivo);

        LOG.info("Configuração de logs iniciada");
    }

    /**
     * Mantém a aplicação operante usando apenas saída no console.
     */
    public static void iniciarApenasConsole() {
        System.setProperty("java.util.logging.SimpleFormatter.format", FORMATO);

        Logger raiz = Logger.getLogger("");
        for (Handler handler : raiz.getHandlers()) {
            raiz.removeHandler(handler);
        }

        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(Level.INFO);
        try {
            console.setEncoding("UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            // Mantém o encoding padrão do console.
        }
        console.setFormatter(new SimpleFormatter());

        raiz.setLevel(Level.INFO);
        raiz.addHandler(console);
    }
}
