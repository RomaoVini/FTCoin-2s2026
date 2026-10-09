package br.unicamp.ft.ftcoin.dao;

import java.util.List;
import java.util.Optional;

public interface DAO<T, K> {
    
    void incluir(T entidade);
    
    Optional<T> consultar(K id);
    
    List<T> consultarTodos();
    
    void editar(T entidade);
    
    void excluir(K id);
}
