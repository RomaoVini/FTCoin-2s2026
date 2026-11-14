package br.ftcoin.controller;

import br.ftcoin.util.I18n;
import br.ftcoin.view.ConsoleUtils;

import java.util.Objects;

/**
 * Controlador responsável por gerenciar a exibição das instruções de uso
 * e os créditos da equipe de desenvolvimento do sistema FT_Coin.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class AjudaController {

    private final ConsoleUtils consoleUtils;

    /**
     * Constrói o controlador de ajuda injetando o utilitário de console.
     *
     * @param consoleUtils Instância centralizada para interação e formatação na CLI.
     * @throws NullPointerException se {@code consoleUtils} for nulo.
     */
    public AjudaController(ConsoleUtils consoleUtils) {
        this.consoleUtils = Objects.requireNonNull(consoleUtils, "ConsoleUtils não pode ser nulo.");
    }

    /**
     * Renderiza na interface de linha de comando (CLI) o manual resumido de
     * operações (Carteira, Movimentação e Relatórios), dicas de uso e os
     * créditos do projeto no idioma ativo, aguardando confirmação para retornar.
     */
    public void exibirMenuAjuda() {
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