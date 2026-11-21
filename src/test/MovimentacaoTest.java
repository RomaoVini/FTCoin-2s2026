package test;

import br.ftcoin.controller.MovimentacaoController;
import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.dao.CarteiraDAOMemoria;
import br.ftcoin.dao.MovimentacaoDAO;
import br.ftcoin.dao.MovimentacaoDAOMemoria;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Carteira;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.service.OraculoCotacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/*Testes unitários do Model Movimentacao e do MovimentacaoController.
 * DAOs e Oráculo são mocks: não há acesso a banco nem à rede.
 *
 * Os DAOs são interfaces seladas; por isso o mock é criado a partir da
 * implementação final em memória (mock maker inline do Mockito 5+),
 * mas a variável é tipada pela interface.*/
class MovimentacaoTest {

    private static final int ID = 1;
    private static final LocalDate DATA = LocalDate.of(2026, 11, 9);

    private CarteiraDAO carteiraDAO;
    private MovimentacaoDAO movDAO;
    private OraculoCotacao oraculo;
    private MovimentacaoController controller;

    @BeforeEach
    void setUp() {
        carteiraDAO = mock(CarteiraDAOMemoria.class);
        movDAO = mock(MovimentacaoDAOMemoria.class);
        oraculo = mock(OraculoCotacao.class);
        controller = new MovimentacaoController(carteiraDAO, movDAO, oraculo);
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private void carteiraExiste() {
        when(carteiraDAO.consultar(ID)).thenReturn(Optional.of(new Carteira(ID, "Ana", "Binance")));
    }

    private void historico(Movimentacao... movs) {
        when(movDAO.consultarPorCarteira(ID)).thenReturn(List.of(movs));
    }

    private Movimentacao compra(int id, String qtd) {
        return new Movimentacao(id, ID, DATA.minusDays(10), TipoOperacao.COMPRA, bd(qtd));
    }

    /*Model*/
    @Test
    void modelCriaMovimentacaoValida() {
        Movimentacao m = new Movimentacao(ID, DATA, TipoOperacao.COMPRA, bd("2.5"));

        assertEquals(0, m.getIdMovimento());
        assertEquals(ID, m.getIdCarteira());
        assertEquals(DATA, m.getDataOperacao());
        assertTrue(m.isCompra());
        assertFalse(m.isVenda());
        assertEquals(bd("2.5"), m.getQuantidade());
    }

    @Test
    void modelConverteCodigoCaractere() {
        assertTrue(new Movimentacao(1, ID, DATA, 'C', bd("1")).isCompra());
        assertTrue(new Movimentacao(2, ID, DATA, 'v', bd("1")).isVenda());
        assertThrows(ValidacaoException.class, () -> new Movimentacao(3, ID, DATA, 'X', bd("1")));
    }

    @Test
    void modelRejeitaDadosInvalidos() {
        assertThrows(ValidacaoException.class, () -> new Movimentacao(-1, ID, DATA, TipoOperacao.COMPRA, bd("1")));
        assertThrows(ValidacaoException.class, () -> new Movimentacao(0, DATA, TipoOperacao.COMPRA, bd("1")));
        assertThrows(ValidacaoException.class, () -> new Movimentacao(ID, null, TipoOperacao.COMPRA, bd("1")));
        assertThrows(ValidacaoException.class, () -> new Movimentacao(ID, DATA, (TipoOperacao) null, bd("1")));
    }

    @Test
    void modelRejeitaQuantidadeNulaZeroOuNegativa() {
        assertThrows(ValidacaoException.class, () -> new Movimentacao(ID, DATA, TipoOperacao.COMPRA, null));
        assertThrows(ValidacaoException.class, () -> new Movimentacao(ID, DATA, TipoOperacao.COMPRA, BigDecimal.ZERO));
        assertThrows(ValidacaoException.class, () -> new Movimentacao(ID, DATA, TipoOperacao.VENDA, bd("-0.01")));
    }

    @Test
    void modelSettersValidamEAtualizam() {
        Movimentacao m = new Movimentacao(ID, DATA, TipoOperacao.COMPRA, bd("1"));

        assertThrows(ValidacaoException.class, () -> m.setQuantidade(BigDecimal.ZERO));
        assertThrows(ValidacaoException.class, () -> m.setIdMovimento(-1));

        m.setQuantidade(bd("3"));
        m.setTipoOperacao(TipoOperacao.VENDA);
        assertEquals(bd("3"), m.getQuantidade());
        assertTrue(m.isVenda());
    }

    @Test
    void modelEqualsUsaApenasOId() {
        Movimentacao a = new Movimentacao(5, 1, DATA, TipoOperacao.COMPRA, bd("1"));
        Movimentacao b = new Movimentacao(5, 2, DATA.plusDays(3), TipoOperacao.VENDA, bd("9"));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Movimentacao(6, 1, DATA, TipoOperacao.COMPRA, bd("1")));
    }

    /*Compra*/
    @Test
    void compraComSucessoPersisteSemConsultarSaldoNemOraculo() {
        carteiraExiste();

        Movimentacao m = controller.cadastrar(ID, DATA, TipoOperacao.COMPRA, bd("10"));

        assertTrue(m.isCompra());
        assertEquals(bd("10"), m.getQuantidade());
        verify(movDAO).incluir(m);
        verify(movDAO, never()).consultarPorCarteira(anyInt());
        verifyNoInteractions(oraculo);
    }

    @Test
    void compraEmCarteiraInexistenteFalhaSemPersistir() {
        when(carteiraDAO.consultar(anyInt())).thenReturn(Optional.empty());

        assertThrows(ValidacaoException.class,
                () -> controller.cadastrar(99, DATA, TipoOperacao.COMPRA, bd("1")));
        verify(movDAO, never()).incluir(any());
    }

