package br.ftcoin.dao;

import br.ftcoin.model.Carteira;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes da {@link DAOFactory}: escolha da persistência (memória ou MariaDB) a partir
 * do texto informado na linha de comando.
 */
class DAOFactoryTest {

    @Test
    void memoriaEhEscolhidaQuandoPedida() {
        assertTrue(DAOFactory.getFactory("MEMORIA") instanceof MemoriaDAOFactory);
    }

    @Test
    void mariaDbEhEscolhidoQuandoPedidoIgnorandoCaixaEEspacos() {
        assertTrue(DAOFactory.getFactory("MARIADB") instanceof MariaDBDAOFactory);
        assertTrue(DAOFactory.getFactory("  mariadb ") instanceof MariaDBDAOFactory);
    }

    @Test
    void textoNuloVazioOuDesconhecidoUsaMemoriaComoPadrao() {
        assertTrue(DAOFactory.getFactory(null) instanceof MemoriaDAOFactory);
        assertTrue(DAOFactory.getFactory("") instanceof MemoriaDAOFactory);
        assertTrue(DAOFactory.getFactory("oracle") instanceof MemoriaDAOFactory);
    }

    @Test
    void fabricaDeMemoriaEntregaDaosEmMemoriaFuncionais() {
        DAOFactory fabrica = DAOFactory.getFactory("MEMORIA");
        CarteiraDAO carteiras = fabrica.getCarteiraDAO();
        MovimentacaoDAO movimentacoes = fabrica.getMovimentacaoDAO();

        carteiras.incluir(new Carteira(1, "Ana", "X"));

        assertTrue(carteiras instanceof CarteiraDAOMemoria);
        assertTrue(movimentacoes instanceof MovimentacaoDAOMemoria);
        assertEquals(1, carteiras.consultarTodos().size());
        assertTrue(movimentacoes.consultarTodos().isEmpty());
    }
}