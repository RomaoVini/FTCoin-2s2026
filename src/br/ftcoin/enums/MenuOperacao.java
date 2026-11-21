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

    /** Módulo de Carteiras (opção 1): cadastro, listagem, edição e exclusão. */
    CARTEIRA(1, "mainMenu.wallet"),

    /** Módulo de Movimentações (opção 2): compra, venda e cotação de FTCoins. */
    MOVIMENTACAO(2, "mainMenu.transaction"),

    /** Módulo de Relatórios (opção 3): listagens, saldo, histórico e ganho/perda. */
    RELATORIOS(3, "mainMenu.reports"),

    /** Tela de Ajuda e Créditos (opção 4). */
    AJUDA(4, "mainMenu.help"),

    /** Troca do idioma da interface (opção 5). */
    IDIOMA(5, "mainMenu.language"),

    /** Encerramento da aplicação (opção 0). */
    SAIR(0, "mainMenu.exit");

    /** Número digitado pelo usuário para escolher a opção. */
    private final int codigo;

    /** Chave {@code I18n} do texto exibido para a opção no menu. */
    private final String chaveI18n;

    /**
     * Constrói uma opção do menu associando o código numérico à chave de internacionalização.
     *
     * @param codigo    Número digitado pelo usuário para escolher a opção.
     * @param chaveI18n Chave {@code I18n} do texto exibido no menu.
     */
    MenuOperacao(int codigo, String chaveI18n) {
        this.codigo = codigo;
        this.chaveI18n = chaveI18n;
    }

    /**
     * Retorna o código numérico associado à opção.
     *
     * @return Número que o usuário digita para escolher esta opção.
     */
    public int getCodigo() {
        return codigo;
    }

    /**
     * Retorna a chave de internacionalização do texto da opção.
     *
     * @return Chave {@code I18n} usada para obter o texto traduzido do menu.
     */
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