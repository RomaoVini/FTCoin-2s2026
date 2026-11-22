package test;

import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Movimentacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes da entidade {@link Movimentacao}: criação, validações dos campos e igualdade.
 */
class MovimentacaoTest {

    private static final LocalDate DATA = LocalDate.of(2026, 2, 1);
    private static final BigDecimal MIL = new BigDecimal("1000");

    @Test
    void criaMovimentacaoValidaComOsDadosInformados() {
        Movimentacao m = new Movimentacao(7, 3, DATA, TipoOperacao.COMPRA, MIL);

        assertEquals(7, m.getIdMovimento());
        assertEquals(3, m.getIdCarteira());
        assertEquals(DATA, m.getDataOperacao());
        assertEquals(TipoOperacao.COMPRA, m.getTipoOperacao());
        assertEquals(0, MIL.compareTo(m.getQuantidade()));
    }

    @Test
    void construtorSemIdIniciaOIdDoMovimentoComoZero() {
        Movimentacao m = new Movimentacao(3, DATA, TipoOperacao.VENDA, MIL);

        assertEquals(0, m.getIdMovimento());
    }

    @Test
    void construtorComCodigoDeTipoAceitaCEVEmQualquerCaixa() {
        assertEquals(TipoOperacao.COMPRA, new Movimentacao(1, 1, DATA, 'C', MIL).getTipoOperacao());
        assertEquals(TipoOperacao.VENDA, new Movimentacao(1, 1, DATA, 'v', MIL).getTipoOperacao());
    }

    @Test
    void codigoDeTipoDesconhecidoEhRejeitado() {
        assertThrows(ValidacaoException.class, () -> new Movimentacao(1, 1, DATA, 'X', MIL));
    }

    @Test
    void rejeitaIdDeCarteiraZeroOuNegativo() {
        assertThrows(ValidacaoException.class, () -> new Movimentacao(0, DATA, TipoOperacao.COMPRA, MIL));
        assertThrows(ValidacaoException.class, () -> new Movimentacao(-5, DATA, TipoOperacao.COMPRA, MIL));
    }

    @Test
    void rejeitaIdDeMovimentoNegativo() {
        assertThrows(ValidacaoException.class, () -> new Movimentacao(-1, 1, DATA, TipoOperacao.COMPRA, MIL));
    }

    @Test
    void rejeitaDataNula() {
        assertThrows(ValidacaoException.class, () -> new Movimentacao(1, null, TipoOperacao.COMPRA, MIL));
    }

    @Test
    void rejeitaDataFutura() {
        assertThrows(ValidacaoException.class,
                () -> new Movimentacao(1, LocalDate.now().plusDays(1), TipoOperacao.COMPRA, MIL));
    }

    @Test
    void rejeitaTipoNulo() {
        assertThrows(ValidacaoException.class, () -> new Movimentacao(1, DATA, (TipoOperacao) null, MIL));
    }

    @Test
    void rejeitaQuantidadeNulaZeroOuNegativa() {
        assertThrows(ValidacaoException.class, () -> new Movimentacao(1, DATA, TipoOperacao.COMPRA, null));
        assertThrows(ValidacaoException.class, () -> new Movimentacao(1, DATA, TipoOperacao.COMPRA, BigDecimal.ZERO));
        assertThrows(ValidacaoException.class, () -> new Movimentacao(1, DATA, TipoOperacao.COMPRA, new BigDecimal("-1")));
    }

    @Test
    void settersTambemValidamOsValores() {
        Movimentacao m = new Movimentacao(1, DATA, TipoOperacao.COMPRA, MIL);

        assertThrows(ValidacaoException.class, () -> m.setQuantidade(BigDecimal.ZERO));
        assertThrows(ValidacaoException.class, () -> m.setDataOperacao(null));
        assertThrows(ValidacaoException.class, () -> m.setIdCarteira(0));
        assertEquals(0, MIL.compareTo(m.getQuantidade())); // valor original preservado
    }

    @Test
    void isCompraEIsVendaRefletemOTipoDaOperacao() {
        Movimentacao compra = new Movimentacao(1, DATA, TipoOperacao.COMPRA, MIL);
        Movimentacao venda = new Movimentacao(1, DATA, TipoOperacao.VENDA, MIL);

        assertTrue(compra.isCompra());
        assertFalse(compra.isVenda());
        assertTrue(venda.isVenda());
        assertFalse(venda.isCompra());
    }

    @Test
    void igualdadeConsideraApenasOIdDoMovimento() {
        Movimentacao a = new Movimentacao(10, 1, DATA, TipoOperacao.COMPRA, MIL);
        Movimentacao b = new Movimentacao(10, 2, DATA.plusDays(1), TipoOperacao.VENDA, BigDecimal.ONE);
        Movimentacao c = new Movimentacao(11, 1, DATA, TipoOperacao.COMPRA, MIL);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}