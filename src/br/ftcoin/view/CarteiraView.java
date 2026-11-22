package br.ftcoin.view;

import br.ftcoin.controller.CarteiraController;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Carteira;
import br.ftcoin.util.I18n;

import java.util.List;
import java.util.Objects;

/**
 * Interface de linha de comando (CLI) responsável pelas telas de interação
 * do módulo de Carteiras (cadastro, listagem, edição e exclusão).
 *
 * <p>Atua estritamente na camada de apresentação, delegando todas as validações
 * e regras de negócio ao {@link CarteiraController}.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 * @see CarteiraController
 * @see Carteira
 */
public class CarteiraView {

    /** Chaves {@link I18n} das opções numeradas exibidas no submenu de Carteiras (de 1 a 4). */
    private static final List<String> OPCOES = List.of(
            "walletView.option.register",
            "walletView.option.list",
            "walletView.option.edit",
            "walletView.option.delete"
    );

    /** Controlador que concentra as regras de negócio e a persistência de carteiras. */
    private final CarteiraController controller;

    /** Utilitário de console para leitura de entradas e exibição de mensagens formatadas. */
    private final ConsoleUtils console;

    /** Componente de menu reutilizável usado para renderizar o submenu de Carteiras. */
    private final MenuView menu;

    /**
     * Constrói a view de carteiras injetando o utilitário de console e o controlador responsável.
     *
     * @param console    Utilitário centralizado para entrada/saída na CLI.
     * @param controller Controlador de regras de negócio de {@link Carteira}.
     * @throws NullPointerException se {@code console} ou {@code controller} forem nulos.
     */
    public CarteiraView(ConsoleUtils console, CarteiraController controller) {
        this.console = Objects.requireNonNull(console, () -> I18n.get("generic.error.nullParameter"));
        this.controller = Objects.requireNonNull(controller, () -> I18n.get("generic.error.nullParameter"));
        this.menu = new MenuView(this.console);
    }

    /**
     * Exibe o loop interativo do submenu de Carteiras e direciona cada opção
     * escolhida pelo usuário para o fluxo correspondente.
     *
     * <p>Erros de validação e exceções de execução lançados pelo controlador são
     * capturados e exibidos ao usuário sem encerrar o loop. O loop termina quando
     * o usuário escolhe a opção {@code 0} (Voltar).</p>
     */
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
            } catch (EntradaEncerradaException e) {
                throw e;   // deixa subir até o Main
            } catch (ValidacaoException e) {
                console.exibirErro(e.getMessage());
            } catch (RuntimeException e) {
                console.exibirErro(e.getMessage());
            }

            if (opcao != 0) {
                console.pausar();
            }
        } while (opcao != 0);
    }

    /**
     * Fluxo de cadastro: solicita identificador, titular e corretora, delega o registro
     * ao controlador e exibe a carteira cadastrada.
     */
    private void cadastrar() {
        int id = console.lerInteiro(I18n.get("walletView.prompt.id"));
        String titular = console.lerString(I18n.get("walletView.prompt.holder"));
        String corretora = console.lerString(I18n.get("walletView.prompt.broker"));

        Carteira carteira = controller.cadastrar(id, titular, corretora);
        console.exibirSucesso(I18n.get("walletView.success.registered"));
        System.out.println(formatar(carteira));
    }

    /**
     * Fluxo de listagem: exibe todas as carteiras cadastradas ou um alerta
     * caso não exista nenhuma.
     */
    private void listar() {
        List<Carteira> carteiras = controller.listarTodas();
        if (carteiras.isEmpty()) {
            console.exibirAlerta(I18n.get("walletView.list.empty"));
            return;
        }
        carteiras.forEach(c -> System.out.println(formatar(c)));
    }

    /**
     * Fluxo de edição: localiza a carteira pelo identificador, mostra seus dados atuais,
     * solicita novo titular e nova corretora e delega a atualização ao controlador.
     */
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

    /**
     * Fluxo de exclusão: localiza a carteira, pede confirmação explícita ao usuário
     * e só então delega a remoção ao controlador. Caso não confirme, a operação é cancelada.
     */
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

    /**
     * Interpreta a resposta do usuário a uma pergunta de confirmação (sim/não),
     * aceitando variações em português, inglês e espanhol, sem diferenciar maiúsculas de minúsculas.
     *
     * @param resposta Texto digitado pelo usuário (pode ser nulo).
     * @return {@code true} se a resposta for afirmativa (ex.: "s", "sim", "y", "yes", "si", "sí");
     *         {@code false} caso contrário ou se for nula.
     */
    private static boolean confirmou(String resposta) {
        if (resposta == null) {
            return false;
        }
        String r = resposta.trim().toLowerCase();
        return r.equals("s") || r.equals("sim") || r.equals("y")
                || r.equals("yes") || r.equals("si") || r.equals("sí");
    }

    /**
     * Formata uma {@link Carteira} em uma linha de texto no idioma ativo.
     *
     * @param c Carteira a ser formatada.
     * @return Texto contendo identificador, titular e corretora da carteira.
     */
    private static String formatar(Carteira c) {
        return I18n.getComParametros("walletView.item",
                String.valueOf(c.getId()), c.getTitular(), c.getCorretora());
    }
}