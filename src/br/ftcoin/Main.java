package br.ftcoin;

import br.ftcoin.controller.AppController;

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
        String persistenciaInicial = (args != null && args.length > 0) ? args[0] : "MEMORIA";
        AppController app = new AppController(persistenciaInicial);
        app.iniciar();
    }
}