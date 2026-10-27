
package br.ftcoin.dao.memory;

import br.ftcoin.model.Carteira;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class CarteiraDAOMemoriaTest {

    private CarteiraDAOMemoria dao;

    @BeforeEach
    void setUp() {
        dao = new CarteiraDAOMemoria();
    }

    @Test
    void deveIncluirCarteira() {
        Carteira carteira =
                new Carteira(1, "Ana Julia", "Binance");

        dao.incluir(carteira);

        assertEquals(carteira, dao.consultar(1).orElseThrow());
    }

    @Test
    void naoDeveIncluirIdentificadorDuplicado() {
        dao.incluir(
            new Carteira(1, "Ana Julia", "Binance")
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> dao.incluir(
                new Carteira(1, "Maria", "Coinbase")
            )
        );
    }

    @Test
    void deveRetornarCarteiraCadastrada() {
        Carteira carteira =
                new Carteira(1, "Ana Julia", "Binance");

        dao.incluir(carteira);

        assertTrue(dao.consultar(1).isPresent());
        assertEquals("Ana Julia",
                dao.consultar(1).orElseThrow().getTitular());
    }

    @Test
    void deveRetornarVazioParaCarteiraInexistente() {
        assertTrue(dao.consultar(99).isEmpty());
    }

    @Test
    void deveConsultarTodasAsCarteiras() {
        dao.incluir(
            new Carteira(1, "Ana Julia", "Binance")
        );
        dao.incluir(
            new Carteira(2, "Maria", "Coinbase")
        );

        assertEquals(2, dao.consultarTodos().size());
    }

    @Test
    void deveEditarCarteiraExistente() {
        dao.incluir(
            new Carteira(1, "Ana Julia", "Binance")
        );

        Carteira atualizada =
                new Carteira(1, "Ana", "Coinbase");

        dao.editar(atualizada);

        assertEquals(
            "Ana",
            dao.consultar(1).orElseThrow().getTitular()
        );
        assertEquals(
            "Coinbase",
            dao.consultar(1).orElseThrow().getCorretora()
        );
    }

    @Test
    void naoDeveEditarCarteiraInexistente() {
        assertThrows(
            NoSuchElementException.class,
            () -> dao.editar(
                new Carteira(99, "Ana Julia", "Binance")
            )
        );
    }

    @Test
    void deveExcluirCarteiraExistente() {
        dao.incluir(
            new Carteira(1, "Ana Julia", "Binance")
        );

        dao.excluir(1);

        assertTrue(dao.consultar(1).isEmpty());
    }

    @Test
    void naoDeveExcluirCarteiraInexistente() {
        assertThrows(
            NoSuchElementException.class,
            () -> dao.excluir(99)
        );
    }
}
