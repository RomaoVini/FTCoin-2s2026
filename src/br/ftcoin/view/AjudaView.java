
package br.ftcoin.view;

import br.ftcoin.util.I18n;

import java.util.List;
import java.util.Objects;

/**
 * Interface de linha de comando (CLI) responsável pela exibição do submenu de Ajuda
 * e das informações institucionais/técnicas sobre o sistema FT_Coin.
 *
 * <p>Atua estritamente na camada de apresentação, utilizando {@link MenuView},
 * {@link ConsoleUtils} e {@link I18n} para internacionalização dinâmica.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class AjudaView {

    private static final List<String> OPCOES = List.of(
            "helpMenu.help",
            "helpMenu.about"
    );

    private final ConsoleUtils console;
    private final MenuView menu;

    /**
     * Constrói a view de ajuda reutilizando o utilitário de console centralizado.
     *
     * @param console Utilitário centralizado para entrada/saída na CLI.
     * @throws NullPointerException se {@code console} for nulo.
     */
    public AjudaView(ConsoleUtils console) {
        this.console = Objects.requireNonNull(console, () -> I18n.get("generic.error.nullParameter"));
        this.menu = new MenuView(this.console);
    }

    /**
     * Exibe o loop interativo do submenu de Ajuda e apresenta os textos explicativos
     * de uso do sistema ou as informações da equipe/versão.
     */
    public void exibirMenu() {
        int opcao;
        do {
            opcao = menu.exibirEObterOpcao("helpMenu.title", OPCOES);

            switch (opcao) {
                case 1 -> exibirAjuda();
                case 2 -> exibirSobre();
                default -> { /* 0 = voltar */ }
            }

            if (opcao != 0) {
                console.pausar();
            }
        } while (opcao != 0);
    }

    /**
     * Renderiza o guia rápido de utilização dos módulos do sistema FT_Coin.
     */
    public void exibirAjuda() {
        System.out.println();
        System.out.println(ConsoleUtils.NEGRITO + ConsoleUtils.AZUL
                + I18n.get("helpView.help.header") + ConsoleUtils.RESET);
        System.out.println(I18n.get("helpView.help.body"));
        System.out.println();
    }

    /**
     * Renderiza as informações sobre a versão, disciplina, propósito e equipe desenvolvedora.
     */
    public void exibirSobre() {
        System.out.println();
        System.out.println(ConsoleUtils.NEGRITO + ConsoleUtils.AZUL
                + I18n.get("helpView.about.header") + ConsoleUtils.RESET);
        System.out.println(I18n.get("helpView.about.body"));
        System.out.println();
    }
}