    @Test
    void compraComQuantidadeInvalidaFalhaSemPersistir() {
        carteiraExiste();

        assertThrows(ValidacaoException.class, () -> controller.cadastrar(ID, DATA, TipoOperacao.COMPRA, BigDecimal.ZERO));
        assertThrows(ValidacaoException.class, () -> controller.cadastrar(ID, DATA, TipoOperacao.COMPRA, bd("-3")));
        assertThrows(ValidacaoException.class, () -> controller.cadastrar(ID, DATA, TipoOperacao.COMPRA, null));
        verify(movDAO, never()).incluir(any());
    }

    @Test
    void compraComDataOuTipoNulosFalhaSemPersistir() {
        carteiraExiste();

        assertThrows(ValidacaoException.class, () -> controller.cadastrar(ID, null, TipoOperacao.COMPRA, bd("1")));
        assertThrows(ValidacaoException.class, () -> controller.cadastrar(ID, DATA, null, bd("1")));
        verify(movDAO, never()).incluir(any());
    }

    /*Venda*/
    @Test
    void vendaComSaldoSuficientePersiste() {
        carteiraExiste();
        historico(compra(1, "10"));

        Movimentacao m = controller.cadastrar(ID, DATA, TipoOperacao.VENDA, bd("4"));

        assertTrue(m.isVenda());
        verify(movDAO).incluir(m);
    }

    @Test
    void vendaDeTodoOSaldoEhPermitida() {
        carteiraExiste();
        historico(compra(1, "5"));

        assertDoesNotThrow(() -> controller.cadastrar(ID, DATA, TipoOperacao.VENDA, bd("5")));
        verify(movDAO).incluir(any(Movimentacao.class));
    }

    @Test
    void vendaComSaldoInsuficienteFalhaSemPersistir() {
        carteiraExiste();
        historico(compra(1, "2"));

        assertThrows(ValidacaoException.class,
                () -> controller.cadastrar(ID, DATA, TipoOperacao.VENDA, bd("3")));
        verify(movDAO, never()).incluir(any());
    }

    @Test
    void vendaSemHistoricoFalhaPorSaldoInsuficiente() {
        carteiraExiste();
        historico();

        assertThrows(ValidacaoException.class,
                () -> controller.cadastrar(ID, DATA, TipoOperacao.VENDA, bd("1")));
        verify(movDAO, never()).incluir(any());
    }

    @Test
    void vendaConsideraVendasAnterioresNoSaldo() {
        carteiraExiste();
        Movimentacao vendaAnterior = new Movimentacao(2, ID, DATA.minusDays(5), TipoOperacao.VENDA, bd("8"));
        historico(compra(1, "10"), vendaAnterior); // saldo = 2

        assertThrows(ValidacaoException.class,
                () -> controller.cadastrar(ID, DATA, TipoOperacao.VENDA, bd("3")));
        verify(movDAO, never()).incluir(any());
    }

    @Test
    void vendaEmCarteiraInexistenteOuQuantidadeNegativaNaoConsultaSaldo() {
        when(carteiraDAO.consultar(anyInt())).thenReturn(Optional.empty());
        assertThrows(ValidacaoException.class, () -> controller.cadastrar(99, DATA, TipoOperacao.VENDA, bd("1")));

        carteiraExiste();
        assertThrows(ValidacaoException.class, () -> controller.cadastrar(ID, DATA, TipoOperacao.VENDA, bd("-1")));

        verify(movDAO, never()).consultarPorCarteira(anyInt());
        verify(movDAO, never()).incluir(any());
    }

    /*Cotação*/
    @Test
    void consultarCotacaoDelegaAoOraculo() {
        Cotacao esperada = new Cotacao(DATA, bd("123.45"));
        when(oraculo.obterCotacao(DATA)).thenReturn(esperada);

        assertSame(esperada, controller.consultarCotacao(DATA));
        verify(oraculo, times(1)).obterCotacao(DATA);
    }

    @Test
    void consultarCotacaoComDataNulaNaoChamaOraculo() {
        assertThrows(NullPointerException.class, () -> controller.consultarCotacao(null));
        verifyNoInteractions(oraculo);
    }

    @Test
    void consultarCotacaoPropagaFalhaDoOraculo() {
        when(oraculo.obterCotacao(DATA)).thenThrow(new RuntimeException("sem cotação"));

        assertThrows(RuntimeException.class, () -> controller.consultarCotacao(DATA));
    }

    /*Consultas*/
    @Test
    void listagensDelegamAoDAO() {
        List<Movimentacao> lista = List.of(compra(1, "1"));
        when(movDAO.consultarTodos()).thenReturn(lista);
        when(movDAO.consultarPorCarteira(ID)).thenReturn(lista);

        assertSame(lista, controller.listarTodas());
        assertSame(lista, controller.listarPorCarteira(ID));
    }

    @Test
    void listarPorCarteiraComIdInvalidoFalhaSemConsultarDAO() {
        assertThrows(ValidacaoException.class, () -> controller.listarPorCarteira(0));
        verify(movDAO, never()).consultarPorCarteira(anyInt());
    }

    /*Construtor*/
    @Test
    void construtorRejeitaDependenciasNulas() {
        assertThrows(NullPointerException.class, () -> new MovimentacaoController(null, movDAO, oraculo));
        assertThrows(NullPointerException.class, () -> new MovimentacaoController(carteiraDAO, null, oraculo));
        assertThrows(NullPointerException.class, () -> new MovimentacaoController(carteiraDAO, movDAO, null));
    }
}