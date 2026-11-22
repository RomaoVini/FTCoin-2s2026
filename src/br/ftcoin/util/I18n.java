package br.ftcoin.util;

import br.ftcoin.enums.Idioma;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Classe utilitária responsável pelo gerenciamento centralizado de internacionalização (I18n)
 * do sistema FT_Coin.
 *
 * <p>Controla o {@link ResourceBundle} ativo em memória, detecta automaticamente o
 * {@link Locale#getDefault()} na inicialização, permite a alternância dinâmica entre os
 * idiomas suportados ({@link Idioma}) e fornece recuperação segura de mensagens.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class I18n {

    private static final String BUNDLE_NAME = "messages";
    private static ResourceBundle bundle;
    private static Idioma idiomaAtual;

    static {
        // Detecta automaticamente o Locale padrão da JVM / Sistema Operacional
        setIdioma(Idioma.fromLocale(Locale.getDefault()));
    }

    /**
     * Construtor privado para impedir a instanciação desta classe utilitária.
     */
    private I18n() {
    }

    /**
     * Altera o idioma ativo do sistema, atualiza o {@link Locale} padrão da JVM
     * e recarrega o arquivo de propriedades ({@code messages_*.properties}) correspondente.
     *
     * @param novoIdioma Enum representando o idioma desejado (se nulo, detecta via {@link Locale#getDefault()}).
     */
    public static synchronized void setIdioma(Idioma novoIdioma) {
        if (novoIdioma == null) {
            novoIdioma = Idioma.fromLocale(Locale.getDefault());
        }
        idiomaAtual = novoIdioma;
        Locale.setDefault(novoIdioma.getLocale());
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, novoIdioma.getLocale());
    }

    /**
     * Retorna o enum correspondente ao idioma atualmente ativo na sessão.
     *
     * @return Instância de {@link Idioma} ativa.
     */
    public static Idioma getIdiomaAtual() {
        return idiomaAtual;
    }

    /**
     * Obtém a mensagem traduzida associada à chave informada no idioma ativo.
     *
     * @param chave Chave mapeada no arquivo {@code .properties}.
     * @return O texto traduzido ou {@code "!chave!"} como fallback caso a chave não exista.
     */
    public static String get(String chave) {
        if (chave == null || chave.isBlank()) {
            return "";
        }

        try {
            return bundle.getString(chave);
        } catch (MissingResourceException e) {
            return "!" + chave + "!";
        }
    }

    /**
     * Obtém uma mensagem traduzida e preenche seus marcadores dinâmicos ({@code {0}}, {@code {1}}, etc.).
     *
     * @param chave      Chave mapeada no arquivo {@code .properties}.
     * @param argumentos Valores que substituirão os placeholders da mensagem.
     * @return Texto traduzido e formatado com os argumentos fornecidos.
     */
    public static String getComParametros(String chave, Object... argumentos) {
        String mensagem = get(chave);
        if (argumentos == null || argumentos.length == 0) {
            return mensagem;
        }

        try {
            return MessageFormat.format(mensagem, argumentos);
        } catch (IllegalArgumentException e) {
            return mensagem;
        }
    }
}