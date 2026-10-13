package br.unicamp.ft.ftcoin.util;

import br.unicamp.ft.ftcoin.enums.Idioma;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utilitário responsável pelo gerenciamento de internacionalização (I18n).
 * Controla o ResourceBundle ativo e fornece métodos seguros para recuperar mensagens.
 */
public class I18n {
    private static final String BUNDLE_NAME = "messages";
    private static ResourceBundle bundle;
    private static Idioma idiomaAtual;

    static {
        // Define o idioma padrão do sistema na inicialização
        setIdioma(Idioma.INGLES);
    }

    // Construtor privado para impedir instanciação (classe utilitária)
    private I18n() {}

    /**
     * Altera o idioma ativo do sistema e recarrega o arquivo de propriedades correspondente.
     * 
     * @param novoIdioma Enum representando o novo idioma
     */
    public static synchronized void setIdioma(Idioma novoIdioma) {
        if (novoIdioma == null) {
            novoIdioma = Idioma.INGLES;
        }
        idiomaAtual = novoIdioma;
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, novoIdioma.getLocale());
    }

    /**
     * Retorna o enum do idioma configurado no momento.
     * 
     * @return Idioma ativo
     */
    public static Idioma getIdiomaAtual() {
        return idiomaAtual;
    }

    /**
     * Obtém a mensagem traduzida associada à chave informada.
     * Garante a conversão correta de caracteres (UTF-8) e evita erros de execução.
     * 
     * @param chave Chave mapeada no arquivo .properties
     * @return O texto traduzido ou "!chave!" em caso de chave não encontrada
     */
    public static String get(String chave) {
        if (chave == null || chave.isBlank()) {
            return "";
        }

        try {
            String rawValue = bundle.getString(chave);
            // Corrige a codificação ISO-8859-1 (padrão de leitura legado) para UTF-8 nativo
            return new String(rawValue.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        } catch (MissingResourceException e) {
            // Retorno de fallback amigável em vez de lançar exceção ao usuário
            return "!" + chave + "!";
        }
    }

    /**
     * Obtém uma mensagem traduzida e formata parâmetros dinâmicos.
     * 
     * @param chave Chave no .properties
     * @param argumentos Argumentos para substituir nos placeholders {0}, {1}, etc.
     * @return Texto formatado com as variáveis inseridas
     */
    public static String getComParametros(String chave, Object... argumentos) {
        String mensagem = get(chave);
        try {
            return MessageFormat.format(mensagem, argumentos);
        } catch (Exception e) {
            return mensagem;
        }
    }
}
