package br.ftcoin.view;

import br.ftcoin.controller.RelatorioController;
import br.ftcoin.dto.RelatorioCarteiraDTO;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Carteira;
import br.ftcoin.service.OraculoException;
import br.ftcoin.util.I18n;

import java.util.List;
import java.util.Objects;

/**
 * Telas dos relatórios de carteira.
 *
 * <p>Responsável apenas pela apresentação e pela interação com o usuário:
 * solicita os dados ao {@link RelatorioController} e os exibe. Não realiza
 * cálculos nem aplica regras de negócio. Erros tratados pelo controlador
 * ({@link ValidacaoException} e {@link OraculoException}) são exibidos como
 * mensagens amigáveis, sem interromper o programa.</p>
 *
 * @author Equipe FT_Coin
 * @version 1.0
 * @see RelatorioController
 * @see RelatorioCarteiraDTO
 */
public class RelatorioView {

    /** Chaves {@link I18n} das opções numeradas exibidas no submenu de Relatórios (de 1 a 5). */
    private static final List<String> OPCOES = List.of(
            "reportMenu.sortedWalletsById",
            "reportMenu.sortedWalletsByHolder",
            "reportMenu.walletBalance",
            "reportMenu.walletHistory",
            "reportMenu.walletGainLoss"
    );

    /** Controlador que fornece os dados já calculados e formatados dos relatórios. */
    private final RelatorioController controller;

    /** Utilitário de console para leitura de entradas e exibição de mensagens formatadas. */
    private final ConsoleUtils console;

    /** Componente de menu reutilizável usado para renderizar o submenu de Relatórios. */
    private final MenuView menu;

    /**
     * Cria a view de relatórios.
     *
     * @param console    utilitário de entrada e saída no console
     * @param controller controlador que fornece os dados dos relatórios
     * @throws NullPointerException se algum argumento for nulo
     */
    public RelatorioView(ConsoleUtils console, RelatorioController controller) {
        this.console = Objects.requireNonNull(console);
        this.controller = Objects.requireNonNull(controller);
        this.menu = new MenuView(this.console);
    }

    /**
     * Exibe o menu de relatórios e trata as escolhas do usuário até que ele
     * volte ao menu principal (opção 0).
     *
     * <p>Erros de validação e falhas do Oráculo lançados pelo controlador são
     * exibidos ao usuário sem encerrar o loop.</p>
     */
    public void exibirMenu() {
        int opcao;
        do {
            opcao = menu.exibirEObterOpcao("reportMenu.title", OPCOES);
            try {
                switch (opcao) {
                    case 1 -> exibirCarteiras("report.wallets.byId.title", controller.listarCarteirasPorId());
                    case 2 -> exibirCarteiras("report.wallets.byHolder.title", controller.listarCarteirasPorTitular());
                    case 3 -> exibirSaldo(controller.gerarSaldoAtual(lerId("report.wallet.balance.prompt")));
                    case 4 -> exibirHistorico(controller.gerarHistorico(lerId("report.wallet.history.prompt")));
                    case 5 -> exibirGanhoPerda(controller.gerarGanhoPerdaTodas());
                    default -> { }
                }
            } catch (ValidacaoException | OraculoException e) {
                console.exibirErro(e.getMessage());
            }
            if (opcao != 0) console.pausar();
        } while (opcao != 0);
    }

    /**
     * Solicita ao usuário o identificador de uma carteira.
     *
     * @param chavePrompt chave {@link I18n} do texto de instrução exibido antes da leitura
     * @return identificador inteiro informado pelo usuário
     */
    private int lerId(String chavePrompt) {
        return console.lerInteiro(I18n.get(chavePrompt));
    }

    /**
     * Exibe uma tabela com as carteiras informadas (identificador, titular e corretora).
     * Se a lista estiver vazia, exibe uma mensagem de erro.
     *
     * @param chaveTitulo chave {@link I18n} do título da listagem
     * @param carteiras   carteiras já ordenadas pelo controlador
     */
    private void exibirCarteiras(String chaveTitulo, List<Carteira> carteiras) {
        if (carteiras.isEmpty()) {
            console.exibirErro(I18n.get("report.wallets.empty"));
            return;
        }
        System.out.println(ConsoleUtils.NEGRITO + I18n.get(chaveTitulo) + ConsoleUtils.RESET);
        System.out.printf("%-6s %-30s %-20s%n",
                I18n.get("report.wallet.table.id"),
                I18n.get("report.wallet.table.holder"),
                I18n.get("report.wallet.table.broker"));
        for (Carteira c : carteiras) {
            System.out.printf("%-6d %-30s %-20s%n", c.getId(), c.getTitular(), c.getCorretora());
        }
    }

