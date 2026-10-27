package br.ftcoin.controller;

import br.ftcoin.dao.MovimentacaoDAO;
import br.ftcoin.dao.mariadb.MovimentacaoDAOMariaDB;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.exception.ValidacaoException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MovimentacaoController {

    private final MovimentacaoDAO dao;

    public MovimentacaoController() {
        this(new MovimentacaoDAOMariaDB());
    }

    public MovimentacaoController(MovimentacaoDAO dao) {
        this.dao = dao;
    }

    public Movimentacao cadastrar(int idCarteira, LocalDate data, TipoOperacao tipo, BigDecimal quantidade) {
        // O construtor valida; se algo estiver inválido, lança ValidacaoException.
        Movimentacao m = new Movimentacao(idCarteira, data, tipo, quantidade);
        dao.incluir(m);
        return m;
    }

    public Movimentacao atualizar(int idMovimento, int idCarteira, LocalDate data,
                                  TipoOperacao tipo, BigDecimal quantidade) {
        if (idMovimento <= 0) {
            throw new ValidacaoException("Identificador de movimentação inválido: " + idMovimento + ".");
        }
        buscarOuFalhar(idMovimento);
        Movimentacao m = new Movimentacao(idMovimento, idCarteira, data, tipo, quantidade);
        dao.editar(m);
        return m;
    }

    public void excluir(int idMovimento) {
        buscarOuFalhar(idMovimento);
        dao.excluir(idMovimento);
    }

    public Movimentacao buscarPorId(int idMovimento) {
        return buscarOuFalhar(idMovimento);
    }

    public List<Movimentacao> listarTodas() {
        return dao.consultarTodos();
    }

    public List<Movimentacao> listarPorCarteira(int idCarteira) {
        if (idCarteira <= 0) {
            throw new ValidacaoException("Identificador de carteira inválido: " + idCarteira + ".");
        }
        return dao.consultarPorCarteira(idCarteira);
    }

    private Movimentacao buscarOuFalhar(int idMovimento) {
        return dao.consultar(idMovimento).orElseThrow(() ->
                new ValidacaoException("Movimentação não encontrada: " + idMovimento + "."));
    }
}