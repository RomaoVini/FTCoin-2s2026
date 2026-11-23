package test;

import br.ftcoin.util.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseConnectionTest {

    @AfterEach
    void resetSingleton() throws Exception {
        Field field = DatabaseConnection.class.getDeclaredField("instancia");
        field.setAccessible(true);
        field.set(null, null);
    }

    private DatabaseConnection criarInstanciaComConexaoMock(Connection conMock) throws Exception {
        sun.misc.Unsafe unsafe = obterUnsafe();
        DatabaseConnection dc = (DatabaseConnection) unsafe.allocateInstance(DatabaseConnection.class);
        Field conField = DatabaseConnection.class.getDeclaredField("conexao");
        conField.setAccessible(true);
        conField.set(dc, conMock);

        Field instField = DatabaseConnection.class.getDeclaredField("instancia");
        instField.setAccessible(true);
        instField.set(null, dc);
        return dc;
    }

    private sun.misc.Unsafe obterUnsafe() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (sun.misc.Unsafe) f.get(null);
    }

    @Test
    void getInstancia_conexaoAberta_retornaMesmaInstanciaSingleton() throws Exception {
        Connection conMock = mock(Connection.class);
        when(conMock.isClosed()).thenReturn(false);
        DatabaseConnection esperada = criarInstanciaComConexaoMock(conMock);

        DatabaseConnection atual1 = DatabaseConnection.getInstancia();
        DatabaseConnection atual2 = DatabaseConnection.getInstancia();

        assertSame(esperada, atual1);
        assertSame(atual1, atual2);
        assertSame(conMock, atual1.getConexao());
    }

    @Test
    void fechar_conexaoAberta_fechaConexaoComSucesso() throws Exception {
        Connection conMock = mock(Connection.class);
        when(conMock.isClosed()).thenReturn(false);
        DatabaseConnection dc = criarInstanciaComConexaoMock(conMock);

        dc.fechar();

        verify(conMock).close();
    }

    @Test
    void fechar_conexaoJaFechada_naoChamaCloseNovamente() throws Exception {
        Connection conMock = mock(Connection.class);
        when(conMock.isClosed()).thenReturn(true);
        DatabaseConnection dc = criarInstanciaComConexaoMock(conMock);

        dc.fechar();

        verify(conMock, never()).close();
    }
}