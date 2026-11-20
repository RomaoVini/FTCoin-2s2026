package br.ftcoin;

import br.ftcoin.controller.AppController;
import br.ftcoin.util.DatabaseConnection;

import java.sql.SQLException;

/**
 * Classe principal e ponto de entrada da aplicação <b>FT_Coin</b>.
 *
 * <p>Responsável exclusivamente por instanciar o controlador raiz ({@link AppController})
 * com o modo de persistência inicial e garantir o encerramento seguro da conexão
 * com o banco de dados ao término da execução.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class Main {

    /**
     * Construtor privado para evitar instanciação da classe principal.
     */
    private Main() {
    }

    /**
     * Método principal que inicia o ciclo de vida da interface de linha de comando (CLI).
     *
     * @param args Argumentos opcionais de linha de comando (ex: {@code "MEMORIA"} ou {@code "MARIADB"}).
     */
    public static void main(String[] args) {
        String persistenciaInicial = (args != null && args.length > 0) ? args[0] : "MEMORIA";

        try {
            AppController app = new AppController(persistenciaInicial);
            app.iniciar();
        } finally {
            encerrarConexaoBanco();
        }
    }

    /**
     * Encerra a conexão Singleton com o MariaDB caso tenha sido estabelecida
     * durante a sessão do usuário.
     */
    private static void encerrarConexaoBanco() {
        try {
            DatabaseConnection.getInstancia().fechar();
        } catch (SQLException ignored) {
            // Caso o usuário tenha utilizado apenas persistência em memória ou as credenciais
            // não tenham sido configuradas, nenhuma conexão ativa precisa ser fechada.
        }
    }
}