package test;

import br.ftcoin.dao.MovimentacaoDAOMariaDB;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.util.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MovimentacaoDAOMariaDBTest {

    private MockedStatic<DatabaseConnection> dbStaticMock;
    private DatabaseConnection dbInstanceMock;
    private Connection connectionMock;
    private PreparedStatement statementMock;
    private ResultSet resultSetMock;
    private MovimentacaoDAOMariaDB dao;

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
        when(connectionMock.prepareStatement(anyString(), anyInt())).thenReturn(statementMock);

        dao = new MovimentacaoDAOMariaDB();
    }

    @AfterEach
    void tearDown() {
        dbStaticMock.close();
    }

    @Test
    void incluir_movimentacaoValida_atribuiIdGeradoPeloBanco() throws SQLException {
        ResultSet generatedKeysMock = mock(ResultSet.class);
        when(statementMock.getGeneratedKeys()).thenReturn(generatedKeysMock);
        when(generatedKeysMock.next()).thenReturn(true);
        when(generatedKeysMock.getInt(1)).thenReturn(42);

        Movimentacao mov = new Movimentacao(0, 1, LocalDate.of(2026, 5, 10), TipoOperacao.COMPRA, new BigDecimal("1.500"));
        dao.incluir(mov);

        assertEquals(42, mov.getIdMovimento());
        verify(statementMock).executeUpdate();
    }

    @Test
    void incluir_movimentacaoNula_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dao.incluir(null));
    }

    @Test
    void incluir_carteiraInexistenteSql1452_lancaIllegalArgumentException() throws SQLException {
        when(statementMock.executeUpdate()).thenThrow(new SQLException("Cannot add child row", "23000", 1452));
        Movimentacao mov = new Movimentacao(0, 999, LocalDate.of(2026, 5, 10), TipoOperacao.COMPRA, BigDecimal.ONE);

        assertThrows(IllegalArgumentException.class, () -> dao.incluir(mov));
    }

    @Test
    void editar_movimentacaoExistente_atualizaComSucesso() throws SQLException {
        when(statementMock.executeUpdate()).thenReturn(1);
        Movimentacao mov = new Movimentacao(5, 1, LocalDate.of(2026, 6, 1), TipoOperacao.VENDA, new BigDecimal("0.500"));

        dao.editar(mov);

        verify(statementMock).setInt(5, 5);
        verify(statementMock).executeUpdate();
    }

    @Test
    void editar_movimentacaoInexistente_lancaNoSuchElementException() throws SQLException {
        when(statementMock.executeUpdate()).thenReturn(0);
        Movimentacao mov = new Movimentacao(99, 1, LocalDate.of(2026, 6, 1), TipoOperacao.VENDA, BigDecimal.ONE);

        assertThrows(NoSuchElementException.class, () -> dao.editar(mov));
    }

    @Test
    void editar_carteiraInexistenteSql1452_lancaIllegalArgumentException() throws SQLException {
        when(statementMock.executeUpdate()).thenThrow(new SQLException("FK fail", "23000", 1452));
        Movimentacao mov = new Movimentacao(5, 999, LocalDate.of(2026, 6, 1), TipoOperacao.VENDA, BigDecimal.ONE);

        assertThrows(IllegalArgumentException.class, () -> dao.editar(mov));
    }

    @Test
    void excluir_idExistente_removeComSucesso() throws SQLException {
        when(statementMock.executeUpdate()).thenReturn(1);

        dao.excluir(7);

        verify(statementMock).setInt(1, 7);
        verify(statementMock).executeUpdate();
    }

    @Test
    void excluir_idInexistente_lancaNoSuchElementException() throws SQLException {
        when(statementMock.executeUpdate()).thenReturn(0);

        assertThrows(NoSuchElementException.class, () -> dao.excluir(999));
    }

    @Test
    void consultar_idExistente_retornaMovimentacaoMapeada() throws SQLException {
        when(statementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getInt("IdMovimento")).thenReturn(7);
        when(resultSetMock.getInt("IdCarteira")).thenReturn(2);
        when(resultSetMock.getDate("Data")).thenReturn(Date.valueOf(LocalDate.of(2026, 3, 15)));
        when(resultSetMock.getString("TipoOperacao")).thenReturn("C");
        when(resultSetMock.getBigDecimal("Quantidade")).thenReturn(new BigDecimal("2.250"));

        Optional<Movimentacao> resultado = dao.consultar(7);

        assertTrue(resultado.isPresent());
        assertEquals(7, resultado.get().getIdMovimento());
        assertEquals(TipoOperacao.COMPRA, resultado.get().getTipoOperacao());
        assertEquals(new BigDecimal("2.250"), resultado.get().getQuantidade());
    }

    @Test
    void consultarTodos_e_consultarPorCarteira_retornamListas() throws SQLException {
        when(statementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true, false, true, false);
        when(resultSetMock.getInt("IdMovimento")).thenReturn(1, 1);
        when(resultSetMock.getInt("IdCarteira")).thenReturn(3, 3);
        when(resultSetMock.getDate("Data")).thenReturn(Date.valueOf(LocalDate.of(2026, 4, 1)));
        when(resultSetMock.getString("TipoOperacao")).thenReturn("V");
        when(resultSetMock.getBigDecimal("Quantidade")).thenReturn(BigDecimal.TEN);

        List<Movimentacao> todas = dao.consultarTodos();
        List<Movimentacao> daCarteira = dao.consultarPorCarteira(3);

        assertEquals(1, todas.size());
        assertEquals(1, daCarteira.size());
        assertEquals(TipoOperacao.VENDA, daCarteira.get(0).getTipoOperacao());
    }
}