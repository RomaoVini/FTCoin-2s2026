package test;

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
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {

    private static boolean criouEnvTemporario = false;
    private AtomicBoolean fechada;
    private AtomicInteger closeCount;
    private Connection connectionStub;
    private Driver fakeDriver;

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

        fechada = new AtomicBoolean(false);
        closeCount = new AtomicInteger(0);

        connectionStub = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("isClosed".equals(method.getName())) {
                        return fechada.get();
                    }
                    if ("close".equals(method.getName())) {
                        closeCount.incrementAndGet();
                        fechada.set(true);
                        return null;
                    }
                    return null;
                }
        );

        fakeDriver = new Driver() {
            @Override
            public Connection connect(String url, Properties info) { return connectionStub; }
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
    void getInstancia_retornaMesmaInstanciaSingletonEConexaoAtiva() throws SQLException {
        DatabaseConnection dc1 = DatabaseConnection.getInstancia();
        DatabaseConnection dc2 = DatabaseConnection.getInstancia();

        assertNotNull(dc1);
        assertSame(dc1, dc2);
        assertSame(connectionStub, dc1.getConexao());
    }

    @Test
    void fechar_conexaoAberta_fechaConexaoComSucesso() throws SQLException {
        DatabaseConnection dc = DatabaseConnection.getInstancia();

        dc.fechar();

        assertEquals(1, closeCount.get());
        assertTrue(fechada.get());
    }

    @Test
    void fechar_conexaoJaFechada_naoChamaCloseNovamente() throws SQLException {
        DatabaseConnection dc = DatabaseConnection.getInstancia();
        fechada.set(true);

        dc.fechar();

        assertEquals(0, closeCount.get());
    }
}