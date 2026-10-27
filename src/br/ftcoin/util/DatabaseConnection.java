
package br.ftcoin.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gerencia a conexão com o MariaDB usando o padrão Singleton.
 */
public final class DatabaseConnection {

    private static final String URL =
            "jdbc:mariadb://SEU_HOST:3306/SEU_BANCO";

    private static final String USUARIO =
            System.getenv("FTCOIN_DB_USER");

    private static final String SENHA =
            System.getenv("FTCOIN_DB_PASSWORD");

    private static DatabaseConnection instancia;
    private Connection conexao;

    private DatabaseConnection() throws SQLException {
        if (USUARIO == null || SENHA == null) {
            throw new SQLException(
                    "Configure as credenciais do banco de dados.");
        }

        conexao = DriverManager.getConnection(
                URL, USUARIO, SENHA);
    }

    public static synchronized DatabaseConnection getInstancia()
            throws SQLException {

        if (instancia == null
                || instancia.conexao.isClosed()) {
            instancia = new DatabaseConnection();
        }

        return instancia;
    }

    public Connection getConexao() {
        return conexao;
    }

    public synchronized void fechar() {
        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
            }
        } catch (SQLException e) {
            System.err.println(
                    "Erro ao fechar conexão: " + e.getMessage());
        }
    }
}
