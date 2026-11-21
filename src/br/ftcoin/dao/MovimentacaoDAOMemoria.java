package br.ftcoin.dao;

import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Movimentacao;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Implementação em memória do repositório de movimentações.
 *
 * <p>Os dados ficam disponíveis somente enquanto a instância existir; novos
 * identificadores são atribuídos automaticamente quando necessário.</p>
 */
public final class MovimentacaoDAOMemoria implements MovimentacaoDAO {

    /** Armazenamento das movimentações, indexadas pelo identificador e mantidas em ordem crescente de ID. */
    private final Map<Integer, Movimentacao> armazenamento = new TreeMap<>();

    /** Próximo identificador a ser atribuído (simula o {@code AUTO_INCREMENT} do banco). */
    private int proximoId = 1;

    /**
     * Armazena uma movimentação e atribui um identificador se ela ainda não tiver um.
     *
     * @param movimentacao movimentação a incluir
     * @throws ValidacaoException se a movimentação for nula ou o identificador já existir
     */
    @Override
    public void incluir(Movimentacao movimentacao){
        if(movimentacao == null){
            throw new ValidacaoException("transaction.error.required");
        }

        int id = movimentacao.getIdMovimento();
        if(id == 0){//simula o AUTO_INCREMENT do banco
            id = proximoId;
            movimentacao.setIdMovimento(id);
        } else if (armazenamento.containsKey(id)){
            throw new ValidacaoException("transaction.error.duplicateId", String.valueOf(id));
        }

        armazenamento.put(id, movimentacao);
        proximoId = Math.max(proximoId, id + 1);
    }

    /**
     * Busca uma movimentação pelo identificador.
     *
     * @param id identificador da movimentação
     * @return movimentação encontrada, ou {@link Optional#empty()} se não existir
     */
    @Override
    public Optional<Movimentacao> consultar(Integer id) {
        return Optional.ofNullable(armazenamento.get(id));
    }

    /**
     * Retorna todas as movimentações armazenadas em uma nova lista.
     *
     * @return cópia da lista de movimentações
     */
    @Override
    public List<Movimentacao> consultarTodos(){
        return new ArrayList<>(armazenamento.values());//devolve uma cópia da lista, para não permitir alterações externas
    }

    /**
     * Busca as movimentações de uma carteira em ordem cronológica e, em caso de
     * empate na data, por identificador crescente.
     *
     * @param idCarteira identificador da carteira
     * @return movimentações associadas à carteira
     */
    @Override
    public List<Movimentacao> consultarPorCarteira(int idCarteira) {
        List<Movimentacao> resultado = new ArrayList<>();
        for (Movimentacao m : armazenamento.values()) {
            if (m.getIdCarteira() == idCarteira) {
                resultado.add(m);
            }
        }
        resultado.sort(Comparator.comparing(Movimentacao::getDataOperacao).thenComparingInt(Movimentacao::getIdMovimento));
        return resultado;
    }

    /**
     * Substitui uma movimentação existente pelo mesmo identificador.
     *
     * @param movimentacao movimentação com os dados atualizados
     * @throws ValidacaoException se a movimentação for nula ou não estiver cadastrada
     */
    @Override
    public void editar(Movimentacao movimentacao) {
        if (movimentacao == null) {
            throw new ValidacaoException("transaction.error.required");
        }
        int id = movimentacao.getIdMovimento();
        if (!armazenamento.containsKey(id)) {
            throw new ValidacaoException("transaction.error.notFound", String.valueOf(id));
        }
        armazenamento.put(id, movimentacao);
    }

    /**
     * Remove a movimentação identificada.
     *
     * @param id identificador da movimentação
     * @throws ValidacaoException se não houver movimentação com esse identificador
     */
    @Override
    public void excluir(Integer id) {
        if (armazenamento.remove(id) == null) {
            throw new ValidacaoException("transaction.error.notFound", String.valueOf(id));
        }
    }

}