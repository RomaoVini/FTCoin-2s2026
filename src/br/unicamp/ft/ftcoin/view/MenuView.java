package br.unicamp.ft.ftcoin.view;

import br.unicamp.ft.ftcoin.util.I18n;

import java.util.List;
import java.util.Scanner;

/**
 * View genérica reusável para renderizar menus CLI e capturar a escolha do usuário.
 */
public class MenuView {
    private final ConsoleUtils consoleUtils;

    public MenuView(Scanner scanner) {
        this.consoleUtils = new ConsoleUtils(scanner);
    }

    public MenuView(ConsoleUtils consoleUtils) {
        this.consoleUtils = consoleUtils;
    }

    /**
     * Renderiza o cabeçalho do menu, lista as opções numeradas dinamicamente
     * e retorna a opção escolhida validada.
     * 
     * @param chaveTitulo Chave mapeada no I18n para o título
     * @param chavesOpcoes Lista com as chaves I18n das opções do submenu
     * @return Número da opção selecionada (1 a N, ou 0 para Voltar/Sair)
     */
    public int exibirEObterOpcao(String chaveTitulo, List<String> chavesOpcoes) {
        while (true) {
            // Imprime título formatado
            System.out.println(ConsoleUtils.NEGRITO + I18n.get(chaveTitulo) + ConsoleUtils.RESET);

            // Imprime opções numeradas a partir de 1
            for (int i = 0; i < chavesOpcoes.size(); i++) {
                int numero = i + 1;
                String textoOpcao = I18n.get(chavesOpcoes.get(i));
                System.out.printf("  %s%d.%s %s%n", ConsoleUtils.AZUL, numero, ConsoleUtils.RESET, textoOpcao);
            }

            // Opção 0 é padronizada como Sair
            String textoZero = chavesOpcoes.contains("mainMenu.exit") 
                    ? I18n.get("mainMenu.exit") 
                    : I18n.get("helpMenu.return");
            System.out.printf("  %s0.%s %s%n%n", ConsoleUtils.AMARELO, ConsoleUtils.RESET, textoZero);

            // Solicita a entrada e valida os limites
            int escolha = consoleUtils.lerInteiro(I18n.get("mainMenu.prompt"));

            if (escolha >= 0 && escolha <= chavesOpcoes.size()) {
                return escolha;
            }

            consoleUtils.exibirErro(I18n.getComParametros("menu.invalidInput", chavesOpcoes.size()));
            System.out.println();
        }
    }

    public ConsoleUtils getConsoleUtils() {
        return consoleUtils;
    }
}
