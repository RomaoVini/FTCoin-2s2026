package br.ftcoin.exception;

import br.ftcoin.util.I18n;

/**
 * Exceção de domínio lançada quando uma regra de negócio ou validação falha.
 * Resolve automaticamente a mensagem traduzida através da chave do {@link I18n}.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class ValidacaoException extends RuntimeException {

    /** Identificador de versão para serialização. */
    private static final long serialVersionUID = 1L;

    /**
     * Cria uma exceção de validação a partir de uma chave de tradução.
     *
     * @param chaveI18n  chave da mensagem em um recurso de idioma
     * @param parametros valores opcionais para preencher os marcadores da mensagem
     */
    public ValidacaoException(String chaveI18n, Object... parametros) {
        super(parametros.length > 0
                ? I18n.getComParametros(chaveI18n, parametros)
                : I18n.get(chaveI18n));
    }
}