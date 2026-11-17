package br.ftcoin.dao;

import br.ftcoin.model.Carteira;
import br.ftcoin.util.I18n;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Implementação concreta de {@link CarteiraDAO} para persistência volátil em memória local
 * utilizando {@link LinkedHashMap}.
 * Declarada como {@code final} por integrar a hierarquia selada de {@link CarteiraDAO}.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class CarteiraDAOMemoria implements CarteiraDAO {

    private final Map<Integer, Carteira> carteiras = new LinkedHashMap<>();

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se a carteira for nula ou se o ID já estiver cadastrado.
     */
    @Override
    public void incluir(Carteira carteira) {
        if (carteira == null) {
            throw new IllegalArgumentException(I18n.get("wallet.error.null"));
        }

        int id = carteira.getId();

        if (carteiras.containsKey(id)) {
            throw new IllegalArgumentException(
                    I18n.getComParametros("wallet.error.duplicateId", String.valueOf(id)));
        }

        carteiras.put(id, carteira);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se o {@code id} informado for nulo.
     */
    @Override
    public Optional<Carteira> consultar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(I18n.get("wallet.error.nullId"));
        }

        return Optional.ofNullable(carteiras.get(id));
    }

    /**
     * {@inheritDoc}
     * <p>Retorna as carteiras ordenadas de forma crescente pelo seu identificador (ID),
     * espelhando o comportamento da consulta relacional no MariaDB.</p>
     */
    @Override
    public List<Carteira> consultarTodos() {
        List<Carteira> lista = new ArrayList<>(carteiras.values());
        lista.sort(Comparator.comparingInt(Carteira::getId));
        return lista;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Carteira> consultarTodasOrdenadasPorTitular() {
        List<Carteira> lista = new ArrayList<>(carteiras.values());
        lista.sort(Comparator.comparing(Carteira::getTitular, String.CASE_INSENSITIVE_ORDER));
        return lista;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se a carteira for nula.
     * @throws NoSuchElementException   se nenhuma carteira com o ID especificado for encontrada.
     */
    @Override
    public void editar(Carteira carteira) {
        if (carteira == null) {
            throw new IllegalArgumentException(I18n.get("wallet.error.null"));
        }

        int id = carteira.getId();

        if (!carteiras.containsKey(id)) {
            throw new NoSuchElementException(
                    I18n.getComParametros("wallet.error.notFound", String.valueOf(id)));
        }

        carteiras.put(id, carteira);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se o {@code id} informado for nulo.
     * @throws NoSuchElementException   se a carteira não existir no mapa em memória.
     */
    @Override
    public void excluir(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(I18n.get("wallet.error.nullId"));
        }

        if (!carteiras.containsKey(id)) {
            throw new NoSuchElementException(
                    I18n.getComParametros("wallet.error.notFound", String.valueOf(id)));
        }

        carteiras.remove(id);
    }
}