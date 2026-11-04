package br.ftcoin.view;

import br.ftcoin.controller.CarteiraController;
import br.ftcoin.model.Carteira;
import br.ftcoin.util.I18n;

import java.util.List;

/**
 * Telas de Carteira (CLI): listar, incluir, editar e excluir.
 * A View só interage com o usuário e exibe resultados; toda a validação
 * e as regras ficam no CarteiraController.
 */
public class CarteiraView {

    private static final List<String> OPCOES = List.of(
            "walletView.option.register",
            "walletView.option.list",
            "walletView.option.edit",
            "walletView.option.delete");

    private final CarteiraController controller;
    private final ConsoleUtils console;
    private final MenuView menu;

    public CarteiraView(ConsoleUtils console, CarteiraController controller) {
        this.console = console;
        this.menu = new MenuView(console);
        this.controller = controller;
    }

    public void exibirMenu() {
        int opcao;
        do {
            opcao = menu.exibirEObterOpcao("walletView.title", OPCOES);

            try {
                switch (opcao) {
                    case 1 -> cadastrar();
                    case 2 -> listar();
                    case 3 -> atualizar();
                    case 4 -> excluir();
                    default -> { /* 0 = voltar */ }
                }
            } catch (RuntimeException e) {
                // ValidacaoException já traz a mensagem traduzida pelo I18n.
                console.exibirErro(e.getMessage());
            }

            if (opcao != 0) {
                console.pausar();
            }
        } while (opcao != 0);
    }

    private void cadastrar() {
        int id = console.lerInteiro(I18n.get("walletView.prompt.id"));
        String titular = console.lerString(I18n.get("walletView.prompt.holder"));
        String corretora = console.lerString(I18n.get("walletView.prompt.broker"));

        Carteira carteira = controller.cadastrar(id, titular, corretora);
        console.exibirSucesso(I18n.get("walletView.success.registered"));
        System.out.println(formatar(carteira));
    }

    private void listar() {
        List<Carteira> carteiras = controller.listarTodas();
        if (carteiras.isEmpty()) {
            console.exibirAlerta(I18n.get("walletView.list.empty"));
            return;
        }
        carteiras.forEach(c -> System.out.println(formatar(c)));
    }

    private void atualizar() {
        int id = console.lerInteiro(I18n.get("walletView.prompt.id"));
        Carteira atual = controller.buscarPorId(id);
        System.out.println(I18n.get("walletView.current") + " " + formatar(atual));

        String titular = console.lerString(I18n.get("walletView.prompt.newHolder"));
        String corretora = console.lerString(I18n.get("walletView.prompt.newBroker"));

        Carteira atualizada = controller.atualizar(id, titular, corretora);
        console.exibirSucesso(I18n.get("walletView.success.updated"));
        System.out.println(formatar(atualizada));
    }

    private void excluir() {
        int id = console.lerInteiro(I18n.get("walletView.prompt.id"));
        Carteira carteira = controller.buscarPorId(id);
        System.out.println(formatar(carteira));

        String resposta = console.lerString(
                I18n.getComParametros("walletView.confirm.delete", String.valueOf(id)));

        if (confirmou(resposta)) {
            controller.excluir(id);
            console.exibirSucesso(I18n.get("walletView.success.deleted"));
        } else {
            console.exibirAlerta(I18n.get("walletView.cancelled"));
        }
    }

    private static boolean confirmou(String resposta) {
        String r = resposta.trim().toLowerCase();
        return r.equals("s") || r.equals("sim") || r.equals("y")
                || r.equals("yes") || r.equals("si");
    }

    private static String formatar(Carteira c) {
        return I18n.getComParametros("walletView.item",
                String.valueOf(c.getId()), c.getTitular(), c.getCorretora());
    }
}