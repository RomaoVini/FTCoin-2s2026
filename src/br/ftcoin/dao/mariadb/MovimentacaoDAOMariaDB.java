package br.ftcoin.dao.mariadb;

import br.ftcoin.dao.ConnectionFactory;
import br.ftcoin.dao.MovimentacaoDAO;
import br.ftcoin.model.Movimentacao;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * Tabela esperada (MariaDB):
 *
 * CREATE TABLE movimentacao (
 *     id_movimento  INT AUTO_INCREMENT PRIMARY KEY,
 *     id_carteira   INT NOT NULL,
 *     data_operacao DATE NOT NULL,
 *     tipo          CHAR(1) NOT NULL,
 *     quantidade    DECIMAL(20,8) NOT NULL,
 *     FOREIGN KEY (id_carteira) REFERENCES carteira(id_carteira)
 * );
 */
public class MovimentacaoDAOMariaDB implements MovimentacaoDAO {

    private static final String INSERT =
            "INSERT INTO movimentacao (id_carteira, data_operacao, tipo, quantidade) VALUES (?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE movimentacao SET id_carteira = ?, data_operacao = ?, tipo = ?, quantidade = ? WHERE id_movimento = ?";
    private static final String DELETE =
            "DELETE FROM movimentacao WHERE id_movimento = ?";
    private static final String SELECT_ID =
            "SELECT id_movimento, id_carteira, data_operacao, tipo, quantidade FROM movimentacao WHERE id_movimento = ?";
    private static final String SELECT_ALL =
            "SELECT id_movimento, id_carteira, data_operacao, tipo, quantidade FROM movimentacao ORDER BY data_operacao DESC, id_movimento DESC";
    private static final String SELECT_CARTEIRA =
            "SELECT id_movimento, id_carteira, data_operacao, tipo, quantidade FROM movimentacao WHERE id_carteira = ? ORDER BY data_operacao DESC, id_movimento DESC";

    @Override
    public void incluir(Movimentacao m) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

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
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir movimentação: " + e.getMessage(), e);
        }
    }

    @Override
    public void editar(Movimentacao m) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {

            ps.setInt(1, m.getIdCarteira());
            ps.setDate(2, Date.valueOf(m.getDataOperacao()));
            ps.setString(3, String.valueOf(m.getTipoOperacao().getCodigo()));
            ps.setBigDecimal(4, m.getQuantidade());
            ps.setInt(5, m.getIdMovimento());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar movimentação: " + e.getMessage(), e);
        }
    }

    @Override
    public void excluir(Integer id) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir movimentação: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Movimentacao> consultar(Integer id) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar movimentação: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Movimentacao> consultarTodos() {
        List<Movimentacao> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar movimentações: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<Movimentacao> consultarPorCarteira(int idCarteira) {
        List<Movimentacao> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_CARTEIRA)) {

            ps.setInt(1, idCarteira);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar movimentações da carteira: " + e.getMessage(), e);
        }
        return lista;
    }

    // Usa o construtor que recebe o tipo como char.
    private Movimentacao mapear(ResultSet rs) throws SQLException {
        return new Movimentacao(
                rs.getInt("id_movimento"),
                rs.getInt("id_carteira"),
                rs.getDate("data_operacao").toLocalDate(),
                rs.getString("tipo").charAt(0),
                rs.getBigDecimal("quantidade"));
    }
}
