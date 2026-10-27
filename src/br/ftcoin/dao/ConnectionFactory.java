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

    private static final String URL = valor("FTCOIN_DB_URL", "jdbc:mariadb://143.106.243.64:3306/POO2_2s26_A02?useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci");
    private static final String USUARIO = valor("FTCOIN_DB_USER", "POO2_2s26_A02");
    private static final String SENHA = valor("FTCOIN_DB_PASSWORD", "W90OL8wsC8");

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