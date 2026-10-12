package br.unicamp.ft.ftcoin.view;

import br.unicamp.ft.ftcoin.util.I18n;

import java.util.Scanner;

/**
 * Utilitário responsável por centralizar a leitura do Scanner e a formatação
 * visual na CLI.
 * Previne falhas de limpeza de buffer e fornece suporte a cores ANSI.
 */
public class ConsoleUtils {
    // Códigos ANSI para estilização de texto no terminal
    public static final String RESET = "\u001B[0m";
    public static final String VERMELHO = "\u001B[31m";
    public static final String VERDE = "\u001B[32m";
    public static final String AMARELO = "\u001B[33m";
    public static final String AZUL = "\u001B[34m";
    public static final String NEGRITO = "\u001B[1m";

    private final Scanner scanner;

    public ConsoleUtils(Scanner scanner) {
        this.scanner = scanner;
    }

    // Lê uma linha do console garantindo que não venha vazia caso seja obrigatória.
    public String lerString(String mensagemPrompt) {
        while (true) {
            System.out.print(mensagemPrompt + " ");
            String entrada = scanner.nextLine().trim();
            if (!entrada.isBlank()) {
                return entrada;
            }
            exibirErro(I18n.get("validation.required"));
        }
    }

    //Lê um valor inteiro com tratamento de exceção para evitar crash da aplicação.
    public int lerInteiro(String mensagemPrompt) {
        while (true) {
            System.out.print(mensagemPrompt + " ");
            try {
                String entrada = scanner.nextLine().trim();
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                exibirErro(I18n.get("generic.confirmInvalid"));
            }
        }
    }

    //Aguarda o usuário pressionar ENTER para continuar a execução.
    public void pausar() {
        System.out.println(I18n.get("generic.pressEnter"));
        scanner.nextLine();
    }

    public void exibirSucesso(String mensagem) {
        System.out.println(VERDE + "✔ " + mensagem + RESET);
    }

    public void exibirErro(String mensagem) {
        System.out.println(VERMELHO + "✖ " + mensagem + RESET);
    }

    public void exibirAlerta(String mensagem) {
        System.out.println(AMARELO + "⚠ " + mensagem + RESET);
    }
}
