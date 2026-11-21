package br.ftcoin.dao;

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
     *
     * @return Nova instância de {@link CarteiraDAOMemoria}.
     */
    @Override
    public CarteiraDAO getCarteiraDAO() {
        return new CarteiraDAOMemoria();
    }

    /**
     * {@inheritDoc}
     *
     * @return Nova instância de {@link MovimentacaoDAOMemoria}.
     */
    @Override
    public MovimentacaoDAO getMovimentacaoDAO() {
        return new MovimentacaoDAOMemoria();
    }
}