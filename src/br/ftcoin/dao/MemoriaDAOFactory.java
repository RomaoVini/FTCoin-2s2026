package br.ftcoin.dao;

import br.ftcoin.dao.OraculoDAO;

/**
 * Fábrica concreta do padrão <b>Abstract Factory</b> responsável por instanciar
 * a família de DAOs com persistência volátil em memória local.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class MemoriaDAOFactory extends DAOFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public CarteiraDAO getCarteiraDAO() {
        return new CarteiraDAOMemoria();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MovimentacaoDAO getMovimentacaoDAO() {
        return new MovimentacaoDAOMemoria();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OraculoDAO getOraculoDAO() {
        return new OraculoDAOMemoria();
    }
}