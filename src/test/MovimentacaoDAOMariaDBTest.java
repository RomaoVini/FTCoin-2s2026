package test;

import br.ftcoin.dao.MovimentacaoDAOMariaDB;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.util.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class MovimentacaoDAOMariaDBTest {

    private static boolean criouEnvTemporario = false;
    private Map<Integer, Object> params;
    private List<Object[]> linhasRs;
    private int cursorRs;
    private int generatedKey;
    private int updateReturn;
    private SQLException erroSql;
    private Driver fakeDriver;
    private MovimentacaoDAOMariaDB dao;

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
        generatedKey = 42;
        updateReturn = 1;
        erroSql = null;

        ResultSet keysProxy = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("next".equals(name)) {
                        return true;
                    }
                    if ("getInt".equals(name)) {
                        return generatedKey;
                    }
                    return null;
                }
        );

        ResultSet rsProxy = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("next".equals(name)) {
                        return ++cursorRs < linhasRs.size();
                    }
                    if ("getInt".equals(name)) {
                        if ("IdMovimento".equals(args[0])) return linhasRs.get(cursorRs)[0];
                        if ("IdCarteira".equals(args[0])) return linhasRs.get(cursorRs)[1];
                    }
                    if ("getDate".equals(name)) {
                        return linhasRs.get(cursorRs)[2];
                    }
                    if ("getString".equals(name)) {
                        return linhasRs.get(cursorRs)[3];
                    }
                    if ("getBigDecimal".equals(name)) {
                        return linhasRs.get(cursorRs)[4];
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
                    if ("getGeneratedKeys".equals(name)) {
                        return keysProxy;
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

        dao = new MovimentacaoDAOMariaDB();
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
    void incluir_movimentacaoValida_atribuiIdGeradoPeloBanco() {
        generatedKey = 42;
        Movimentacao mov = new Movimentacao(0, 1, LocalDate.of(2026, 5, 10), TipoOperacao.COMPRA, new BigDecimal("1.500"));

        dao.incluir(mov);

        assertEquals(42, mov.getIdMovimento());
        assertEquals(1, params.get(1));
    }

    @Test
    void incluir_movimentacaoNula_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dao.incluir(null));
    }

    @Test
    void incluir_carteiraInexistenteSql1452_lancaIllegalArgumentException() {
        erroSql = new SQLException("Cannot add child row", "23000", 1452);
        Movimentacao mov = new Movimentacao(0, 999, LocalDate.of(2026, 5, 10), TipoOperacao.COMPRA, BigDecimal.ONE);

        assertThrows(IllegalArgumentException.class, () -> dao.incluir(mov));
    }

    @Test
    void editar_movimentacaoExistente_atualizaComSucesso() {
        updateReturn = 1;
        Movimentacao mov = new Movimentacao(5, 1, LocalDate.of(2026, 6, 1), TipoOperacao.VENDA, new BigDecimal("0.500"));

        dao.editar(mov);

        assertEquals(5, params.get(5));
    }

    @Test
    void editar_movimentacaoInexistente_lancaNoSuchElementException() {
        updateReturn = 0;
        Movimentacao mov = new Movimentacao(99, 1, LocalDate.of(2026, 6, 1), TipoOperacao.VENDA, BigDecimal.ONE);

        assertThrows(NoSuchElementException.class, () -> dao.editar(mov));
    }

    @Test
    void editar_carteiraInexistenteSql1452_lancaIllegalArgumentException() {
        erroSql = new SQLException("FK fail", "23000", 1452);
        Movimentacao mov = new Movimentacao(5, 999, LocalDate.of(2026, 6, 1), TipoOperacao.VENDA, BigDecimal.ONE);

        assertThrows(IllegalArgumentException.class, () -> dao.editar(mov));
    }

    @Test
    void excluir_idExistente_removeComSucesso() {
        updateReturn = 1;

        dao.excluir(7);

        assertEquals(7, params.get(1));
    }

    @Test
    void excluir_idInexistente_lancaNoSuchElementException() {
        updateReturn = 0;

        assertThrows(NoSuchElementException.class, () -> dao.excluir(999));
    }

    @Test
    void consultar_idExistente_retornaMovimentacaoMapeada() {
        linhasRs.add(new Object[]{7, 2, Date.valueOf(LocalDate.of(2026, 3, 15)), "C", new BigDecimal("2.250")});

        Optional<Movimentacao> resultado = dao.consultar(7);

        assertTrue(resultado.isPresent());
        assertEquals(7, resultado.get().getIdMovimento());
        assertEquals(TipoOperacao.COMPRA, resultado.get().getTipoOperacao());
        assertEquals(new BigDecimal("2.250"), resultado.get().getQuantidade());
    }

    @Test
    void consultarTodos_e_consultarPorCarteira_retornamListas() {
        linhasRs.add(new Object[]{1, 3, Date.valueOf(LocalDate.of(2026, 4, 1)), "V", BigDecimal.TEN});

        List<Movimentacao> todas = dao.consultarTodos();
        List<Movimentacao> daCarteira = dao.consultarPorCarteira(3);

        assertEquals(1, todas.size());
        assertEquals(1, daCarteira.size());
        assertEquals(TipoOperacao.VENDA, daCarteira.get(0).getTipoOperacao());
    }
}