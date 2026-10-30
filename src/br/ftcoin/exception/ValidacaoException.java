package br.ftcoin.exception;

import br.ftcoin.util.I18n;

//Recebe uma chave do I18n (e parâmetros opcionais), nunca um texto fixo
public class ValidacaoException extends RuntimeException {

    private final String chave;
    private final Object[] argumentos;

    public ValidacaoException(String chave, Object... argumentos) {
        super(chave);
        this.chave = chave;
        this.argumentos = argumentos;
    }

    public String getChave() {
        return chave;
    }

    // A tradução acontece aqui, no momento em que a mensagem é lida,
    // então sempre respeita o idioma ativo no I18n.
    @Override
    public String getMessage() {
        return I18n.getComParametros(chave, argumentos);
    }
}