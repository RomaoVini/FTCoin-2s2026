package br.ftcoin.controller;

import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.dao.DAOFactory;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Carteira;

import java.util.List;

/**
 * Orquestra o fluxo entre a View e o DAO de Carteira.
 * Valida os dados de entrada e traduz falhas em ValidacaoException
 * (mensagens via I18n), que a View exibe ao usuário.
 */
public class CarteiraController {

    private final CarteiraDAO dao;

        // Escolhe a persistência: "MEMORIA" ou "MARIADB"
    public CarteiraController() {
        this(DAOFactory.getFactory("MARIADB"));
    }

    // Recebe a fábrica, sem depender de uma implementação específica
    public CarteiraController(DAOFactory factory) {
        this(factory.getCarteiraDAO());
    }

    // Permite injetar um DAO (por exemplo, o de memória nos testes).
    public CarteiraController(CarteiraDAO dao) {
        this.dao = dao;
    }

    public Carteira cadastrar(int id, String titular, String corretora) {
        validarId(id);
        validarTexto(titular, "wallet.error.holderRequired");
        validarTexto(corretora, "wallet.error.brokerRequired");

        if (dao.consultar(id).isPresent()) {
            throw new ValidacaoException("wallet.error.duplicateId", String.valueOf(id));
        }

        Carteira carteira = new Carteira(id, titular.trim(), corretora.trim());
        dao.incluir(carteira);
        return carteira;
    }

    public Carteira atualizar(int id, String titular, String corretora) {
        validarId(id);
        validarTexto(titular, "wallet.error.holderRequired");
        validarTexto(corretora, "wallet.error.brokerRequired");
        buscarOuFalhar(id);

        Carteira carteira = new Carteira(id, titular.trim(), corretora.trim());
        dao.editar(carteira);
        return carteira;
    }

    public void excluir(int id) {
        validarId(id);
        buscarOuFalhar(id);

        try {
            dao.excluir(id);
        } catch (IllegalStateException e) {
            // O DAO MariaDB lança isso quando a carteira possui movimentações.
            throw new ValidacaoException("wallet.error.hasTransactions", String.valueOf(id));
        }
    }

    public Carteira buscarPorId(int id) {
        validarId(id);
        return buscarOuFalhar(id);
    }

    public List<Carteira> listarTodas() {
        return dao.consultarTodos();
    }

    private Carteira buscarOuFalhar(int id) {
        return dao.consultar(id).orElseThrow(() ->
                new ValidacaoException("wallet.error.notFound", String.valueOf(id)));
    }

    private static void validarId(int id) {
        if (id <= 0) {
            throw new ValidacaoException("wallet.error.invalidId", String.valueOf(id));
        }
    }

    private static void validarTexto(String valor, String chaveErro) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException(chaveErro);
        }
    }
}
