package test;

import br.ftcoin.dao.CarteiraDAOMariaDB;
import br.ftcoin.model.Carteira;
import br.ftcoin.util.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CarteiraDAOMariaDBTest {

    private MockedStatic<DatabaseConnection> dbStaticMock;
    private DatabaseConnection dbInstanceMock;
    private Connection connectionMock;
    private PreparedStatement statementMock;
    private ResultSet resultSetMock;
    private CarteiraDAOMariaDB dao;

    @BeforeEach
    void setUp() throws SQLException {
        dbInstanceMock = mock(DatabaseConnection.class);
        connectionMock = mock(Connection.class);
        statementMock = mock(PreparedStatement.class);
        resultSetMock = mock(ResultSet.class);

        dbStaticMock = mockStatic(DatabaseConnection.class);
        dbStaticMock.when(DatabaseConnection::getInstancia).thenReturn(dbInstanceMock);
        when(dbInstanceMock.getConexao()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(anyString())).thenReturn(statementMock);

        dao = new CarteiraDAOMariaDB();
    }

    @AfterEach
    void tearDown() {
        dbStaticMock.close();
    }

    @Test
    void incluir_carteiraValida_executaInsertComSucesso() throws SQLException {
        Carteira c = new Carteira(1, "Alice", "Binance");

        dao.incluir(c);

        verify(statementMock).setInt(1, 1);
        verify(statementMock).setString(2, "Alice");
        verify(statementMock).setString(3, "Binance");
        verify(statementMock).executeUpdate();
    }

    @Test
    void incluir_carteiraNula_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dao.incluir(null));
    }

    @Test
    void incluir_idDuplicadoSql1062_lancaIllegalArgumentException() throws SQLException {
        when(statementMock.executeUpdate()).thenThrow(new SQLException("Duplicate entry", "23000", 1062));

        assertThrows(IllegalArgumentException.class, () -> dao.incluir(new Carteira(1, "Alice", "Binance")));
    }

    @Test
    void incluir_erroGenericoSql_lancaRuntimeException() throws SQLException {
        when(statementMock.executeUpdate()).thenThrow(new SQLException("Connection lost", "08003", 2006));

        assertThrows(RuntimeException.class, () -> dao.incluir(new Carteira(1, "Alice", "Binance")));
    }

    @Test
    void consultar_idExistente_retornaOptionalPreenchido() throws SQLException {
        when(statementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getInt("IdCarteira")).thenReturn(10);
        when(resultSetMock.getString("Titular")).thenReturn("Bob");
        when(resultSetMock.getString("Corretora")).thenReturn("Kraken");

        Optional<Carteira> resultado = dao.consultar(10);

        assertTrue(resultado.isPresent());
        assertEquals(10, resultado.get().getId());
        assertEquals("Bob", resultado.get().getTitular());
        assertEquals("Kraken", resultado.get().getCorretora());
    }

    @Test
    void consultar_idInexistente_retornaOptionalVazio() throws SQLException {
        when(statementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false);

        Optional<Carteira> resultado = dao.consultar(999);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void consultar_idNulo_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dao.consultar(null));
    }

    @Test
    void consultarTodos_retornaListaMapeada() throws SQLException {
        when(statementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true, true, false);
        when(resultSetMock.getInt("IdCarteira")).thenReturn(1, 2);
        when(resultSetMock.getString("Titular")).thenReturn("Alice", "Bob");
        when(resultSetMock.getString("Corretora")).thenReturn("Binance", "Coinbase");

        List<Carteira> lista = dao.consultarTodos();

        assertEquals(2, lista.size());
        assertEquals("Alice", lista.get(0).getTitular());
        assertEquals("Bob", lista.get(1).getTitular());
    }

    @Test
    void consultarTodasOrdenadasPorTitular_retornaListaMapeada() throws SQLException {
        when(statementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true, false);
        when(resultSetMock.getInt("IdCarteira")).thenReturn(2);
        when(resultSetMock.getString("Titular")).thenReturn("Ana");
        when(resultSetMock.getString("Corretora")).thenReturn("Mercado Bitcoin");

        List<Carteira> lista = dao.consultarTodasOrdenadasPorTitular();

        assertEquals(1, lista.size());
        assertEquals("Ana", lista.get(0).getTitular());
    }

    @Test
    void editar_carteiraExistente_atualizaComSucesso() throws SQLException {
        when(statementMock.executeUpdate()).thenReturn(1);

        dao.editar(new Carteira(1, "Alice Nova", "OKX"));

        verify(statementMock).setString(1, "Alice Nova");
        verify(statementMock).setString(2, "OKX");
        verify(statementMock).setInt(3, 1);
    }

    @Test
    void editar_carteiraInexistente_lancaNoSuchElementException() throws SQLException {
        when(statementMock.executeUpdate()).thenReturn(0);

        assertThrows(NoSuchElementException.class, () -> dao.editar(new Carteira(99, "Ninguem", "X")));
    }

    @Test
    void editar_carteiraNula_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dao.editar(null));
    }

    @Test
    void excluir_carteiraSemMovimentacoes_removeComSucesso() throws SQLException {
        when(statementMock.executeUpdate()).thenReturn(1);

        dao.excluir(1);

        verify(statementMock).setInt(1, 1);
        verify(statementMock).executeUpdate();
    }

    @Test
    void excluir_carteiraInexistente_lancaNoSuchElementException() throws SQLException {
        when(statementMock.executeUpdate()).thenReturn(0);

        assertThrows(NoSuchElementException.class, () -> dao.excluir(99));
    }

    @Test
    void excluir_carteiraComMovimentacoesSql1451_lancaIllegalStateException() throws SQLException {
        when(statementMock.executeUpdate()).thenThrow(new SQLException("FK constraint fails", "23000", 1451));

        assertThrows(IllegalStateException.class, () -> dao.excluir(1));
    }

    @Test
    void excluir_idNulo_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dao.excluir(null));
    }
}