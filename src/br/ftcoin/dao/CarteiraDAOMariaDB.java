package br.ftcoin.dao;

import br.ftcoin.model.Carteira;
import br.ftcoin.util.DatabaseConnection;
import br.ftcoin.util.I18n;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Implementação concreta de {@link CarteiraDAO} para persistência relacional
 * no banco de dados MariaDB via JDBC.
 * Declarada como {@code final} por integrar a hierarquia selada de {@link CarteiraDAO}.
 *
 * <p><b>Estrutura esperada da tabela no MariaDB:</b></p>
 * <pre>{@code
 * CREATE TABLE carteira (
 *     id_carteira INT PRIMARY KEY,
 *     titular     VARCHAR(100) NOT NULL,
 *     corretora   VARCHAR(100) NOT NULL
 * );
 * }</pre>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class CarteiraDAOMariaDB implements CarteiraDAO {

    /** Código de erro MariaDB/MySQL para violação de chave única (Duplicate entry). */
    private static final int ERRO_DUPLICADO = 1062;

    /** Código de erro MariaDB/MySQL para violação de integridade referencial (FK em uso). */
    private static final int ERRO_FK_REFERENCIADA = 1451;

    private static final String INSERT =
            "INSERT INTO carteira (id_carteira, titular, corretora) VALUES (?, ?, ?)";
    private static final String UPDATE =
            "UPDATE carteira SET titular = ?, corretora = ? WHERE id_carteira = ?";
    private static final String DELETE =
            "DELETE FROM carteira WHERE id_carteira = ?";
    private static final String SELECT_ID =
            "SELECT id_carteira, titular, corretora FROM carteira WHERE id_carteira = ?";
    private static final String SELECT_ALL =
            "SELECT id_carteira, titular, corretora FROM carteira ORDER BY id_carteira ASC";
    private static final String SELECT_ALL_BY_HOLDER =
            "SELECT id_carteira, titular, corretora FROM carteira ORDER BY titular ASC";

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se a carteira for nula ou o ID já existir no banco.
     */
    @Override
    public void incluir(Carteira carteira) {
        if (carteira == null) {
            throw new IllegalArgumentException(I18n.get("wallet.error.null"));
        }

        try {
            Connection con = DatabaseConnection.getInstancia().getConexao();
            try (PreparedStatement ps = con.prepareStatement(INSERT)) {
                ps.setInt(1, carteira.getId());
                ps.setString(2, carteira.getTitular());
                ps.setString(3, carteira.getCorretora());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERRO_DUPLICADO) {
                throw new IllegalArgumentException(
                        I18n.getComParametros("wallet.error.duplicateId", String.valueOf(carteira.getId())), e);
            }
            throw new RuntimeException(I18n.getComParametros("db.error.insert", e.getMessage()), e);
        }
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

        try {
            Connection con = DatabaseConnection.getInstancia().getConexao();
            try (PreparedStatement ps = con.prepareStatement(SELECT_ID)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.getComParametros("db.error.select", e.getMessage()), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Carteira> consultarTodos() {
        return executarConsultaLista(SELECT_ALL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Carteira> consultarTodasOrdenadasPorTitular() {
        return executarConsultaLista(SELECT_ALL_BY_HOLDER);
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

        try {
            Connection con = DatabaseConnection.getInstancia().getConexao();
            try (PreparedStatement ps = con.prepareStatement(UPDATE)) {
                ps.setString(1, carteira.getTitular());
                ps.setString(2, carteira.getCorretora());
                ps.setInt(3, carteira.getId());

                if (ps.executeUpdate() == 0) {
                    throw new NoSuchElementException(
                            I18n.getComParametros("wallet.error.notFound", String.valueOf(carteira.getId())));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.getComParametros("db.error.update", e.getMessage()), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se o {@code id} for nulo.
     * @throws NoSuchElementException   se a carteira não existir no banco.
     * @throws IllegalStateException    se a carteira possuir movimentações vinculadas (FK).
     */
    @Override
    public void excluir(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(I18n.get("wallet.error.nullId"));
        }

        try {
            Connection con = DatabaseConnection.getInstancia().getConexao();
            try (PreparedStatement ps = con.prepareStatement(DELETE)) {
                ps.setInt(1, id);

                if (ps.executeUpdate() == 0) {
                    throw new NoSuchElementException(
                            I18n.getComParametros("wallet.error.notFound", String.valueOf(id)));
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERRO_FK_REFERENCIADA) {
                throw new IllegalStateException(
                        I18n.getComParametros("wallet.error.hasTransactions", String.valueOf(id)), e);
            }
            throw new RuntimeException(I18n.getComParametros("db.error.delete", e.getMessage()), e);
        }
    }

    /**
     * Método auxiliar para evitar duplicação de código nas consultas que retornam listas de carteiras.
     */
    private List<Carteira> executarConsultaLista(String sql) {
        List<Carteira> lista = new ArrayList<>();
        try {
            Connection con = DatabaseConnection.getInstancia().getConexao();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.getComParametros("db.error.select", e.getMessage()), e);
        }
        return lista;
    }

    /**
     * Converte a linha atual do {@link ResultSet} em uma instância de {@link Carteira}.
     */
    private Carteira mapear(ResultSet rs) throws SQLException {
        return new Carteira(
                rs.getInt("id_carteira"),
                rs.getString("titular"),
                rs.getString("corretora")
        );
    }
}