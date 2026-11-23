package br.ftcoin.controller;

import br.ftcoin.util.I18n;
import br.ftcoin.view.AjudaView;
import br.ftcoin.view.ConsoleUtils;

import java.util.Objects;

/**
 * Controlador responsável por gerenciar a exibição das instruções de uso
 * e dos créditos da equipe de desenvolvimento do sistema FT_Coin.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class AjudaController {

    /** Camada de visualização que renderiza a ajuda e os créditos no console. */
    private final AjudaView view;

    /**
     * Constrói o controlador de ajuda com o utilitário de console fornecido
     * e cria sua camada de visualização ({@link AjudaView}).
     *
     * @param consoleUtils instância centralizada para interação e formatação na CLI
     * @throws NullPointerException se {@code consoleUtils} for nulo.
     */
    public AjudaController(ConsoleUtils consoleUtils) {
        ConsoleUtils console = Objects.requireNonNull(consoleUtils, () -> I18n.get("generic.error.nullParameter"));
        this.view = new AjudaView(console);
    }

    /**
     * Solicita à camada de visualização que exiba as instruções de uso
     * e os créditos do sistema no idioma atualmente configurado.
     */
    public void exibirMenuAjuda() {
        view.exibirAjudaECreditos();
    }
}