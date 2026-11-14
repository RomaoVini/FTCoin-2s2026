package br.ftcoin.dao.memory;

import br.ftcoin.dao.*;

public class MemoriaDAOFactory extends DAOFactory {
    @Override
    public CarteiraDAO getCarteiraDAO() {
        return new CarteiraDAOMemoria();
    }

    @Override
    public MovimentacaoDAO getMovimentacaoDAO() {
        return new MovimentacaoDAOMemoria();
    }
}
