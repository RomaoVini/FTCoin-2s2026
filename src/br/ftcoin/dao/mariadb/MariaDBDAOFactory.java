
package br.ftcoin.dao.mariadb;
import br.ftcoin.dao.*;

public class MariaDBDAOFactory extends DAOFactory {
    @Override
    public CarteiraDAO getCarteiraDAO() {
        return new CarteiraDAOMariaDB(); //ta faltando a carteiradao
    }

    @Override
    public MovimentacaoDAO getMovimentacaoDAO() {
        return new MovimentacaoDAOMariaDB();
    }
}