package br.ftcoin.enums;

import br.ftcoin.exception.ValidacaoException;

/**
 * Enumeração que representa os tipos de movimentação financeira suportados
 * pelo sistema FT_Coin ({@link #COMPRA} e {@link #VENDA}), mapeando cada operação
 * ao seu respectivo código de persistência no banco de dados.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public enum TipoOperacao {

    /** Operação de aquisição de moedas virtuais (código {@code 'C'}). */
    COMPRA('C'),

    /** Operação de alienação/venda de moedas virtuais (código {@code 'V'}). */
    VENDA('V');

    /** Caractere identificador da operação usado na persistência. */
    private final char codigo;

    /**
     * Construtor interno do enum.
     *
     * @param codigo Caractere identificador persistido no banco de dados ({@code 'C'} ou {@code 'V'}).
     */
    TipoOperacao(char codigo) {
        this.codigo = codigo;
    }

    /**
     * Retorna o caractere identificador da operação para persistência.
     *
     * @return {@code 'C'} para compra ou {@code 'V'} para venda.
     */
    public char getCodigo() {
        return codigo;
    }

    /**
     * Converte o código persistido ({@code 'C'} ou {@code 'V'}, independente de caixa)
     * na constante {@link TipoOperacao} correspondente.
     *
     * @param codigo Caractere lido do banco de dados ou entrada do usuário.
     * @return Instância de {@link TipoOperacao} correspondente.
     * @throws ValidacaoException se o caractere informado não corresponder a nenhuma operação válida.
     */
    public static TipoOperacao fromCodigo(char codigo) {
        return switch (Character.toUpperCase(codigo)) {
            case 'C' -> COMPRA;
            case 'V' -> VENDA;
            default -> throw new ValidacaoException("validation.operationType.invalid", String.valueOf(codigo));
        };
    }

    /**
     * Retorna a representação textual do código da operação ({@code "C"} ou {@code "V"}),
     * compatível com a persistência relacional.
     *
     * @return String contendo o código da operação.
     */
    @Override
    public String toString() {
        return String.valueOf(codigo);
    }
}