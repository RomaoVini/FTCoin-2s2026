package br.unicamp.ft.ftcoin.controller;
import br.unicamp.ft.ftcoin.dao.MovimentacaoDAO;
import br.unicamp.ft.ftcoin.model.Movimentacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MovimentacaoController {

    private final MovimentacaoDAO movimentacaoDAO;

    public MovimentacaoController(MovimentacaoDAO movimentacaoDAO) {
        this.movimentacaoDAO = movimentacaoDAO;
    }

    public void cadastrarMovimentacao(int idCarteira, LocalDate data, TipoOperacao tipo, BigDecimal quantidade) {
        // A própria entidade garante que não existirá em estado inválido através do construtor/setters
        Movimentacao movimentacao = new Movimentacao(idCarteira, data, tipo, quantidade);
        movimentacaoDAO.incluir(movimentacao);
    }

    public Optional<Movimentacao> buscarPorId(int id) {
        return movimentacaoDAO.consultar(id);
    }

    public List<Movimentacao> listarTodas() {
        return movimentacaoDAO.consultarTodos();
    }

    public List<Movimentacao> listarPorCarteira(int idCarteira) {
        return movimentacaoDAO.consultarPorCarteira(idCarteira);
    }

    public void atualizarMovimentacao(int idMovimento, int idCarteira, LocalDate data, TipoOperacao tipo, BigDecimal quantidade) {
        Movimentacao movimentacao = new Movimentacao(idMovimento, idCarteira, data, tipo, quantidade);
        movimentacaoDAO.editar(movimentacao);
    }

    public void excluirMovimentacao(int idMovimento) {
        movimentacaoDAO.excluir(idMovimento);
    }
}
