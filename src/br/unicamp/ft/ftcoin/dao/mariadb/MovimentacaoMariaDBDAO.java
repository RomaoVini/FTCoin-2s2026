
package br.unicamp.ft.ftcoin.dao.mariadb;
import br.unicamp.ft.ftcoin.model.Movimentacao;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovimentacaoMariaDBDAO implements MovimentacaoDAO {

    private Connection getConnection() throws SQLException {
        // Configure aqui a sua conexão com o MariaDB
        throw new UnsupportedOperationException("Configure a sua conexão JDBC com o MariaDB.");
    }

    @Override
    public void incluir(Movimentacao mov) {
        String sql = "INSERT INTO movimentacao (id_carteira, data_operacao, tipo, quantidade) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, mov.getIdCarteira());
            stmt.setDate(2, Date.valueOf(mov.getDataOperacao()));
            // Passando o char do tipo operacao conforme sua regra de negócio
            stmt.setString(3, String.valueOf(mov.getTipoOperacao().getCodigo()));
            stmt.setBigDecimal(4, mov.getQuantidade());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    mov.setIdMovimento(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao incluir movimentação: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Movimentacao> consultar(Integer id) {
        String sql = "SELECT id_movimento, id_carteira, data_operacao, tipo, quantidade FROM movimentacao WHERE id_movimento = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extrairMovimentacao(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar movimentação por ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Movimentacao> consultarTodos() {
        String sql = "SELECT id_movimento, id_carteira, data_operacao, tipo, quantidade FROM movimentacao";
        List<Movimentacao> movimentacoes = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                movimentacoes.add(extrairMovimentacao(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar todas as movimentações: " + e.getMessage(), e);
        }

        return movimentacoes;
    }

    @Override
    public void editar(Movimentacao mov) {
        String sql = "UPDATE movimentacao SET id_carteira = ?, data_operacao = ?, tipo = ?, quantidade = ? WHERE id_movimento = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mov.getIdCarteira());
            stmt.setDate(2, Date.valueOf(mov.getDataOperacao()));
            stmt.setString(3, String.valueOf(mov.getTipoOperacao().getCodigo()));
            stmt.setBigDecimal(4, mov.getQuantidade());
            stmt.setInt(5, mov.getIdMovimento());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao editar movimentação: " + e.getMessage(), e);
        }
    }

    @Override
    public void excluir(Integer id) {
        String sql = "DELETE FROM movimentacao WHERE id_movimento = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir movimentação: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Movimentacao> consultarPorCarteira(int idCarteira) {
        String sql = "SELECT id_movimento, id_carteira, data_operacao, tipo, quantidade FROM movimentacao WHERE id_carteira = ?";
        List<Movimentacao> movimentacoes = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCarteira);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimentacoes.add(extrairMovimentacao(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar movimentações por carteira: " + e.getMessage(), e);
        }

        return movimentacoes;
    }

    // Utiliza estritamente o construtor da sua entidade que recebe o 'char' do banco
    private Movimentacao extrairMovimentacao(ResultSet rs) throws SQLException {
        int idMovimento = rs.getInt("id_movimento");
        int idCarteira = rs.getInt("id_carteira");
        LocalDate data = rs.getDate("data_operacao").toLocalDate();
        
        // Lê o caractere do banco (ex: 'C' ou 'V')
        char tipoChar = rs.getString("tipo").charAt(0);
        
        BigDecimal quantidade = rs.getBigDecimal("quantidade");

        // Chama exatamente o construtor especificado na entidade:
        // public Movimentacao(int idMovimento, int idCarteira, LocalDate data, char tipo, BigDecimal quantidade)
        return new Movimentacao(idMovimento, idCarteira, data, tipoChar, quantidade);
    }
}