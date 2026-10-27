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
        setIdMovimento(idMovimento);
        setIdCarteira(idCarteira);
        setDataOperacao(data);
        setTipoOperacao(tipo);
        setQuantidade(quantidade);
    }

    //Usado pelo DAO, que lê o tipo como char do banco.
    public Movimentacao(int idMovimento, int idCarteira, LocalDate data, char tipo, BigDecimal quantidade){
        this(idMovimento, idCarteira, data, TipoOperacao.fromCodigo(tipo), quantidade);
    }

    public int getIdMovimento(){ 
        return idMovimento; 
    }

    public void setIdMovimento(int idMovimento){
        if(idMovimento < 0){
            throw new ValidacaoException("O identificador da movimentação não pode ser negativo.");
        }
        this.idMovimento = idMovimento;
    }

    public int getIdCarteira(){ 
        return idCarteira; 
    }

    public void setIdCarteira(int idCarteira){
        if(idCarteira <= 0){
            throw new ValidacaoException("Identificador de carteira inválido: " + idCarteira + ".");
        }
        this.idCarteira = idCarteira;
    }

    public LocalDate getDataOperacao(){ 
        return data; 
    }

    public void setDataOperacao(LocalDate data){
        if(data == null){
            throw new ValidacaoException("A data da operação é obrigatória.");
        }
        this.data = data;
    }

    public TipoOperacao getTipoOperacao(){ 
        return tipo; 
    }

    public void setTipoOperacao(TipoOperacao tipo) {
        if(tipo == null){
            throw new ValidacaoException("O tipo da operação é obrigatório.");
        }
        this.tipo = tipo;
    }

    public BigDecimal getQuantidade(){ 
        return quantidade; 
    }

    public void setQuantidade(BigDecimal quantidade) {
        if(quantidade == null){
            throw new ValidacaoException("A quantidade é obrigatória.");
        }
        if(quantidade.compareTo(BigDecimal.ZERO) < 0){
            throw new ValidacaoException("A quantidade não pode ser negativa.");
        }
        this.quantidade = quantidade;
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
