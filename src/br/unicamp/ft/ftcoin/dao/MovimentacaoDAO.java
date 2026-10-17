package br.unicamp.ft.ftcoin.dao;

import br.unicamp.ft.ftcoin.model.Movimentacao;

import java.util.List;

public interface MovimentacaoDAO extends DAO<Movimentacao, Integer> {
    List<Movimentacao> consultarPorCarteira(int idCarteira);
}
