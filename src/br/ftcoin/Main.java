package br.ftcoin;

import br.ftcoin.controller.AppController;
import br.ftcoin.util.ConsoleEncoding;
import br.ftcoin.util.LogConfig;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe principal e ponto de entrada da aplicação <b>FT_Coin</b>.
 *
 * <p>Responsável exclusivamente por instanciar o controlador raiz ({@link AppController})
 * com o modo de persistência inicial e iniciar o ciclo de vida da aplicação.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 * @see AppController
 */
public final class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    /**
     * Construtor privado para evitar instanciação da classe principal.
     */
    private Main() {
    }

    /**
     * Método principal que inicia a interface de linha de comando (CLI).
     *
     * @param args Argumentos opcionais de linha de comando (ex: {@code "MEMORIA"} ou {@code "MARIADB"}).
     */
    public static void main(String[] args) {
        configurarCodificacao();

        try {
            LogConfig.iniciar();
        } catch (IOException e) {
            LogConfig.iniciarApenasConsole();
            LOG.log(Level.SEVERE, "Não foi possível iniciar o log em arquivo; usando apenas o console", e);
        }

        try {
            String persistenciaInicial = (args != null && args.length > 0) ? args[0] : "MEMORIA";
            LOG.log(Level.INFO, "Iniciando FT_Coin com persistência {0}", persistenciaInicial);

            AppController app = new AppController(persistenciaInicial);
            app.iniciar();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erro inesperado durante a execução da aplicação", e);
        }
    }

    private static void configurarCodificacao() {
        Charset consoleCharset = ConsoleEncoding.getCharset();
        System.setOut(new PrintStream(System.out, true, consoleCharset));
        System.setErr(new PrintStream(System.err, true, consoleCharset));
    }
}