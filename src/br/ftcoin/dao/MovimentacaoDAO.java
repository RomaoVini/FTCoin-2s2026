package br.ftcoin.dao;

import br.ftcoin.model.Movimentacao;

import java.util.List;

public interface MovimentacaoDAO extends DAO<Movimentacao, Integer> {
    List<Movimentacao> consultarPorCarteira(int idCarteira);
}
