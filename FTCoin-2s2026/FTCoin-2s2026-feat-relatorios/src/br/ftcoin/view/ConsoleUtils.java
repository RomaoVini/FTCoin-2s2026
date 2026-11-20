package br.ftcoin.view;

import br.ftcoin.util.I18n;

import java.util.Objects;
import java.util.Scanner;

/**
 * Utilitário responsável por centralizar a leitura segura da entrada padrão ({@link Scanner})
 * e a formatação visual da interface de linha de comando (CLI) com suporte a cores ANSI.
 *
 * <p>Utiliza exclusivamente {@code nextLine()} seguido de conversão explícita para prevenir
 * falhas de limpeza de buffer durante a navegação entre menus e formulários.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class ConsoleUtils {

    /** Código ANSI para restaurar a formatação padrão do terminal. */
    public static final String RESET = "\u001B[0m";

    /** Código ANSI para texto vermelho (mensagens de erro). */
    public static final String VERMELHO = "\u001B[31m";

    /** Código ANSI para texto verde (mensagens de sucesso). */
    public static final String VERDE = "\u001B[32m";

    /** Código ANSI para texto amarelo (avisos, alertas e opção de retorno). */
    public static final String AMARELO = "\u001B[33m";

    /** Código ANSI para texto azul (numeração de opções e destaques). */
    public static final String AZUL = "\u001B[34m";

    /** Código ANSI para texto em negrito (títulos e cabeçalhos). */
    public static final String NEGRITO = "\u001B[1m";

    private final Scanner scanner;

    /**
     * Constrói uma instância de {@link ConsoleUtils} encapsulando o {@link Scanner} da aplicação.
     *
     * @param scanner Leitor de entrada padrão ({@code System.in}).
     * @throws NullPointerException se {@code scanner} for nulo.
     */
    public ConsoleUtils(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, () -> I18n.get("generic.error.nullParameter"));
    }

    /**
     * Solicita e lê uma linha de texto obrigatória do console, repetindo o prompt
     * enquanto o usuário informar apenas espaços em branco.
     *
     * @param mensagemPrompt Texto de instrução exibido antes da leitura.
     * @return Texto não vazio informado pelo usuário (sem espaços nas extremidades).
     */
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

    /**
     * Solicita e lê uma linha de texto opcional do console, permitindo retorno vazio
     * (por exemplo, quando pressionar ENTER assume um valor padrão como a data atual).
     *
     * @param mensagemPrompt Texto de instrução exibido antes da leitura.
     * @return Texto informado pelo usuário (pode ser string vazia {@code ""}).
     */
    public String lerStringOpcional(String mensagemPrompt) {
        System.out.print(mensagemPrompt + " ");
        return scanner.nextLine().trim();
    }

    /**
     * Solicita e lê um número inteiro válido do console, tratando entradas não numéricas
     * sem interromper a execução do programa.
     *
     * @param mensagemPrompt Texto de instrução exibido antes da leitura.
     * @return Valor inteiro convertido.
     */
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

    /**
     * Pausa a execução da tela atual aguardando que o usuário pressione ENTER.
     */
    public void pausar() {
        System.out.println(I18n.get("generic.pressEnter"));
        scanner.nextLine();
    }

    /**
     * Exibe uma mensagem de confirmação/sucesso formatada em verde no terminal.
     *
     * @param mensagem Texto traduzido a ser exibido.
     */
    public void exibirSucesso(String mensagem) {
        System.out.println(VERDE + "✔ " + mensagem + RESET);
    }

    /**
     * Exibe uma mensagem de erro ou falha de validação formatada em vermelho no terminal.
     *
     * @param mensagem Texto traduzido a ser exibido.
     */
    public void exibirErro(String mensagem) {
        System.out.println(VERMELHO + "✖ " + mensagem + RESET);
    }

    /**
     * Exibe uma mensagem de aviso ou alerta formatada em amarelo no terminal.
     *
     * @param mensagem Texto traduzido a ser exibido.
     */
    public void exibirAlerta(String mensagem) {
        System.out.println(AMARELO + "⚠ " + mensagem + RESET);
    }
}