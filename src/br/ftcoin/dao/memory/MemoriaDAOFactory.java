package br.ftcoin.dao.memory;

import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.dao.DAOFactory;
import br.ftcoin.dao.MovimentacaoDAO;
import br.ftcoin.dao.OraculoDAO;

public class MemoriaDAOFactory extends DAOFactory {
    @Override
    public CarteiraDAO getCarteiraDAO() {
        return new CarteiraDAOMemoria();
    }

    @Override
    public MovimentacaoDAO getMovimentacaoDAO() {
        return new MovimentacaoDAOMemoria();
    }

    @Override
    public OraculoDAO getOraculoDAO() {
        return new OraculoDAOMemoria();
    }
}
