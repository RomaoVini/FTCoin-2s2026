package br.ftcoin.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * Fábrica de conexões com o MariaDB.
 *
 * Os dados de acesso podem ser definidos por variáveis de ambiente
 * (FTCOIN_DB_URL, FTCOIN_DB_USER, FTCOIN_DB_PASSWORD). Se não existirem,
 * valem os padrões abaixo, que você deve ajustar para o seu banco.
 *
 * Requer o driver JDBC no classpath: org.mariadb.jdbc:mariadb-java-client
 */
public final class ConnectionFactory {

    private static final String URL = valor("FTCOIN_DB_URL", "jdbc:mariadb://localhost:3306/ftcoin");
    private static final String USUARIO = valor("FTCOIN_DB_USER", "root");
    private static final String SENHA = valor("FTCOIN_DB_PASSWORD", "");

    private ConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    private static String valor(String variavel, String padrao) {
        String v = System.getenv(variavel);
        return (v == null || v.isBlank()) ? padrao : v;
    }
}