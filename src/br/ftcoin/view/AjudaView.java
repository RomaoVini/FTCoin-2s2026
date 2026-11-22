package br.ftcoin.view;

import br.ftcoin.util.I18n;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Interface de linha de comando (CLI) responsável por renderizar as instruções
 * de uso, os créditos da equipe, copyright e data de acesso do sistema FT_Coin.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class AjudaView {

    private static final DateTimeFormatter FORMATO_DATA_ACESSO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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
     * Renderiza o manual resumido de operações, dicas de uso, créditos completos
     * da equipe, copyright e data de acesso no idioma ativo.
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
        System.out.println("  " + ConsoleUtils.NEGRITO + I18n.get("help.credits.team") + ConsoleUtils.RESET
                + " (" + I18n.get("help.credits.version") + ")");
        System.out.println("  • " + I18n.get("help.credits.role.sm"));
        System.out.println("  • " + I18n.get("help.credits.role.po"));
        System.out.println("  • " + I18n.get("help.credits.role.reports"));
        System.out.println("  • " + I18n.get("help.credits.role.wallet"));
        System.out.println("  • " + I18n.get("help.credits.role.transactionPersistence"));
        System.out.println("  • " + I18n.get("help.credits.role.documentation"));
        System.out.println();

        String dataHoraAtual = LocalDateTime.now().format(FORMATO_DATA_ACESSO);
        System.out.println("  " + I18n.get("help.credits.accessDate") + " " + dataHoraAtual);
        System.out.println("  " + I18n.getComParametros("help.credits.copyright", String.valueOf(Year.now().getValue())));
        System.out.println();

        consoleUtils.pausar();
    }
}