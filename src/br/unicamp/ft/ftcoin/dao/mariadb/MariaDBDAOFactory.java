package br.unicamp.ft.ftcoin.dao.mariadb;

import br.unicamp.ft.ftcoin.dao.*;

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