    /**
     * Exibe o relatório de saldo atual de uma carteira (quantidade de FTCoins e saldo financeiro).
     *
     * @param d dados do saldo, já formatados pelo controlador
     */
    private void exibirSaldo(RelatorioCarteiraDTO d) {
        System.out.println(ConsoleUtils.NEGRITO + I18n.get("report.wallet.currentBalance.title") + ConsoleUtils.RESET);
        System.out.println(I18n.get("report.wallet.currentBalance.id") + " " + d.id());
        System.out.println(I18n.get("report.wallet.currentBalance.holder") + " " + d.titular());
        System.out.println(I18n.get("report.wallet.currentBalance.broker") + " " + d.corretora());
        System.out.println(I18n.get("report.wallet.currentBalance.balance") + " " + d.saldoQuantidade());
        System.out.println(I18n.get("report.wallet.currentBalance.financialBalance") + " " + d.saldoFinanceiro());
    }

    /**
     * Exibe o histórico de movimentações de uma carteira em formato de tabela.
     * Se não houver movimentações, exibe um alerta após os dados da carteira.
     *
     * @param h histórico da carteira, já formatado pelo controlador
     */
    private void exibirHistorico(RelatorioCarteiraDTO.Historico h) {
        System.out.println(ConsoleUtils.NEGRITO + I18n.get("report.wallet.history.title") + ConsoleUtils.RESET);
        System.out.println(I18n.get("report.wallet.history.id") + " " + h.id());
        System.out.println(I18n.get("report.wallet.history.holder") + " " + h.titular());
        System.out.println(I18n.get("report.wallet.history.broker") + " " + h.corretora());

        if (h.movimentos().isEmpty()) {
            console.exibirAlerta(I18n.get("report.wallet.history.empty"));
            return;
        }
        System.out.printf("%-12s %-10s %-18s %-15s%n",
                I18n.get("report.wallet.history.table.date"),
                I18n.get("report.wallet.history.table.type"),
                I18n.get("report.wallet.history.table.quantity"),
                I18n.get("report.wallet.history.table.quote"));
        for (RelatorioCarteiraDTO.Movimento m : h.movimentos()) {
            System.out.printf("%-12s %-10s %-18s %-15s%n",
                    m.data(), I18n.get(m.tipoChave()), m.quantidade(), m.cotacao());
        }
    }

    /**
     * Exibe o relatório de ganho ou perda de todas as carteiras: a apuração
     * operação a operação e a valoração a mercado de cada uma. Se alguma carteira
     * usou uma cotação desatualizada (falha do Oráculo), exibe um alerta ao final.
     *
     * @param carteiras relatórios de ganho/perda, um por carteira, já formatados pelo controlador
     */
    private void exibirGanhoPerda(List<RelatorioCarteiraDTO.GanhoPerda> carteiras) {
        System.out.println(ConsoleUtils.NEGRITO + I18n.get("report.wallet.gainLoss.title") + ConsoleUtils.RESET);
        boolean desatualizada = false;

        for (RelatorioCarteiraDTO.GanhoPerda g : carteiras) {
            System.out.println();
            System.out.println(I18n.get("report.wallet.currentBalance.id") + " " + g.id()
                    + "  " + I18n.get("report.wallet.currentBalance.holder") + " " + g.titular());

            if (!g.operacoes().isEmpty()) {
                String formato = "%-12s %-8s %-12s %-14s %-12s %-16s %-16s %-14s%n";
                System.out.printf(formato,
                        I18n.get("report.wallet.history.table.date"),
                        I18n.get("report.wallet.history.table.type"),
                        I18n.get("report.wallet.history.table.quantity"),
                        I18n.get("report.wallet.gainLoss.table.price"),
                        I18n.get("report.wallet.gainLoss.table.balance"),
                        I18n.get("report.wallet.gainLoss.table.operationResult"),
                        I18n.get("report.wallet.gainLoss.table.accumulatedResult"),
                        I18n.get("report.wallet.gainLoss.table.averageCost"));
                for (RelatorioCarteiraDTO.OperacaoApurada op : g.operacoes()) {
                    System.out.printf(formato, op.data(), I18n.get(op.tipoChave()), op.quantidade(),
                            op.preco(), op.saldo(), op.resultadoOperacao(),
                            op.resultadoAcumulado(), op.custoMedio());
                }
            }

            System.out.println(I18n.get("report.wallet.gainLoss.table.price") + " "
                    + g.cotacaoAtual() + " (" + g.dataCotacao() + ")");
            System.out.println(I18n.get("report.financial.result") + " " + g.quantidade());
            System.out.println(I18n.get("report.wallet.gainLoss.table.averageCost") + ": " + g.custoMedio());
            System.out.println(I18n.get("report.wallet.gainLoss.table.accumulatedResult") + ": " + g.resultadoAcumulado());
            System.out.println(I18n.get("report.wallet.gainLoss.marketBalance") + " " + g.saldoMercado());
            System.out.println(I18n.get("report.financial.gainLoss") + " " + g.resultadoPotencial());
            desatualizada |= g.cotacaoDesatualizada();
        }

        if (desatualizada) {
            console.exibirAlerta(I18n.get("report.wallet.gainLoss.staleQuote"));
        }
    }
}