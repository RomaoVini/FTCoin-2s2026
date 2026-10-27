package br.ftcoin.dao;

import br.ftcoin.model.Movimentacao;
import br.ftcoin.dao.memory.MovimentacaoDAOMemoria;
import java.util.List;

public sealed interface MovimentacaoDAO
        extends DAO<Movimentacao, Integer>
        permits MovimentacaoDAOMemoria {
    List<Movimentacao> consultarPorCarteira(int idCarteira);
}