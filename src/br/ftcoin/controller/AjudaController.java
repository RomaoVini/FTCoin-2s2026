package br.ftcoin.controller;

import br.ftcoin.util.I18n;
import br.ftcoin.view.AjudaView;
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

    /** Camada de visualização que renderiza a ajuda e os créditos no console. */
    private final AjudaView view;

    /**
     * Constrói o controlador de ajuda injetando o utilitário de console
     * e instanciando sua respectiva camada de visualização ({@link AjudaView}).
     *
     * @param consoleUtils Instância centralizada para interação e formatação na CLI.
     * @throws NullPointerException se {@code consoleUtils} for nulo.
     */
    public AjudaController(ConsoleUtils consoleUtils) {
        ConsoleUtils console = Objects.requireNonNull(consoleUtils, () -> I18n.get("generic.error.nullParameter"));
        this.view = new AjudaView(console);
    }

    /**
     * Aciona a camada de visualização para exibir as instruções de uso
     * e os créditos do sistema no idioma ativo.
     */
    public void exibirMenuAjuda() {
        view.exibirAjudaECreditos();
    }
}