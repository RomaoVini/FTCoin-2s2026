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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementação concreta de {@link CarteiraDAO} para persistência relacional
 * no banco de dados MariaDB via JDBC.
 * É declarada como {@code final} por integrar a hierarquia selada de {@link CarteiraDAO}.
 *
 * <p><b>Estrutura esperada da tabela no MariaDB:</b></p>
 * <pre>{@code
 * CREATE TABLE CARTEIRA (
 *     IdCarteira INT AUTO_INCREMENT PRIMARY KEY,
 *     Titular    VARCHAR(50) NOT NULL,
 *     Corretora  VARCHAR(50) NOT NULL
 * );
 * }</pre>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class CarteiraDAOMariaDB implements CarteiraDAO {

    private static final Logger LOG = Logger.getLogger(CarteiraDAOMariaDB.class.getName());

    /** Código de erro MariaDB/MySQL para violação de chave única (Duplicate entry). */
    /** Código de erro MariaDB/MySQL para violação de chave única. */
    private static final int ERRO_DUPLICADO = 1062;

    /** Código de erro MariaDB/MySQL para violação de chave estrangeira em uso. */
    private static final int ERRO_FK_REFERENCIADA = 1451;

    /** Comando SQL para inserir uma nova carteira. */
    private static final String INSERT =
            "INSERT INTO CARTEIRA (IdCarteira, Titular, Corretora) VALUES (?, ?, ?)";

    /** Comando SQL para atualizar titular e corretora de uma carteira existente. */
    private static final String UPDATE =
            "UPDATE CARTEIRA SET Titular = ?, Corretora = ? WHERE IdCarteira = ?";

    /** Comando SQL para excluir uma carteira pelo identificador. */
    private static final String DELETE =
            "DELETE FROM CARTEIRA WHERE IdCarteira = ?";

    /** Consulta SQL que busca uma carteira pelo identificador. */
    private static final String SELECT_ID =
            "SELECT IdCarteira, Titular, Corretora FROM CARTEIRA WHERE IdCarteira = ?";

    /** Consulta SQL que lista todas as carteiras em ordem crescente de identificador. */
    private static final String SELECT_ALL =
            "SELECT IdCarteira, Titular, Corretora FROM CARTEIRA ORDER BY IdCarteira ASC";

    /** Consulta SQL que lista todas as carteiras em ordem alfabética de titular. */
    private static final String SELECT_ALL_BY_HOLDER =
            "SELECT IdCarteira, Titular, Corretora FROM CARTEIRA ORDER BY Titular ASC";

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se a carteira for nula ou o identificador já existir
     *                                  no banco
     * @throws RuntimeException se ocorrer um erro de acesso ao banco diferente da duplicidade
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
            LOG.log(Level.SEVERE, "Falha SQL ao persistir carteira", e);
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
     * @throws IllegalArgumentException se o {@code id} informado for nulo
     * @throws RuntimeException se ocorrer erro de acesso ao banco
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
            LOG.log(Level.SEVERE, "Falha SQL ao consultar carteiras", e);
            throw new RuntimeException(I18n.getComParametros("db.error.select", e.getMessage()), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException se ocorrer erro de acesso ao banco
     */
    @Override
    public List<Carteira> consultarTodos() {
        return executarConsultaLista(SELECT_ALL);
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException se ocorrer erro de acesso ao banco
     */
    @Override
    public List<Carteira> consultarTodasOrdenadasPorTitular() {
        return executarConsultaLista(SELECT_ALL_BY_HOLDER);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se a carteira for nula
     * @throws NoSuchElementException se nenhuma carteira com o ID especificado for encontrada
     * @throws RuntimeException se ocorrer erro de acesso ao banco
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
     * @throws IllegalArgumentException se o {@code id} for nulo
     * @throws NoSuchElementException se a carteira não existir no banco
     * @throws IllegalStateException se a carteira possuir movimentações vinculadas
     * @throws RuntimeException se ocorrer outro erro de acesso ao banco
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
     * Executa uma consulta de carteiras que não possui parâmetros externos.
     *
     * @param sql consulta SQL que retorna as colunas da tabela {@code CARTEIRA}
     * @return lista de carteiras retornadas pela consulta (pode ser vazia)
     * @throws RuntimeException se ocorrer erro de acesso ao banco de dados
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
     *
     * @param rs resultado da consulta posicionado na linha a ser convertida
     * @return {@link Carteira} correspondente à linha atual
     * @throws SQLException se ocorrer erro ao ler as colunas do resultado
     */
    private Carteira mapear(ResultSet rs) throws SQLException {
        return new Carteira(
                rs.getInt("IdCarteira"),
                rs.getString("Titular"),
                rs.getString("Corretora")
        );
    }
}