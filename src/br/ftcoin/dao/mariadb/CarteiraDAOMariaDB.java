package br.ftcoin.dao.mariadb;

import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.dao.ConnectionFactory;
import br.ftcoin.model.Carteira;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/*
 * Tabela esperada (MariaDB):
 *
 * CREATE TABLE carteira (
 *     id_carteira INT PRIMARY KEY,
 *     titular     VARCHAR(100) NOT NULL,
 *     corretora   VARCHAR(100) NOT NULL
 * );
 *
 * Obs.: o id é informado pela aplicação (como em CarteiraDAOMemoria),
 * por isso a coluna não é AUTO_INCREMENT.
 */
public final class CarteiraDAOMariaDB implements CarteiraDAO {

    // Códigos de erro do MariaDB
    private static final int ERRO_DUPLICADO = 1062;      // Duplicate entry
    private static final int ERRO_FK_REFERENCIADA = 1451; // Linha referenciada por outra tabela

    private static final String INSERT =
            "INSERT INTO carteira (Id_Carteira, Titular, Corretora) VALUES (?, ?, ?)";
    private static final String UPDATE =
            "UPDATE carteira SET Titular = ?, Corretora = ? WHERE Id_Carteira = ?";
    private static final String DELETE =
            "DELETE FROM carteira WHERE Id_Carteira = ?";
    private static final String SELECT_ID =
            "SELECT Id_Carteira, Titular, Corretora FROM carteira WHERE Id_Carteira = ?";
    private static final String SELECT_ALL =
            "SELECT Id_Carteira, Titular, Corretora FROM carteira ORDER BY Id_Carteira";

    @Override
    public void incluir(Carteira carteira) {
        if (carteira == null) {
            throw new IllegalArgumentException("A carteira não pode ser nula.");
        }

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT)) {

            ps.setInt(1, carteira.getId());
            ps.setString(2, carteira.getTitular());
            ps.setString(3, carteira.getCorretora());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == ERRO_DUPLICADO) {
                throw new IllegalArgumentException(
                        "Já existe uma carteira com esse identificador.", e);
            }
            throw new RuntimeException("Erro ao inserir carteira: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Carteira> consultar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("O identificador não pode ser nulo.");
        }

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar carteira: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Carteira> consultarTodos() {
        List<Carteira> lista = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar carteiras: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void editar(Carteira carteira) {
        if (carteira == null) {
            throw new IllegalArgumentException("A carteira não pode ser nula.");
        }

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {

            ps.setString(1, carteira.getTitular());
            ps.setString(2, carteira.getCorretora());
            ps.setInt(3, carteira.getId());

            if (ps.executeUpdate() == 0) {
                throw new NoSuchElementException("Carteira não encontrada.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar carteira: " + e.getMessage(), e);
        }
    }

    @Override
    public void excluir(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("O identificador não pode ser nulo.");
        }

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new NoSuchElementException("Carteira não encontrada.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERRO_FK_REFERENCIADA) {
                throw new IllegalStateException(
                        "Não é possível excluir: a carteira possui movimentações.", e);
            }
            throw new RuntimeException("Erro ao excluir carteira: " + e.getMessage(), e);
        }
    }

    private Carteira mapear(ResultSet rs) throws SQLException {
        return new Carteira(
                rs.getInt("Id_Carteira"),
                rs.getString("Titular"),
                rs.getString("Corretora"));
    }
}
