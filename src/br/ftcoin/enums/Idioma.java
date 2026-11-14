package br.ftcoin.enums;

import java.util.Locale;

/**
 * Enumeração que representa os idiomas suportados pelo sistema FT_Coin
 * para internacionalização (I18n) e formatação regional.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public enum Idioma {

    PORTUGUES("pt", "BR", "Português (Brasil)"),
    INGLES("en", "US", "English (US)"),
    ESPANHOL("es", "ES", "Español (España)");

    private final String codigo;
    private final String pais;
    private final String descricao;
    private final Locale locale;

    /**
     * Construtor interno do enum que inicializa os metadados regionais
     * e instancia o {@link Locale} utilizando o padrão moderno {@code Locale.of}.
     *
     * @param codigo    Código ISO 639 do idioma (ex: {@code "pt"}).
     * @param pais      Código ISO 3166 do país (ex: {@code "BR"}).
     * @param descricao Nome legível do idioma para exibição.
     */
    Idioma(String codigo, String pais, String descricao) {
        this.codigo = codigo;
        this.pais = pais;
        this.descricao = descricao;
        this.locale = Locale.of(codigo, pais);
    }

    public String getCodigo() {
        return codigo;
    }

    public String getPais() {
        return pais;
    }

    public String getDescricao() {
        return descricao;
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * Converte a opção numérica selecionada no menu de idiomas para o enum correspondente.
     *
     * @param opcao Número escolhido pelo usuário (1: Português, 2: Inglês, 3: Espanhol).
     * @return Instância de {@link Idioma} correspondente (padrão: {@link #PORTUGUES}).
     */
    public static Idioma porOpcaoMenu(int opcao) {
        return switch (opcao) {
            case 2 -> INGLES;
            case 3 -> ESPANHOL;
            default -> PORTUGUES;
        };
    }
}