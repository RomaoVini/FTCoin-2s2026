package br.ftcoin.controller;

import br.ftcoin.dao.CarteiraDAOMemoria;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Carteira;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes do {@link CarteiraController}: cadastro, consulta, edição e exclusão de
 * carteiras, com as validações e mensagens de erro tratadas.
 */
class CarteiraControllerTest {

    private CarteiraDAOMemoria dao;
    private CarteiraController controller;

    @BeforeEach
    void setUp() {
        dao = new CarteiraDAOMemoria();
        controller = new CarteiraController(dao);
    }

    @Test
    void cadastraCarteiraEGuardaNoDaoRemovendoEspacosDasBordas() {
        Carteira c = controller.cadastrar(1, "  Ana Lima ", " Corretora X  ");

        assertEquals("Ana Lima", c.getTitular());
        assertEquals("Corretora X", c.getCorretora());
        assertTrue(dao.consultar(1).isPresent());
    }

    @Test
    void cadastroComIdentificadorRepetidoLancaExcecaoDeDominio() {
        controller.cadastrar(1, "Ana", "X");

        assertThrows(ValidacaoException.class, () -> controller.cadastrar(1, "Bruno", "Y"));
        assertEquals(1, controller.listarTodas().size());
    }

    @Test
    void cadastroComIdentificadorInvalidoLancaExcecao() {
        assertThrows(ValidacaoException.class, () -> controller.cadastrar(0, "Ana", "X"));
        assertThrows(ValidacaoException.class, () -> controller.cadastrar(-3, "Ana", "X"));
    }

    @Test
    void cadastroSemTitularOuSemCorretoraLancaExcecao() {
        assertThrows(ValidacaoException.class, () -> controller.cadastrar(1, "  ", "X"));
        assertThrows(ValidacaoException.class, () -> controller.cadastrar(1, "Ana", ""));
        assertThrows(ValidacaoException.class, () -> controller.cadastrar(1, null, "X"));
        assertTrue(controller.listarTodas().isEmpty());
    }

    @Test
    void consultaPorIdentificadorEncontraACarteira() {
        controller.cadastrar(4, "Ana", "X");

        assertEquals("Ana", controller.buscarPorId(4).getTitular());
    }

    @Test
    void consultaDeCarteiraInexistenteLancaExcecaoDeDominio() {
        assertThrows(ValidacaoException.class, () -> controller.buscarPorId(99));
        assertThrows(ValidacaoException.class, () -> controller.buscarPorId(0));
    }

    @Test
    void edicaoAlteraTitularECorretoraMantendoOIdentificador() {
        controller.cadastrar(1, "Ana", "X");

        Carteira editada = controller.atualizar(1, "Ana Maria", "Corretora Nova");

        assertEquals(1, editada.getId());
        assertEquals("Ana Maria", controller.buscarPorId(1).getTitular());
        assertEquals("Corretora Nova", controller.buscarPorId(1).getCorretora());
    }

    @Test
    void edicaoDeCarteiraInexistenteLancaExcecao() {
        assertThrows(ValidacaoException.class, () -> controller.atualizar(99, "Ana", "X"));
    }

    @Test
    void exclusaoRemoveACarteira() {
        controller.cadastrar(1, "Ana", "X");

        controller.excluir(1);

        assertTrue(controller.listarTodas().isEmpty());
    }

    @Test
    void exclusaoDeCarteiraInexistenteLancaExcecaoSemAlterarAsDemais() {
        controller.cadastrar(1, "Ana", "X");

        assertThrows(ValidacaoException.class, () -> controller.excluir(5));
        assertEquals(1, controller.listarTodas().size());
    }

    @Test
    void depoisDeExcluirNaoEhMaisPossivelConsultarNemEditar() {
        controller.cadastrar(5, "Ana", "X");
        controller.excluir(5);

        assertThrows(ValidacaoException.class, () -> controller.buscarPorId(5));
        assertThrows(ValidacaoException.class, () -> controller.atualizar(5, "Ana", "X"));
    }

    @Test
    void listagemVemEmOrdemDeIdentificador() {
        controller.cadastrar(10, "Bruno", "X");
        controller.cadastrar(2, "Carla", "X");
        controller.cadastrar(1, "Ana", "X");

        List<Carteira> lista = controller.listarTodas();

        assertEquals(1, lista.get(0).getId());
        assertEquals(2, lista.get(1).getId());
        assertEquals(10, lista.get(2).getId());
    }
}