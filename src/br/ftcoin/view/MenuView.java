package br.ftcoin.view;

import br.ftcoin.util.I18n;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Componente visual genérico e reutilizável para renderização de menus na interface
 * de linha de comando (CLI) e captura validada da opção selecionada pelo usuário.
 *
 * <p>Suporta internacionalização dinâmica via {@link I18n} e padroniza a opção {@code 0}
 * como "Sair" no Menu Principal e "Voltar" nos submenus.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class MenuView {

    private final ConsoleUtils consoleUtils;

    /**
     * Constrói uma {@link MenuView} criando internamente uma instância de {@link ConsoleUtils}
     * a partir do {@link Scanner} fornecido.
     *
     * @param scanner Leitor de entrada padrão da aplicação.
     * @throws NullPointerException se {@code scanner} for nulo.
     */
    public MenuView(Scanner scanner) {
        this(new ConsoleUtils(Objects.requireNonNull(scanner, () -> I18n.get("generic.error.nullParameter"))));
    }

    /**
     * Constrói uma {@link MenuView} reutilizando uma instância existente de {@link ConsoleUtils}.
     *
     * @param consoleUtils Utilitário centralizado de entrada/saída no console.
     * @throws NullPointerException se {@code consoleUtils} for nulo.
     */
    public MenuView(ConsoleUtils consoleUtils) {
        this.consoleUtils = Objects.requireNonNull(consoleUtils, () -> I18n.get("generic.error.nullParameter"));
    }

    /**
     * Renderiza o cabeçalho do menu, lista as opções numeradas dinamicamente (de 1 a N),
     * exibe a opção {@code 0} (Sair ou Voltar) e solicita uma escolha válida ao usuário.
     *
     * @param chaveTitulo  Chave mapeada no {@link I18n} para o título do menu.
     * @param chavesOpcoes Lista ordenada com as chaves {@link I18n} das opções numeradas.
     * @return Número inteiro da opção selecionada ({@code 1} a {@code N}, ou {@code 0} para Sair/Voltar).
     */
    public int exibirEObterOpcao(String chaveTitulo, List<String> chavesOpcoes) {
        Objects.requireNonNull(chavesOpcoes, () -> I18n.get("menu.error.nullOptions"));

        while (true) {
            System.out.println();
            System.out.println(ConsoleUtils.NEGRITO + I18n.get(chaveTitulo) + ConsoleUtils.RESET);

            for (int i = 0; i < chavesOpcoes.size(); i++) {
                int numero = i + 1;
                String textoOpcao = I18n.get(chavesOpcoes.get(i));
                System.out.printf("  %s%d.%s %s%n", ConsoleUtils.AZUL, numero, ConsoleUtils.RESET, textoOpcao);
            }

            // No Menu Principal a opção 0 exibe "Sair"; nos submenus exibe "Voltar"
            String chaveZero = "mainMenu.title".equals(chaveTitulo)
                    ? "mainMenu.exit"
                    : "helpMenu.return";
            System.out.printf("  %s0.%s %s%n%n", ConsoleUtils.AMARELO, ConsoleUtils.RESET, I18n.get(chaveZero));

            int escolha = consoleUtils.lerInteiro(I18n.get("mainMenu.prompt"));

            if (escolha >= 0 && escolha <= chavesOpcoes.size()) {
                return escolha;
            }

            consoleUtils.exibirErro(I18n.getComParametros("menu.invalidInput", chavesOpcoes.size()));
        }
    }

    /**
     * Retorna a instância de {@link ConsoleUtils} associada a esta view.
     *
     * @return Utilitário de console ativo.
     */
    public ConsoleUtils getConsoleUtils() {
        return consoleUtils;
    }
}