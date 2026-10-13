package br.unicamp.ft.ftcoin.enums;

import java.util.Locale;

//Enumeração que representa os idiomas suportados pelo sistema FT_Coin.

public enum Idioma {
    PORTUGUES("pt", "BR", "Português (Brasil)", Locale.of("pt", "BR")),
    INGLES("en", "US", "English", Locale.US),
    ESPANHOL("es", "ES", "Español", Locale.of("es", "ES"));

    private final String codigo;
    private final String pais;
    private final String descricao;
    private final Locale locale;

    Idioma(String codigo, String pais, String descricao, Locale locale) {
        this.codigo = codigo;
        this.pais = pais;
        this.descricao = descricao;
        this.locale = locale;
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
     * Obtém a instância de Idioma a partir da opção informada no submenu da CLI.
     * 
     * @param opcao Opção numérica (1: Português, 2: Inglês, 3: Espanhol)
     * @return O enum Idioma selecionado ou INGLES como padrão.
     */
    public static Idioma porOpcaoMenu(int opcao) {
        return switch (opcao) {
            case 1 -> PORTUGUES;
            case 2 -> INGLES;
            case 3 -> ESPANHOL;
            default -> INGLES;
        };
    }
}