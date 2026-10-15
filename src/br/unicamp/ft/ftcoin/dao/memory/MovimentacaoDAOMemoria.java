import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

//com persistência de memória os dados existem apenas enquanto a aplicação está rodando.
public class MovimentacaoDAOMemoria implements MovimentacaoDAO{

    //o TreeMap mantém as movimentações ordenadas pelo identificador
    private final Map<Integer, Movimentacao> armazenamento = new TreeMap<>();
    private int proximoId = 1;

    @Override
    public void incluir(Movimentacao movimentacao){
        if(movimentacao == null){
            throw new ValidacaoException("A movimentação é obrigatória.");
        }

        int id = movimentacao.getIdMovimento();
        if(id == 0){//simula o AUTO_INCREMENT do banco
            id = proximoId;
            movimentacao.setIdMovimento(id);
        } else if (armazenamento.containsKey(id)){
            throw new ValidacaoException("Já existe uma movimentação com o identificador " + id + ".");
        }

        armazenamento.put(id, movimentacao);
        proximoId = Math.max(proximoId, id + 1);
    }

    @Override
    public Optional<Movimentacao> consultar(Integer id) {
        return Optional.ofNullable(armazenamento.get(id));
    }

    @Override
    public List<Movimentacao> consultarTodos(){
        return new ArrayList<>(armazenamento.values());//devolve uma cópia da lista, para não permitir alterações externas
    }

    @Override
    public List<Movimentacao> consultarPorCarteira(int idCarteira) {
        List<Movimentacao> resultado = new ArrayList<>();
        for (Movimentacao m : armazenamento.values()) {
            if (m.getIdCarteira() == idCarteira) {
                resultado.add(m);
            }
        }
        resultado.sort(Comparator.comparing(Movimentacao::getDataOperacao)
                .thenComparingInt(Movimentacao::getIdMovimento));
        return resultado;
    }

    @Override
    public void editar(Movimentacao movimentacao) {
        if (movimentacao == null) {
            throw new ValidacaoException("A movimentação é obrigatória.");
        }
        int id = movimentacao.getIdMovimento();
        if (!armazenamento.containsKey(id)) {
            throw new ValidacaoException("Movimentação " + id + " não encontrada.");
        }
        armazenamento.put(id, movimentacao);
    }

    @Override
    public void excluir(Integer id) {
        if (armazenamento.remove(id) == null) {
            throw new ValidacaoException("Movimentação " + id + " não encontrada.");
        }
    }
}
