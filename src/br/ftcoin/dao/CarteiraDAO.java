package br.ftcoin.dao;

import br.ftcoin.model.Carteira;

import java.util.List;

/**
 * Interface selada (Sealed Interface) que define o contrato de persistência
 * para a entidade {@link Carteira}.
 *
 * <p>Estende as operações CRUD padrão de {@link DAO} utilizando {@link Integer}
 * como chave primária e restringe explicitamente as implementações permitidas
 * às camadas de memória e banco de dados MariaDB.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public sealed interface CarteiraDAO extends DAO<Carteira, Integer>
        permits CarteiraDAOMemoria, CarteiraDAOMariaDB {

    /**
     * Recupera todas as carteiras cadastradas ordenadas alfabeticamente
     * pelo nome do titular (ordem crescente, ignorando maiúsculas/minúsculas).
     *
     * @return Lista de {@link Carteira} ordenada por titular.
     */
    List<Carteira> consultarTodasOrdenadasPorTitular();
}