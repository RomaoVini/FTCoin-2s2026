package test;

import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.dao.MovimentacaoDAOMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes do {@link MovimentacaoDAOMemoria}: inclusão com identificador automático,
 * consultas, edição e exclusão.
 */
class MovimentacaoDAOMemoriaTest {

    private MovimentacaoDAOMemoria dao;

    @BeforeEach
    void setUp() {
        dao = new MovimentacaoDAOMemoria();
    }

    @Test
    void inclusaoSemIdGeraIdentificadoresSequenciais() {
        Movimentacao a = movimentacao(1, "2026-02-01", TipoOperacao.COMPRA);
        Movimentacao b = movimentacao(1, "2026-02-02", TipoOperacao.VENDA);

        dao.incluir(a);
        dao.incluir(b);

        assertEquals(1, a.getIdMovimento());
        assertEquals(2, b.getIdMovimento());
    }

    @Test
    void inclusaoComIdInformadoMantemOIdEContinuaDepoisDele() {
        Movimentacao fixa = new Movimentacao(10, 1, LocalDate.of(2026, 2, 1), TipoOperacao.COMPRA, BigDecimal.TEN);
        Movimentacao automatica = movimentacao(1, "2026-02-02", TipoOperacao.COMPRA);

        dao.incluir(fixa);
        dao.incluir(automatica);

        assertEquals(10, fixa.getIdMovimento());
        assertEquals(11, automatica.getIdMovimento());
    }

    @Test
    void inclusaoComIdDuplicadoLancaExcecao() {
        dao.incluir(new Movimentacao(5, 1, LocalDate.of(2026, 2, 1), TipoOperacao.COMPRA, BigDecimal.TEN));

        assertThrows(ValidacaoException.class,
                () -> dao.incluir(new Movimentacao(5, 1, LocalDate.of(2026, 2, 2), TipoOperacao.COMPRA, BigDecimal.TEN)));
    }

    @Test
    void inclusaoDeMovimentacaoNulaLancaExcecao() {
        assertThrows(ValidacaoException.class, () -> dao.incluir(null));
    }

    @Test
    void consultaPorIdentificadorExistenteEInexistente() {
        Movimentacao m = movimentacao(1, "2026-02-01", TipoOperacao.COMPRA);
        dao.incluir(m);

        assertTrue(dao.consultar(m.getIdMovimento()).isPresent());
        assertFalse(dao.consultar(999).isPresent());
    }

    @Test
    void consultaPorCarteiraFiltraEOrdenaPorDataEDepoisPorId() {
        dao.incluir(movimentacao(1, "2026-02-05", TipoOperacao.VENDA));
        dao.incluir(movimentacao(2, "2026-02-01", TipoOperacao.COMPRA)); // outra carteira
        dao.incluir(movimentacao(1, "2026-02-01", TipoOperacao.COMPRA));
        dao.incluir(movimentacao(1, "2026-02-01", TipoOperacao.VENDA)); // mesmo dia: id desempata

        List<Movimentacao> lista = dao.consultarPorCarteira(1);

        assertEquals(3, lista.size());
        assertEquals(LocalDate.of(2026, 2, 1), lista.get(0).getDataOperacao());
        assertTrue(lista.get(0).isCompra());
        assertTrue(lista.get(1).isVenda());
        assertEquals(LocalDate.of(2026, 2, 5), lista.get(2).getDataOperacao());
    }

    @Test
    void consultaPorCarteiraSemMovimentacoesRetornaListaVazia() {
        dao.incluir(movimentacao(2, "2026-02-01", TipoOperacao.COMPRA));

        assertTrue(dao.consultarPorCarteira(1).isEmpty());
    }

    @Test
    void consultarTodosDevolveUmaCopiaDoArmazenamento() {
        dao.incluir(movimentacao(1, "2026-02-01", TipoOperacao.COMPRA));

        dao.consultarTodos().clear();

        assertEquals(1, dao.consultarTodos().size());
    }

    @Test
    void edicaoSubstituiAMovimentacaoExistente() {
        Movimentacao m = movimentacao(1, "2026-02-01", TipoOperacao.COMPRA);
        dao.incluir(m);

        m.setQuantidade(new BigDecimal("77"));
        dao.editar(m);

        assertEquals(0, new BigDecimal("77").compareTo(dao.consultar(m.getIdMovimento()).get().getQuantidade()));
    }

    @Test
    void edicaoDeMovimentacaoInexistenteLancaExcecao() {
        Movimentacao inexistente = new Movimentacao(42, 1, LocalDate.of(2026, 2, 1), TipoOperacao.COMPRA, BigDecimal.TEN);

        assertThrows(ValidacaoException.class, () -> dao.editar(inexistente));
        assertThrows(ValidacaoException.class, () -> dao.editar(null));
    }

    @Test
    void exclusaoRemoveAMovimentacaoEExcluirInexistenteLancaExcecao() {
        Movimentacao m = movimentacao(1, "2026-02-01", TipoOperacao.COMPRA);
        dao.incluir(m);

        dao.excluir(m.getIdMovimento());

        assertTrue(dao.consultarTodos().isEmpty());
        assertThrows(ValidacaoException.class, () -> dao.excluir(m.getIdMovimento()));
    }

    private static Movimentacao movimentacao(int idCarteira, String data, TipoOperacao tipo) {
        return new Movimentacao(idCarteira, LocalDate.parse(data), tipo, new BigDecimal("100"));
    }
}
