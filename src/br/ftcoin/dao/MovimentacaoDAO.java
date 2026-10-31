package br.ftcoin.dao;
import br.ftcoin.model.Movimentacao;
import java.util.List;

public sealed interface MovimentacaoDAO
        extends DAO<Movimentacao, Integer> {

    List<Movimentacao> consultarPorCarteira(int idCarteira);
}