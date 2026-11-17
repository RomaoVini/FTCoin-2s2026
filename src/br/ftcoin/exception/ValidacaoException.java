package br.ftcoin.exception;

import br.ftcoin.util.I18n;

/**
 * Exceção de domínio lançada quando uma regra de negócio ou validação falha.
 * Resolve automaticamente a mensagem traduzida através da chave do {@link I18n}.
 */
public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String chaveI18n, Object... parametros) {
        super(parametros.length > 0 
                ? I18n.getComParametros(chaveI18n, parametros) 
                : I18n.get(chaveI18n));
    }
}