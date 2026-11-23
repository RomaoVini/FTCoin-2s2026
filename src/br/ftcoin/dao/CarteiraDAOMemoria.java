package br.ftcoin.dao;

import br.ftcoin.model.Carteira;
import br.ftcoin.util.I18n;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementação concreta de {@link CarteiraDAO} para persistência volátil em memória local
 * utilizando {@link LinkedHashMap}.
 * É declarada como {@code final} por integrar a hierarquia selada de {@link CarteiraDAO}.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class CarteiraDAOMemoria implements CarteiraDAO {

    /** Armazenamento das carteiras em memória, indexadas pelo identificador e mantidas na ordem de inclusão. */
    private final Map<Integer, Carteira> carteiras = new LinkedHashMap<>();

    /** Repositório compartilhado das movimentações vinculadas às carteiras. */
    private final MovimentacaoDAO movimentacaoDAO;

    /**
     * Cria um DAO de carteiras com um repositório próprio de movimentações em memória.
     */
    public CarteiraDAOMemoria() {
        this(new MovimentacaoDAOMemoria());
    }

    /**
     * Cria um DAO de carteiras associado ao repositório de movimentações informado.
     *
     * @param movimentacaoDAO repositório que contém o histórico das carteiras
     * @throws NullPointerException se {@code movimentacaoDAO} for nulo
     */
    public CarteiraDAOMemoria(MovimentacaoDAO movimentacaoDAO) {
        this.movimentacaoDAO = Objects.requireNonNull(
                movimentacaoDAO, "MovimentacaoDAO não pode ser nulo.");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se a carteira for nula ou o ID já estiver cadastrado
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
     * @return a carteira encontrada, ou {@link Optional#empty()} se o ID não estiver cadastrado
     * @throws IllegalArgumentException se o {@code id} informado for nulo
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
     * em uma nova lista que não altera o mapa interno.</p>
     *
     * @return lista de carteiras ordenada pelo ID
     */
    @Override
    public List<Carteira> consultarTodos() {
        List<Carteira> lista = new ArrayList<>(carteiras.values());
        lista.sort(Comparator.comparingInt(Carteira::getId));
        return lista;
    }

    /**
     * {@inheritDoc}
     *
     * @return lista de carteiras ordenada pelo nome do titular, ignorando diferenças
     *         entre maiúsculas e minúsculas
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
     * @throws IllegalArgumentException se a carteira for nula
     * @throws NoSuchElementException se nenhuma carteira com o ID especificado for encontrada
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
     * @throws IllegalArgumentException se o {@code id} informado for nulo
     * @throws NoSuchElementException se a carteira não existir no mapa em memória
     * @throws IllegalStateException se a carteira possuir movimentações vinculadas
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

        if (!movimentacaoDAO.consultarPorCarteira(id).isEmpty()) {
            throw new IllegalStateException(
                    I18n.getComParametros("wallet.error.hasTransactions", String.valueOf(id)));
        }

        carteiras.remove(id);
    }
}