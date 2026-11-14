package br.ftcoin.dao;

import br.ftcoin.dao.OraculoDAO;

/**
 * Implementação concreta do padrão Abstract Factory ({@link DAOFactory})
 * responsável por instanciar a família de DAOs voltada à persistência
 * relacional no banco de dados MariaDB.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class MariaDBDAOFactory extends DAOFactory {

    /**
     * {@inheritDoc}
     *
     * @return Nova instância de {@link CarteiraDAOMariaDB}.
     */
    @Override
    public CarteiraDAO getCarteiraDAO() {
        return new CarteiraDAOMariaDB();
    }

    /**
     * {@inheritDoc}
     *
     * @return Nova instância de {@link MovimentacaoDAOMariaDB}.
     */
    @Override
    public MovimentacaoDAO getMovimentacaoDAO() {
        return new MovimentacaoDAOMariaDB();
    }

    /**
     * {@inheritDoc}
     *
     * @return Nova instância de {@link OraculoDAOMariaDB}.
     */
    @Override
    public OraculoDAO getOraculoDAO() {
        return new OraculoDAOMariaDB();
    }
}