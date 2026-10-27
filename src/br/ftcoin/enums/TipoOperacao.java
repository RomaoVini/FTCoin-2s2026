package br.ftcoin.enums;

import br.ftcoin.exception.ValidacaoException;

public enum TipoOperacao {
    COMPRA('C'),
    VENDA('V');

    private final char codigo;

    TipoOperacao(char codigo) {
        this.codigo = codigo;
    }

    public char getCodigo() {
        return codigo;
    }

    //converte o código persistido 'C' ou 'V' no enum correspondente.
    public static TipoOperacao fromCodigo(char codigo) {
        char c = Character.toUpperCase(codigo);
        for (TipoOperacao tipo : values()) {
            if (tipo.codigo == c) {
                return tipo;
            }
        }
        throw new ValidacaoException("Tipo de operação inválido: '" + codigo + "'. Use 'C' (compra) ou 'V' (venda).");
    }

    //faz o String.valueOf(tipo) devolver "C" ou "V", como o DAO espera.
    @Override
    public String toString() {
        return String.valueOf(codigo);
    }
}
