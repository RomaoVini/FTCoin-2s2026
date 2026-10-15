import java.util.List;

public interface MovimentacaoDAO extends DAO<Movimentacao, Integer> {

    List<Movimentacao> consultarPorCarteira(int idCarteira);
}

public non-sealed interface MovimentacaoDAO extends DAO<Movimentacao, Integer> {
    List<Movimentacao> consultarPorCarteira(int idCarteira);
}
