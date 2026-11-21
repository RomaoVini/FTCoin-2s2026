package br.ftcoin.enums;

import br.ftcoin.exception.ValidacaoException;

import java.util.Arrays;
import java.util.List;

/**
 * Enumeração que representa as opções disponíveis no Menu Principal do sistema FT_Coin,
 * associando o código numérico digitado pelo usuário à sua respectiva chave de
 * internacionalização ({@code I18n}).
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public enum MenuOperacao {

    CARTEIRA(1, "mainMenu.wallet"),
    MOVIMENTACAO(2, "mainMenu.transaction"),
    RELATORIOS(3, "mainMenu.reports"),
    AJUDA(4, "mainMenu.help"),
    IDIOMA(5, "mainMenu.language"),
    SAIR(0, "mainMenu.exit");

    private final int codigo;
    private final String chaveI18n;

    MenuOperacao(int codigo, String chaveI18n) {
        this.codigo = codigo;
        this.chaveI18n = chaveI18n;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getChaveI18n() {
        return chaveI18n;
    }

    /**
     * Converte o número inteiro digitado pelo usuário na constante {@link MenuOperacao} correspondente.
     *
     * @param codigo Número informado na entrada padrão.
     * @return Instância de {@link MenuOperacao} correspondente.
     * @throws ValidacaoException se o código não corresponder a nenhuma opção válida do menu.
     */
    public static MenuOperacao fromCodigo(int codigo) {
        for (MenuOperacao operacao : values()) {
            if (operacao.codigo == codigo) {
                return operacao;
            }
        }
        throw new ValidacaoException("menu.invalidInput", String.valueOf(values().length - 1));
    }

    /**
     * Retorna a lista ordenada das chaves {@code I18n} das opções numeradas (1 a 5),
     * excluindo {@link #SAIR} (renderizada automaticamente como opção {@code 0} pela {@code MenuView}).
     *
     * @return Lista imutável de chaves para renderização do Menu Principal.
     */
    public static List<String> obterChavesMenuPrincipal() {
        return Arrays.stream(values())
                .filter(op -> op != SAIR)
                .map(MenuOperacao::getChaveI18n)
                .toList();
    }
}