package br.ftcoin.service;

import br.ftcoin.util.I18n;

/**
 * Exceção de domínio lançada quando ocorre uma falha ao consultar, obter
 * ou interpretar uma cotação junto ao {@link OraculoCotacao}.
 *
 * <p>Por estender {@link RuntimeException}, permite que falhas de comunicação HTTP,
 * datas sem cotação disponível ou respostas malformadas sejam propagadas de forma
 * limpa até a camada de apresentação (CLI), onde são exibidas ao usuário.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 * @see OraculoCotacao
 * @see OraculoService
 */
public class OraculoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria uma exceção para uma falha ao consultar ou interpretar uma cotação.
     *
     * @param message Descrição detalhada da falha (preferencialmente já traduzida via {@link I18n}).
     */
    public OraculoException(String message) {
        super(message);
    }

    /**
     * Cria uma exceção encapsulando a causa original da falha (ex.: erro de rede ou parsing).
     *
     * @param message Descrição detalhada da falha.
     * @param cause   Exceção original que motivou a falha.
     */
    public OraculoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Cria uma exceção resolvendo automaticamente a mensagem internacionalizada
     * a partir de uma chave e argumentos opcionais do {@link I18n}.
     *
     * @param chaveI18n  Chave mapeada nos arquivos {@code messages_*.properties}.
     * @param argumentos Argumentos dinâmicos para formatação da mensagem.
     * @return Nova instância de {@link OraculoException} com mensagem traduzida.
     */
    public static OraculoException comChave(String chaveI18n, Object... argumentos) {
        return new OraculoException(I18n.getComParametros(chaveI18n, argumentos));
    }
}