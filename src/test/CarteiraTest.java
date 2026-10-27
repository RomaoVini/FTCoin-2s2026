
package br.ftcoin.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarteiraTest {

    @Test
    void deveCriarCarteiraComDadosValidos() {
        Carteira carteira =
                new Carteira(1, "Ana Julia", "Binance");

        assertEquals(1, carteira.getId());
        assertEquals("Ana Julia", carteira.getTitular());
        assertEquals("Binance", carteira.getCorretora());
    }

    @Test
    void naoDeveAceitarIdentificadorInvalido() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Carteira(0, "Ana Julia", "Binance")
        );
    }

    @Test
    void naoDeveAceitarTitularVazio() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Carteira(1, " ", "Binance")
        );
    }

    @Test
    void naoDeveAceitarCorretoraVazia() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Carteira(1, "Ana Julia", "")
        );
    }

    @Test
    void devePermitirAlterarTitular() {
        Carteira carteira =
                new Carteira(1, "Ana Julia", "Binance");

        carteira.setTitular("Maria");

        assertEquals("Maria", carteira.getTitular());
    }

    @Test
    void devePermitirAlterarCorretora() {
        Carteira carteira =
                new Carteira(1, "Ana Julia", "Binance");

        carteira.setCorretora("Coinbase");

        assertEquals("Coinbase", carteira.getCorretora());
    }
}
