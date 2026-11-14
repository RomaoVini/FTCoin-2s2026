package br.ftcoin.view;

import br.ftcoin.util.I18n;

import java.util.Objects;

/**
 * Interface de linha de comando (CLI) responsável por renderizar as instruções
 * de uso e os créditos da equipe do sistema FT_Coin.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class AjudaView {

    private final ConsoleUtils consoleUtils;

    /**
     * Constrói a view de ajuda injetando o utilitário de console.
     *
     * @param consoleUtils Instância centralizada para interação e formatação na CLI.
     * @throws NullPointerException se {@code consoleUtils} for nulo.
     */
    public AjudaView(ConsoleUtils consoleUtils) {
        this.consoleUtils = Objects.requireNonNull(consoleUtils, () -> I18n.get("generic.error.nullParameter"));
    }

    /**
     * Renderiza o manual resumido de operações (Carteira, Movimentação e Relatórios),
     * dicas de uso e os créditos do projeto no idioma ativo, aguardando confirmação para retornar.
     */
    public void exibirAjudaECreditos() {
        System.out.println();
        System.out.println(ConsoleUtils.NEGRITO + ConsoleUtils.AZUL + I18n.get("help.instructions.header") + ConsoleUtils.RESET);
        System.out.println(ConsoleUtils.NEGRITO + I18n.get("help.instructions.title") + ConsoleUtils.RESET);
        System.out.println("  • " + I18n.get("help.instructions.wallet"));
        System.out.println("  • " + I18n.get("help.instructions.transaction"));
        System.out.println("  • " + I18n.get("help.instructions.reports"));
        System.out.println();
        System.out.println(ConsoleUtils.AMARELO + I18n.get("help.instructions.tip") + ConsoleUtils.RESET);
        System.out.println();

        System.out.println(ConsoleUtils.NEGRITO + ConsoleUtils.AZUL + I18n.get("help.credits.header") + ConsoleUtils.RESET);
        System.out.println("  " + I18n.get("help.credits.team"));
        System.out.println("  " + I18n.get("help.credits.version"));
        System.out.println();

        consoleUtils.pausar();
    }
}