package br.ftcoin.dao;

/**
 * Fábrica concreta do padrão <b>Abstract Factory</b> responsável por prover
 * DAOs com persistência volátil em memória, garantindo que todos os
 * controladores compartilhem o mesmo estado.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class MemoriaDAOFactory extends DAOFactory {

    private final MovimentacaoDAO movimentacaoDAO = new MovimentacaoDAOMemoria();
    private final CarteiraDAO carteiraDAO = new CarteiraDAOMemoria(movimentacaoDAO);

    /**
     * {@inheritDoc}
     *
     * @return instância compartilhada de {@link CarteiraDAOMemoria}
     */
    @Override
    public CarteiraDAO getCarteiraDAO() {
        return carteiraDAO;
    }

    /**
     * {@inheritDoc}
     *
     * @return instância compartilhada de {@link MovimentacaoDAOMemoria}
     */
    @Override
    public MovimentacaoDAO getMovimentacaoDAO() {
        return movimentacaoDAO;
    }
}