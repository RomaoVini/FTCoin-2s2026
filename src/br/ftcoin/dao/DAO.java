package br.ftcoin.dao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico base para operações fundamentais de persistência (CRUD),
 * conforme o padrão Data Access Object (DAO).
 *
 * <p>As interfaces específicas de domínio, como {@link CarteiraDAO}
 * e {@link MovimentacaoDAO}, estendem este contrato e podem restringir
 * suas implementações concretas por meio de interfaces seladas.</p>
 *
 * @param <T> tipo da entidade gerenciada pelo DAO
 * @param <K> tipo da chave primária da entidade
 * @author Equipe FT_Coin
 * @version 2.0
 */
public interface DAO<T, K> {

    /**
     * Persiste uma nova entidade no repositório de dados.
     *
     * @param entidade entidade a ser incluída
     */
    void incluir(T entidade);

    /**
     * Busca uma entidade específica a partir de seu identificador único.
     *
     * @param id chave primária da entidade buscada
     * @return {@link Optional} contendo a entidade encontrada ou vazio se ela não existir
     */
    Optional<T> consultar(K id);

    /**
     * Recupera todas as entidades cadastradas no repositório.
     *
     * @return lista contendo todas as entidades armazenadas (pode ser vazia)
     */
    List<T> consultarTodos();

    /**
     * Atualiza os dados de uma entidade já existente no repositório.
     *
     * @param entidade entidade contendo os dados atualizados
     */
    void editar(T entidade);

    /**
     * Remove uma entidade do repositório a partir de seu identificador único.
     *
     * @param id chave primária da entidade a ser excluída
     */
    void excluir(K id);
}