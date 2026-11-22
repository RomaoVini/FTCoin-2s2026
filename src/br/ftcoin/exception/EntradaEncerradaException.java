package br.ftcoin.exception;

/** Lançada quando a entrada padrão é encerrada (EOF) durante a leitura. */
public class EntradaEncerradaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EntradaEncerradaException() {
        super("Entrada encerrada.");
    }
}