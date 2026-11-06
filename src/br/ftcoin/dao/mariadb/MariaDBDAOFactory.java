
package br.ftcoin.dao.mariadb;

import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.dao.DAOFactory;
import br.ftcoin.dao.MovimentacaoDAO;
import br.ftcoin.dao.OraculoDAO;

public class MariaDBDAOFactory extends DAOFactory {
    @Override
    public CarteiraDAO getCarteiraDAO() {
        return new CarteiraDAOMariaDB();
    }

    @Override
    public MovimentacaoDAO getMovimentacaoDAO() {
        return new MovimentacaoDAOMariaDB();
    }

    @Override
    public OraculoDAO getOraculoDAO() {
        return new OraculoDAOMariaDB();
    }
}