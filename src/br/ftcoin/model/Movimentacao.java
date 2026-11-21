package br.ftcoin.model;

import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade de domínio que representa uma movimentação financeira (compra ou venda
 * de moedas virtuais) associada a uma {@link Carteira} no sistema FT_Coin.
 *
 * <p>Garante que nenhuma instância exista em estado inválido através de validações
 * rigorosas e internacionalizadas ({@link ValidacaoException}) aplicadas desde a construção.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class Movimentacao {

    /** Identificador único da movimentação ({@code 0} enquanto não persistida). */
    private int idMovimento;

    /** Identificador da carteira titular da operação. */
    private int idCarteira;

    /** Data em que a operação foi realizada. */
    private LocalDate data;

    /** Tipo da operação (compra ou venda). */
    private TipoOperacao tipo;

    /** Quantidade de moedas negociadas (sempre maior que zero). */
    private BigDecimal quantidade;

    /**
     * Constrói uma nova movimentação ainda não persistida (com {@code idMovimento = 0}),
     * cujo identificador será atribuído automaticamente pelo repositório/banco.
     *
     * @param idCarteira Identificador da carteira titular da operação.
     * @param data       Data em que a operação foi realizada.
     * @param tipo       Tipo da operação ({@link TipoOperacao#COMPRA} ou {@link TipoOperacao#VENDA}).
     * @param quantidade Quantidade de moedas negociadas (deve ser estritamente maior que zero).
     * @throws ValidacaoException se algum parâmetro violar as regras de integridade do domínio.
     */
    public Movimentacao(int idCarteira, LocalDate data, TipoOperacao tipo, BigDecimal quantidade) {
        this(0, idCarteira, data, tipo, quantidade);
    }

    /**
     * Constrói uma movimentação completa especificando seu identificador único.
     *
     * @param idMovimento Identificador único da movimentação (zero para novas ou positivo para existentes).
     * @param idCarteira  Identificador da carteira titular.
     * @param data        Data da operação.
     * @param tipo        Tipo da operação ({@link TipoOperacao}).
     * @param quantidade  Quantidade de moedas negociadas.
     * @throws ValidacaoException se algum parâmetro violar as regras de integridade do domínio.
     */
    public Movimentacao(int idMovimento, int idCarteira, LocalDate data, TipoOperacao tipo, BigDecimal quantidade) {
        this.idMovimento = validarIdMovimento(idMovimento);
        this.idCarteira = validarIdCarteira(idCarteira);
        this.data = validarDataOperacao(data);
        this.tipo = validarTipoOperacao(tipo);
        this.quantidade = validarQuantidade(quantidade);
    }

    /**
     * Construtor de conveniência utilizado pela camada de persistência (DAO)
     * para reconstruir a entidade a partir do caractere armazenado no banco ({@code 'C'} ou {@code 'V'}).
     *
     * @param idMovimento Identificador único da movimentação.
     * @param idCarteira  Identificador da carteira titular.
     * @param data        Data da operação.
     * @param tipo        Código caractere da operação ({@code 'C'} ou {@code 'V'}).
     * @param quantidade  Quantidade de moedas negociadas.
     * @throws ValidacaoException se o código da operação ou qualquer outro campo for inválido.
     */
    public Movimentacao(int idMovimento, int idCarteira, LocalDate data, char tipo, BigDecimal quantidade) {
        this(idMovimento, idCarteira, data, TipoOperacao.fromCodigo(tipo), quantidade);
    }

    /**
     * Retorna o identificador único da movimentação.
     *
     * @return Identificador numérico da movimentação ({@code 0} se ainda não persistida).
     */
    public int getIdMovimento() {
        return idMovimento;
    }

    /**
     * Define o identificador único da movimentação (utilizado após inserção no banco/memória).
     *
     * @param idMovimento Identificador numérico não negativo.
     * @throws ValidacaoException se {@code idMovimento} for negativo.
     */
    public void setIdMovimento(int idMovimento) {
        this.idMovimento = validarIdMovimento(idMovimento);
    }

    /**
     * Retorna o identificador da carteira vinculada a esta movimentação.
     *
     * @return Identificador da carteira.
     */
    public int getIdCarteira() {
        return idCarteira;
    }

    /**
     * Define o identificador da carteira vinculada.
     *
     * @param idCarteira Identificador positivo da carteira.
     * @throws ValidacaoException se {@code idCarteira} for menor ou igual a zero.
     */
    public void setIdCarteira(int idCarteira) {
        this.idCarteira = validarIdCarteira(idCarteira);
    }

    /**
     * Retorna a data de realização da movimentação.
     *
     * @return Data da operação ({@link LocalDate}).
     */
    public LocalDate getDataOperacao() {
        return data;
    }

    /**
     * Define a data de realização da movimentação.
     *
     * @param data Data não nula da operação.
     * @throws ValidacaoException se {@code data} for nula.
     */
    public void setDataOperacao(LocalDate data) {
        this.data = validarDataOperacao(data);
    }

    /**
     * Retorna o tipo da operação financeira ({@link TipoOperacao#COMPRA} ou {@link TipoOperacao#VENDA}).
     *
     * @return Tipo da operação.
     */
    public TipoOperacao getTipoOperacao() {
        return tipo;
    }

    /**
     * Define o tipo da operação financeira.
     *
     * @param tipo Tipo não nulo da operação.
     * @throws ValidacaoException se {@code tipo} for nulo.
     */
    public void setTipoOperacao(TipoOperacao tipo) {
        this.tipo = validarTipoOperacao(tipo);
    }

    /**
     * Retorna a quantidade exata de moedas virtuais negociadas.
     *
     * @return Quantidade em {@link BigDecimal}.
     */
    public BigDecimal getQuantidade() {
        return quantidade;
    }

    /**
     * Define a quantidade de moedas negociadas na movimentação.
     *
     * @param quantidade Valor estritamente positivo ({@code > 0}).
     * @throws ValidacaoException se {@code quantidade} for nula, zero ou negativa.
     */
    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = validarQuantidade(quantidade);
    }

    /**
     * Indica se esta movimentação representa uma operação de compra.
     *
     * @return {@code true} se o tipo for {@link TipoOperacao#COMPRA}.
     */
    public boolean isCompra() {
        return tipo == TipoOperacao.COMPRA;
    }

    /**
     * Indica se esta movimentação representa uma operação de venda.
     *
     * @return {@code true} se o tipo for {@link TipoOperacao#VENDA}.
     */
    public boolean isVenda() {
        return tipo == TipoOperacao.VENDA;
    }

    /**
     * Valida o identificador da movimentação.
     *
     * @param idMovimento Identificador a validar.
     * @return O próprio identificador, se válido.
     * @throws ValidacaoException se o identificador for negativo.
     */
    private static int validarIdMovimento(int idMovimento) {
        if (idMovimento < 0) {
            throw new ValidacaoException("transaction.error.negativeId");
        }
        return idMovimento;
    }

    /**
     * Valida o identificador da carteira.
     *
     * @param idCarteira Identificador a validar.
     * @return O próprio identificador, se válido.
     * @throws ValidacaoException se o identificador for menor ou igual a zero.
     */
    private static int validarIdCarteira(int idCarteira) {
        if (idCarteira <= 0) {
            throw new ValidacaoException("transaction.error.invalidWalletId", String.valueOf(idCarteira));
        }
        return idCarteira;
    }

    /**
     * Valida a data da operação.
     *
     * @param data Data a validar.
     * @return A própria data, se válida.
     * @throws ValidacaoException se a data for nula.
     */
    private static LocalDate validarDataOperacao(LocalDate data) {
        if (data == null) {
            throw new ValidacaoException("transaction.error.dateRequired");
        }
        return data;
    }

    /**
     * Valida o tipo da operação.
     *
     * @param tipo Tipo a validar.
     * @return O próprio tipo, se válido.
     * @throws ValidacaoException se o tipo for nulo.
     */
    private static TipoOperacao validarTipoOperacao(TipoOperacao tipo) {
        if (tipo == null) {
            throw new ValidacaoException("transaction.error.typeRequired");
        }
        return tipo;
    }

    /**
     * Valida a quantidade negociada.
     *
     * @param quantidade Quantidade a validar.
     * @return A própria quantidade, se válida.
     * @throws ValidacaoException se a quantidade for nula, zero ou negativa.
     */
    private static BigDecimal validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null) {
            throw new ValidacaoException("transaction.error.quantityRequired");
        }
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("transaction.error.quantityNegative");
        }
        return quantidade;
    }

    /**
     * Compara duas movimentações com base exclusivamente em seu identificador único ({@code idMovimento}).
     *
     * @param o Objeto a ser comparado.
     * @return {@code true} se ambos representarem a movimentação com o mesmo identificador.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movimentacao that)) return false;
        return idMovimento == that.idMovimento;
    }

    /**
     * Gera o código hash baseado no identificador único da movimentação.
     *
     * @return Código hash do {@code idMovimento}.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idMovimento);
    }

    /**
     * Retorna uma representação textual estruturada da movimentação.
     *
     * @return String formatada com ID, carteira, data, tipo e quantidade.
     */
    @Override
    public String toString() {
        return String.format("Movimentacao{id=%d, carteira=%d, data=%s, tipo=%s, quantidade=%s}",
                idMovimento, idCarteira, data, tipo.name(), quantidade);
    }
}