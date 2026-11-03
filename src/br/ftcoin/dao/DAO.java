package br.ftcoin.dao;

import java.util.List;
import java.util.Optional;

public sealed interface DAO<T, K>
        permits CarteiraDAO, MovimentacaoDAO {

    void incluir(T entidade);
    Optional<T> consultar(K id);
    List<T> consultarTodos();
    void editar(T entidade);
    void excluir(K id);
}