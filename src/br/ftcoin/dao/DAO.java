package br.ftcoin.dao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico base para operações fundamentais de persistência (CRUD)
 * seguindo o padrão Data Access Object (DAO).
 *
 * <p>As interfaces específicas de domínio (como {@link CarteiraDAO},
 * {@link MovimentacaoDAO} e {@link OraculoDAO}) estendem este contrato
 * aplicando o recurso de <i>Sealed Interfaces</i> para restringir suas
 * implementações concretas.</p>
 *
 * @param <T> Tipo da entidade gerenciada pelo DAO.
 * @param <K> Tipo da chave primária (identificador) da entidade.
 * @author Equipe FT_Coin
 * @version 2.0
 */
public interface DAO<T, K> {

    /**
     * Persiste uma nova entidade no repositório de dados.
     *
     * @param entidade Instância da entidade a ser incluída.
     */
    void incluir(T entidade);

    /**
     * Busca uma entidade específica a partir de seu identificador único.
     *
     * @param id Chave primária da entidade buscada.
     * @return Um {@link Optional} contendo a entidade encontrada, ou vazio caso não exista.
     */
    Optional<T> consultar(K id);

    /**
     * Recupera todas as entidades cadastradas no repositório.
     *
     * @return Lista contendo todas as instâncias armazenadas (pode ser vazia).
     */
    List<T> consultarTodos();

    /**
     * Atualiza os dados de uma entidade já existente no repositório.
     *
     * @param entidade Instância contendo os dados atualizados.
     */
    void editar(T entidade);

    /**
     * Remove uma entidade do repositório a partir de seu identificador único.
     *
     * @param id Chave primária da entidade a ser excluída.
     */
    void excluir(K id);
}