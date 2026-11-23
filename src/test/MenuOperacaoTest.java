package test;

import br.ftcoin.enums.MenuOperacao;
import br.ftcoin.exception.ValidacaoException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuOperacaoTest {

    @Test
    void fromCodigo_codigosValidos_retornaConstantesCorretas() {
        assertEquals(MenuOperacao.CARTEIRA, MenuOperacao.fromCodigo(1));
        assertEquals(MenuOperacao.MOVIMENTACAO, MenuOperacao.fromCodigo(2));
        assertEquals(MenuOperacao.RELATORIOS, MenuOperacao.fromCodigo(3));
        assertEquals(MenuOperacao.AJUDA, MenuOperacao.fromCodigo(4));
        assertEquals(MenuOperacao.IDIOMA, MenuOperacao.fromCodigo(5));
        assertEquals(MenuOperacao.SAIR, MenuOperacao.fromCodigo(0));
    }

    @Test
    void fromCodigo_codigoInvalido_lancaValidacaoException() {
        assertThrows(ValidacaoException.class, () -> MenuOperacao.fromCodigo(99));
        assertThrows(ValidacaoException.class, () -> MenuOperacao.fromCodigo(-1));
    }

    @Test
    void obterChavesMenuPrincipal_retornaCincoOpcoesSemSair() {
        List<String> chaves = MenuOperacao.obterChavesMenuPrincipal();

        assertEquals(5, chaves.size());
        assertFalse(chaves.contains(MenuOperacao.SAIR.getChaveI18n()));
        assertEquals("mainMenu.wallet", chaves.get(0));
        assertEquals("mainMenu.language", chaves.get(4));
    }
}