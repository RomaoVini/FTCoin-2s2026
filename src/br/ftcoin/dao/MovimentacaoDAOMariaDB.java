package br.ftcoin.dao;

import br.ftcoin.model.Movimentacao;
import br.ftcoin.util.DatabaseConnection;
import br.ftcoin.util.I18n;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Implementação concreta de {@link MovimentacaoDAO} para persistência relacional
 * no banco de dados MariaDB via JDBC.
 * Declarada como {@code final} por integrar a hierarquia selada de {@link MovimentacaoDAO}.
 *
 * <p><b>Estrutura esperada da tabela no MariaDB:</b></p>
 * <pre>{@code
 * CREATE TABLE MOVIMENTACAO (
 *     IdCarteira   INT NOT NULL,
 *     IdMovimento  INT AUTO_INCREMENT PRIMARY KEY,
 *     Data         DATE,
 *     TipoOperacao CHAR(1),
 *     Quantidade   DECIMAL(10,3),
 *     FOREIGN KEY (IdCarteira) REFERENCES CARTEIRA(IdCarteira)
 * );
 * }</pre>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class MovimentacaoDAOMariaDB implements MovimentacaoDAO {

    /** Código de erro MariaDB/MySQL para violação de chave estrangeira ao inserir/atualizar registro filho. */
    private static final int ERRO_FK_INEXISTENTE = 1452;

    /** Comando SQL para inserir uma nova movimentação (o {@code IdMovimento} é gerado pelo banco). */
    private static final String INSERT =
            "INSERT INTO MOVIMENTACAO (IdCarteira, Data, TipoOperacao, Quantidade) VALUES (?, ?, ?, ?)";

    /** Comando SQL para atualizar todos os campos de uma movimentação existente. */
    private static final String UPDATE =
            "UPDATE MOVIMENTACAO SET IdCarteira = ?, Data = ?, TipoOperacao = ?, Quantidade = ? WHERE IdMovimento = ?";

    /** Comando SQL para excluir uma movimentação pelo identificador. */
    private static final String DELETE =
            "DELETE FROM MOVIMENTACAO WHERE IdMovimento = ?";

    /** Consulta SQL que busca uma movimentação pelo identificador. */
    private static final String SELECT_ID =
            "SELECT IdMovimento, IdCarteira, Data, TipoOperacao, Quantidade FROM MOVIMENTACAO WHERE IdMovimento = ?";

    /** Consulta SQL que lista todas as movimentações, das mais recentes para as mais antigas. */
    private static final String SELECT_ALL =
            "SELECT IdMovimento, IdCarteira, Data, TipoOperacao, Quantidade FROM MOVIMENTACAO ORDER BY Data DESC, IdMovimento DESC";

    /** Consulta SQL que lista as movimentações de uma carteira, das mais recentes para as mais antigas. */
    private static final String SELECT_CARTEIRA =
            "SELECT IdMovimento, IdCarteira, Data, TipoOperacao, Quantidade FROM MOVIMENTACAO WHERE IdCarteira = ? ORDER BY Data DESC, IdMovimento DESC";

    /**
     * {@inheritDoc}
     * <p>Recupera automaticamente o {@code IdMovimento} gerado pelo banco via {@code AUTO_INCREMENT}
     * e o atribui à instância recebida.</p>
     *
     * @throws IllegalArgumentException se a movimentação for nula ou a carteira referenciada não existir no banco.
     */
    @Override
    public void incluir(Movimentacao m) {
        if (m == null) {
            throw new IllegalArgumentException(I18n.get("transaction.error.null"));
        }

        try {
            Connection con = DatabaseConnection.getInstancia().getConexao();
            try (PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, m.getIdCarteira());
                ps.setDate(2, Date.valueOf(m.getDataOperacao()));
                ps.setString(3, String.valueOf(m.getTipoOperacao().getCodigo()));
                ps.setBigDecimal(4, m.getQuantidade());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        m.setIdMovimento(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERRO_FK_INEXISTENTE) {
                throw new IllegalArgumentException(
                        I18n.getComParametros("transaction.wallet.notFound", String.valueOf(m.getIdCarteira())), e);
            }
            throw new RuntimeException(I18n.getComParametros("db.error.insert", e.getMessage()), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se a movimentação for nula ou a carteira referenciada não existir no banco.
     * @throws NoSuchElementException   se nenhuma movimentação com o ID informado for encontrada.
     */
    @Override
    public void editar(Movimentacao m) {
        if (m == null) {
            throw new IllegalArgumentException(I18n.get("transaction.error.null"));
        }

        try {
            Connection con = DatabaseConnection.getInstancia().getConexao();
            try (PreparedStatement ps = con.prepareStatement(UPDATE)) {
                ps.setInt(1, m.getIdCarteira());
                ps.setDate(2, Date.valueOf(m.getDataOperacao()));
                ps.setString(3, String.valueOf(m.getTipoOperacao().getCodigo()));
                ps.setBigDecimal(4, m.getQuantidade());
                ps.setInt(5, m.getIdMovimento());

                if (ps.executeUpdate() == 0) {
                    throw new NoSuchElementException(
                            I18n.getComParametros("transaction.error.notFound", String.valueOf(m.getIdMovimento())));
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERRO_FK_INEXISTENTE) {
                throw new IllegalArgumentException(
                        I18n.getComParametros("transaction.wallet.notFound", String.valueOf(m.getIdCarteira())), e);
            }
            throw new RuntimeException(I18n.getComParametros("db.error.update", e.getMessage()), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se o {@code id} informado for nulo.
     * @throws NoSuchElementException   se nenhuma movimentação com o ID informado for encontrada.
     */
    @Override
    public void excluir(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(I18n.get("transaction.error.nullId"));
        }

        try {
            Connection con = DatabaseConnection.getInstancia().getConexao();
            try (PreparedStatement ps = con.prepareStatement(DELETE)) {
                ps.setInt(1, id);

                if (ps.executeUpdate() == 0) {
                    throw new NoSuchElementException(
                            I18n.getComParametros("transaction.error.notFound", String.valueOf(id)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.getComParametros("db.error.delete", e.getMessage()), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se o {@code id} informado for nulo.
     */
    @Override
    public Optional<Movimentacao> consultar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(I18n.get("transaction.error.nullId"));
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
    public List<Movimentacao> consultarTodos() {
        List<Movimentacao> lista = new ArrayList<>();
        try {
            Connection con = DatabaseConnection.getInstancia().getConexao();
            try (PreparedStatement ps = con.prepareStatement(SELECT_ALL);
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
     * {@inheritDoc}
     */
    @Override
    public List<Movimentacao> consultarPorCarteira(int idCarteira) {
        List<Movimentacao> lista = new ArrayList<>();
        try {
            Connection con = DatabaseConnection.getInstancia().getConexao();
            try (PreparedStatement ps = con.prepareStatement(SELECT_CARTEIRA)) {
                ps.setInt(1, idCarteira);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapear(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.getComParametros("db.error.select", e.getMessage()), e);
        }
        return lista;
    }

    /**
     * Converte a linha atual do {@link ResultSet} em uma instância de {@link Movimentacao},
     * utilizando o construtor que converte o caractere {@code 'C'} ou {@code 'V'} do banco.
     *
     * @param rs Resultado da consulta, posicionado na linha a ser convertida.
     * @return A {@link Movimentacao} correspondente à linha atual.
     * @throws SQLException se ocorrer erro ao ler as colunas do resultado.
     */
    private Movimentacao mapear(ResultSet rs) throws SQLException {
        return new Movimentacao(
                rs.getInt("IdMovimento"),
                rs.getInt("IdCarteira"),
                rs.getDate("Data").toLocalDate(),
                rs.getString("TipoOperacao").charAt(0),
                rs.getBigDecimal("Quantidade")
        );
    }
}