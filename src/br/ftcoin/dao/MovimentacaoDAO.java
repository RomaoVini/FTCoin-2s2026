package br.ftcoin.dao;

import br.ftcoin.model.Movimentacao;

import java.util.List;

/**
 * Interface selada (Sealed Interface) que define o contrato de persistência
 * para a entidade {@link Movimentacao}.
 *
 * <p>Estende as operações CRUD padrão de {@link DAO} utilizando {@link Integer}
 * como chave primária e restringe explicitamente as implementações permitidas
 * às camadas de memória e banco de dados MariaDB.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public sealed interface MovimentacaoDAO extends DAO<Movimentacao, Integer>
        permits MovimentacaoDAOMemoria, MovimentacaoDAOMariaDB {

    /**
     * Recupera todo o histórico de movimentações financeiras (compras e vendas)
     * vinculadas a uma carteira específica.
     *
     * @param idCarteira Identificador único da carteira pesquisada.
     * @return Lista de {@link Movimentacao} pertencentes à carteira (pode ser vazia).
     */
    List<Movimentacao> consultarPorCarteira(int idCarteira);
}