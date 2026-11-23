package test;

import br.ftcoin.dao.CarteiraDAOMariaDB;
import br.ftcoin.model.Carteira;
import br.ftcoin.util.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class CarteiraDAOMariaDBTest {

    private static boolean criouEnvTemporario = false;
    private Map<Integer, Object> params;
    private List<Object[]> linhasRs;
    private int cursorRs;
    private int updateReturn;
    private SQLException erroSql;
    private Driver fakeDriver;
    private CarteiraDAOMariaDB dao;

    @BeforeAll
    static void prepararAmbiente() throws Exception {
        Path envPath = Path.of("mariadb.env");
        if (!Files.exists(envPath)) {
            Files.writeString(envPath, "MARIADB_USER=test_user\nMARIADB_PASSWORD=test_pass\n");
            criouEnvTemporario = true;
        }
    }

    @AfterAll
    static void limparAmbiente() throws Exception {
        if (criouEnvTemporario) {
            Files.deleteIfExists(Path.of("mariadb.env"));
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        resetarSingleton();

        params = new HashMap<>();
        linhasRs = new ArrayList<>();
        cursorRs = -1;
        updateReturn = 1;
        erroSql = null;

        ResultSet rsProxy = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("next".equals(name)) {
                        return ++cursorRs < linhasRs.size();
                    }
                    if ("getInt".equals(name) && "IdCarteira".equals(args[0])) {
                        return linhasRs.get(cursorRs)[0];
                    }
                    if ("getString".equals(name)) {
                        if ("Titular".equals(args[0])) return linhasRs.get(cursorRs)[1];
                        if ("Corretora".equals(args[0])) return linhasRs.get(cursorRs)[2];
                    }
                    return null;
                }
        );

        PreparedStatement psProxy = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.startsWith("set") && args != null && args.length >= 2) {
                        params.put((Integer) args[0], args[1]);
                        return null;
                    }
                    if ("executeUpdate".equals(name)) {
                        if (erroSql != null) {
                            throw erroSql;
                        }
                        return updateReturn;
                    }
                    if ("executeQuery".equals(name)) {
                        if (erroSql != null) {
                            throw erroSql;
                        }
                        cursorRs = -1;
                        return rsProxy;
                    }
                    return null;
                }
        );

        Connection conProxy = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("isClosed".equals(method.getName())) {
                        return false;
                    }
                    if ("prepareStatement".equals(method.getName())) {
                        return psProxy;
                    }
                    return null;
                }
        );

        fakeDriver = new Driver() {
            @Override
            public Connection connect(String url, Properties info) { return conProxy; }
            @Override
            public boolean acceptsURL(String url) { return url != null && url.startsWith("jdbc:mariadb:"); }
            @Override
            public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }
            @Override
            public int getMajorVersion() { return 1; }
            @Override
            public int getMinorVersion() { return 0; }
            @Override
            public boolean jdbcCompliant() { return false; }
            @Override
            public Logger getParentLogger() { return Logger.getGlobal(); }
        };
        DriverManager.registerDriver(fakeDriver);

        dao = new CarteiraDAOMariaDB();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (fakeDriver != null) {
            DriverManager.deregisterDriver(fakeDriver);
        }
        resetarSingleton();
    }

    private void resetarSingleton() throws Exception {
        Field field = DatabaseConnection.class.getDeclaredField("instancia");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    void incluir_carteiraValida_executaInsertComSucesso() {
        dao.incluir(new Carteira(1, "Alice", "Binance"));

        assertEquals(1, params.get(1));
        assertEquals("Alice", params.get(2));
        assertEquals("Binance", params.get(3));
    }

    @Test
    void incluir_carteiraNula_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dao.incluir(null));
    }

    @Test
    void incluir_idDuplicadoSql1062_lancaIllegalArgumentException() {
        erroSql = new SQLException("Duplicate entry", "23000", 1062);

        assertThrows(IllegalArgumentException.class, () -> dao.incluir(new Carteira(1, "Alice", "Binance")));
    }

    @Test
    void incluir_erroGenericoSql_lancaRuntimeException() {
        erroSql = new SQLException("Connection lost", "08003", 2006);

        assertThrows(RuntimeException.class, () -> dao.incluir(new Carteira(1, "Alice", "Binance")));
    }

    @Test
    void consultar_idExistente_retornaOptionalPreenchido() {
        linhasRs.add(new Object[]{10, "Bob", "Kraken"});

        Optional<Carteira> resultado = dao.consultar(10);

        assertTrue(resultado.isPresent());
        assertEquals(10, resultado.get().getId());
        assertEquals("Bob", resultado.get().getTitular());
        assertEquals("Kraken", resultado.get().getCorretora());
    }

    @Test
    void consultar_idInexistente_retornaOptionalVazio() {
        Optional<Carteira> resultado = dao.consultar(999);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void consultar_idNulo_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dao.consultar(null));
    }

    @Test
    void consultarTodos_retornaListaMapeada() {
        linhasRs.add(new Object[]{1, "Alice", "Binance"});
        linhasRs.add(new Object[]{2, "Bob", "Coinbase"});

        List<Carteira> lista = dao.consultarTodos();

        assertEquals(2, lista.size());
        assertEquals("Alice", lista.get(0).getTitular());
        assertEquals("Bob", lista.get(1).getTitular());
    }

    @Test
    void consultarTodasOrdenadasPorTitular_retornaListaMapeada() {
        linhasRs.add(new Object[]{2, "Ana", "Mercado Bitcoin"});

        List<Carteira> lista = dao.consultarTodasOrdenadasPorTitular();

        assertEquals(1, lista.size());
        assertEquals("Ana", lista.get(0).getTitular());
    }

    @Test
    void editar_carteiraExistente_atualizaComSucesso() {
        updateReturn = 1;

        dao.editar(new Carteira(1, "Alice Nova", "OKX"));

        assertEquals("Alice Nova", params.get(1));
        assertEquals("OKX", params.get(2));
        assertEquals(1, params.get(3));
    }

    @Test
    void editar_carteiraInexistente_lancaNoSuchElementException() {
        updateReturn = 0;

        assertThrows(NoSuchElementException.class, () -> dao.editar(new Carteira(99, "Ninguem", "X")));
    }

    @Test
    void editar_carteiraNula_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dao.editar(null));
    }

    @Test
    void excluir_carteiraSemMovimentacoes_removeComSucesso() {
        updateReturn = 1;

        dao.excluir(1);

        assertEquals(1, params.get(1));
    }

    @Test
    void excluir_carteiraInexistente_lancaNoSuchElementException() {
        updateReturn = 0;

        assertThrows(NoSuchElementException.class, () -> dao.excluir(99));
    }

    @Test
    void excluir_carteiraComMovimentacoesSql1451_lancaIllegalStateException() {
        erroSql = new SQLException("FK constraint fails", "23000", 1451);

        assertThrows(IllegalStateException.class, () -> dao.excluir(1));
    }

    @Test
    void excluir_idNulo_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dao.excluir(null));
    }
}