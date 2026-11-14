package br.ftcoin.view;

import br.ftcoin.controller.MovimentacaoController;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.util.I18n;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

/**
 * Interface de linha de comando (CLI) responsável pelas telas de interação
 * do módulo de Movimentações Financeiras (Compra, Venda e Cotação de moedas virtuais).
 *
 * <p>Atua estritamente na camada de apresentação, delegando validações de saldo,
 * existência de carteira e consultas ao Oráculo ao {@link MovimentacaoController}.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class MovimentacaoView {

    private static final List<String> OPCOES = List.of(
            "transactionMenu.buyCoin",
            "transactionMenu.sellCoin",
            "transactionMenu.quoteCoin"
    );

    private final MovimentacaoController controller;
    private final ConsoleUtils console;
    private final MenuView menu;

    /**
     * Constrói a view de movimentações injetando o utilitário de console e o controlador responsável.
     *
     * @param console    Utilitário centralizado para entrada/saída na CLI.
     * @param controller Controlador de regras de negócio de {@link Movimentacao}.
     * @throws NullPointerException se {@code console} ou {@code controller} forem nulos.
     */
    public MovimentacaoView(ConsoleUtils console, MovimentacaoController controller) {
        this.console = Objects.requireNonNull(console, () -> I18n.get("generic.error.nullParameter"));
        this.controller = Objects.requireNonNull(controller, () -> I18n.get("generic.error.nullParameter"));
        this.menu = new MenuView(this.console);
    }

    /**
     * Exibe o loop interativo do submenu de Movimentações e direciona cada opção
     * escolhida pelo usuário para o fluxo correspondente.
     */
    public void exibirMenu() {
        int opcao;
        do {
            opcao = menu.exibirEObterOpcao("transactionMenu.title", OPCOES);

            try {
                switch (opcao) {
                    case 1 -> registrarOperacao(TipoOperacao.COMPRA);
                    case 2 -> registrarOperacao(TipoOperacao.VENDA);
                    case 3 -> consultarCotacao();
                    default -> { /* 0 = voltar */ }
                }
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

    private void registrarOperacao(TipoOperacao tipo) {
        int idCarteira = console.lerInteiro(I18n.get("walletView.prompt.id"));
        LocalDate data = lerDataOpcional(I18n.get("transactionView.prompt.date"));
        BigDecimal quantidade = lerDecimal(I18n.get("transactionView.prompt.quantity"));

        Movimentacao m = controller.cadastrar(idCarteira, data, tipo, quantidade);

        String chaveSucesso = (tipo == TipoOperacao.COMPRA)
                ? "transaction.buy.success"
                : "transaction.sell.success";
        console.exibirSucesso(I18n.get(chaveSucesso));
        System.out.println(formatar(m));
    }

    private void consultarCotacao() {
        LocalDate data = lerDataOpcional(I18n.get("transactionView.prompt.quoteDate"));
        Cotacao cotacao = controller.consultarCotacao(data);

        System.out.println(ConsoleUtils.NEGRITO + I18n.get("report.oracle.display.title") + ConsoleUtils.RESET);
        System.out.println(I18n.get("report.oracle.display.date") + " " + cotacao.getData());
        System.out.println(I18n.get("report.oracle.display.price") + " R$ " + cotacao.getValorCotacao());
    }

    private BigDecimal lerDecimal(String prompt) {
        while (true) {
            String entrada = console.lerString(prompt);
            try {
                return new BigDecimal(entrada.trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                console.exibirErro(I18n.get("transactionView.error.invalidDecimal"));
            }
        }
    }

    private LocalDate lerDataOpcional(String prompt) {
        while (true) {
            String entrada = console.lerStringOpcional(prompt);
            if (entrada == null || entrada.isBlank()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(entrada.trim());
            } catch (DateTimeParseException e) {
                console.exibirErro(I18n.get("transactionView.error.invalidDate"));
            }
        }
    }

    private static String formatar(Movimentacao m) {
        return I18n.getComParametros("transactionView.item",
                String.valueOf(m.getIdMovimento()),
                String.valueOf(m.getIdCarteira()),
                m.getDataOperacao().toString(),
                m.getTipoOperacao().name(),
                m.getQuantidade().toPlainString());
    }
}