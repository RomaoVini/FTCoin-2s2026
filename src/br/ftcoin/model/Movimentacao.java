package br.ftcoin.model;

import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import java.math.BigDecimal;
import java.time.LocalDate;

//Entidade nunca existe em estado inválido.
public class Movimentacao {

    private int idMovimento;
    private int idCarteira;
    private LocalDate data;
    private TipoOperacao tipo;
    private BigDecimal quantidade;

    public Movimentacao(int idCarteira, LocalDate data, TipoOperacao tipo, BigDecimal quantidade){
        this(0, idCarteira, data, tipo, quantidade);
    }

    public Movimentacao(int idMovimento, int idCarteira, LocalDate data, TipoOperacao tipo, BigDecimal quantidade){
        this.idMovimento = validarIdMovimento(idMovimento);
        this.idCarteira = validarIdCarteira(idCarteira);
        this.data = validarDataOperacao(data);
        this.tipo = validarTipoOperacao(tipo);
        this.quantidade = validarQuantidade(quantidade);
    }

    //Usado pelo DAO, que lê o tipo como char do banco.
    public Movimentacao(int idMovimento, int idCarteira, LocalDate data, char tipo, BigDecimal quantidade){
        this(idMovimento, idCarteira, data, TipoOperacao.fromCodigo(tipo), quantidade);
    }

    public int getIdMovimento(){ 
        return idMovimento; 
    }

    public void setIdMovimento(int idMovimento){
        this.idMovimento = validarIdMovimento(idMovimento);
    }

    private static int validarIdMovimento(int idMovimento){
        if(idMovimento < 0){
            throw new ValidacaoException("O identificador da movimentação não pode ser negativo.");
        }
        return idMovimento;
    }

    public int getIdCarteira(){ 
        return idCarteira; 
    }

    public void setIdCarteira(int idCarteira){
        this.idCarteira = validarIdCarteira(idCarteira);
    }

    private static int validarIdCarteira(int idCarteira){
        if(idCarteira <= 0){
            throw new ValidacaoException("Identificador de carteira inválido: " + idCarteira + ".");
        }
        return idCarteira;
    }

    public LocalDate getDataOperacao(){ 
        return data; 
    }

    public void setDataOperacao(LocalDate data){
        this.data = validarDataOperacao(data);
    }

    private static LocalDate validarDataOperacao(LocalDate data){
        if(data == null){
            throw new ValidacaoException("A data da operação é obrigatória.");
        }
        return data;
    }

    public TipoOperacao getTipoOperacao(){ 
        return tipo; 
    }

    public void setTipoOperacao(TipoOperacao tipo) {
        this.tipo = validarTipoOperacao(tipo);
    }

    private static TipoOperacao validarTipoOperacao(TipoOperacao tipo){
        if(tipo == null){
            throw new ValidacaoException("O tipo da operação é obrigatório.");
        }
        return tipo;
    }

    public BigDecimal getQuantidade(){ 
        return quantidade; 
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = validarQuantidade(quantidade);
    }

    private static BigDecimal validarQuantidade(BigDecimal quantidade){
        if(quantidade == null){
            throw new ValidacaoException("A quantidade é obrigatória.");
        }
        if(quantidade.compareTo(BigDecimal.ZERO) < 0){
            throw new ValidacaoException("A quantidade não pode ser negativa.");
        }
        return quantidade;
    }

    public boolean isCompra(){ 
        return tipo == TipoOperacao.COMPRA; 
    }

    public boolean isVenda(){ 
        return tipo == TipoOperacao.VENDA; 
    }

    @Override
    public String toString(){
        return "Movimentacao{id=" + idMovimento + ", carteira=" + idCarteira
                + ", data=" + data + ", tipo=" + tipo.name()
                + ", quantidade=" + quantidade + "}";
    }
}