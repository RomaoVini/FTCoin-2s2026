package br.ftcoin.dao;

import java.util.List;
import java.util.Optional;

/**
 * Define as operações genéricas de acesso a dados.
 *
 * @param <T> tipo da entidade
 * @param <K> tipo do identificador
 */
public sealed interface DAO<T, K>
        permits CarteiraDAO, MovimentacaoDAO {

    void incluir(T entidade);
    Optional<T> consultar(K id);
    List<T> consultarTodos();
    void editar(T entidade);
    void excluir(K id);
}
