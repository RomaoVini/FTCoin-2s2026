package test;

import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.enums.TipoOperacao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes da enumeração {@link TipoOperacao}: conversão entre o código do banco
 * ('C' ou 'V') e o tipo da operação.
 */
class TipoOperacaoTest {

    @Test
    void codigosDosTiposSaoCParaCompraEVParaVenda() {
        assertEquals("C", String.valueOf(TipoOperacao.COMPRA.getCodigo()));
        assertEquals("V", String.valueOf(TipoOperacao.VENDA.getCodigo()));
    }

    @Test
    void converteCodigoEmTipoDeOperacao() {
        assertEquals(TipoOperacao.COMPRA, TipoOperacao.fromCodigo('C'));
        assertEquals(TipoOperacao.VENDA, TipoOperacao.fromCodigo('V'));
    }

    @Test
    void conversaoIgnoraMaiusculaEMinuscula() {
        assertEquals(TipoOperacao.COMPRA, TipoOperacao.fromCodigo('c'));
        assertEquals(TipoOperacao.VENDA, TipoOperacao.fromCodigo('v'));
    }

    @Test
    void codigoDesconhecidoLancaExcecaoDeDominio() {
        assertThrows(ValidacaoException.class, () -> TipoOperacao.fromCodigo('X'));
        assertThrows(ValidacaoException.class, () -> TipoOperacao.fromCodigo(' '));
    }

    @Test
    void toStringDevolveOCodigoDeUmaLetra() {
        assertEquals("C", TipoOperacao.COMPRA.toString());
        assertEquals("V", TipoOperacao.VENDA.toString());
    }
}
