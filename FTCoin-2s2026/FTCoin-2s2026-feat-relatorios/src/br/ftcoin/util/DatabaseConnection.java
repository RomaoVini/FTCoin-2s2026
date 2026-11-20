package br.ftcoin.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gerencia a conexão única com o banco de dados relacional MariaDB
 * utilizando o padrão de projeto <b>Singleton</b> (com sincronização <i>thread-safe</i>).
 *
 * <p>As credenciais ({@code MARIADB_USER} e {@code MARIADB_PASSWORD}) são buscadas
 * primeiramente nas variáveis de ambiente do sistema operacional e, como alternativa,
 * no arquivo local {@code mariadb.env} na raiz do projeto.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mariadb://143.106.243.64:3306/POO2_2s26_A02?useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci";

    private static final Map<String, String> ENV_LOCAL = carregarArquivoEnv(Path.of("mariadb.env"));

    private static final String URL = obterConfiguracao("MARIADB_URL", DEFAULT_URL);
    private static final String USUARIO = obterConfiguracao("MARIADB_USER", null);
    private static final String SENHA = obterConfiguracao("MARIADB_PASSWORD", null);

    private static DatabaseConnection instancia;
    private Connection conexao;

    /**
     * Construtor privado do Singleton que valida as credenciais e estabelece
     * a conexão física com o servidor MariaDB via {@link DriverManager}.
     *
     * @throws SQLException se as credenciais não estiverem configuradas ou ocorrer falha de conexão.
     */
    private DatabaseConnection() throws SQLException {
        if (USUARIO == null || USUARIO.isBlank() || SENHA == null || SENHA.isBlank()) {
            throw new SQLException(I18n.get("database.connection.credentials.missing"));
        }

        this.conexao = DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    /**
     * Retorna a instância única de {@link DatabaseConnection}, recriando a conexão
     * automaticamente caso ela ainda não tenha sido aberta ou tenha sido fechada.
     *
     * @return Instância ativa do gerenciador de conexão.
     * @throws SQLException se ocorrer erro ao conectar ao banco de dados.
     */
    public static synchronized DatabaseConnection getInstancia() throws SQLException {
        if (instancia == null || instancia.conexao == null || instancia.conexao.isClosed()) {
            instancia = new DatabaseConnection();
        }
        return instancia;
    }

    /**
     * Retorna o objeto {@link Connection} JDBC ativo para execução de instruções SQL.
     *
     * @return Conexão JDBC ativa.
     */
    public Connection getConexao() {
        return conexao;
    }

    /**
     * Encerra a conexão física ativa com o banco de dados MariaDB de forma segura.
     *
     * @throws SQLException se ocorrer falha ao fechar a conexão junto ao driver JDBC.
     */
    public synchronized void fechar() throws SQLException {
        if (conexao != null && !conexao.isClosed()) {
            conexao.close();
        }
    }

    /**
     * Busca a chave nas variáveis de ambiente do SO; se ausente, procura no arquivo
     * {@code mariadb.env}; se ainda ausente, retorna o valor padrão informado.
     */
    private static String obterConfiguracao(String chave, String valorPadrao) {
        String envSistema = System.getenv(chave);
        if (envSistema != null && !envSistema.isBlank()) {
            return envSistema.trim();
        }

        String envArquivo = ENV_LOCAL.get(chave);
        if (envArquivo != null && !envArquivo.isBlank()) {
            return envArquivo.trim();
        }

        return valorPadrao;
    }

    /**
     * Lê pares {@code CHAVE=VALOR} do arquivo {@code mariadb.env}, ignorando linhas
     * vazias ou comentadas com {@code #}.
     */
    private static Map<String, String> carregarArquivoEnv(Path caminho) {
        Map<String, String> mapa = new HashMap<>();
        if (!Files.exists(caminho)) {
            return mapa;
        }

        try {
            for (String linha : Files.readAllLines(caminho)) {
                String limpa = linha.trim();
                if (limpa.isEmpty() || limpa.startsWith("#") || !limpa.contains("=")) {
                    continue;
                }
                String[] partes = limpa.split("=", 2);
                mapa.put(partes[0].trim(), partes[1].trim());
            }
        } catch (IOException ignored) {
            // Caso não consiga ler o arquivo local, dependerá apenas das variáveis de ambiente do SO
        }
        return mapa;
    }
}