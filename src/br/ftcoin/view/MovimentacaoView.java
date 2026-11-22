package br.ftcoin.view;

import br.ftcoin.controller.MovimentacaoController;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.util.I18n;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
 * @see MovimentacaoController
 * @see Movimentacao
 * @see TipoOperacao
 */
public class MovimentacaoView {

    /** Chaves {@link I18n} das opções numeradas exibidas no submenu de Movimentações (de 1 a 3). */
    private static final List<String> OPCOES = List.of(
            "transactionMenu.buyCoin",
            "transactionMenu.sellCoin",
            "transactionMenu.quoteCoin"
    );

    /** Controlador que concentra as regras de negócio de movimentações e consulta de cotações. */
    /** private final MovimentacaoController controller; */

    /**Mudanças de movimentação controller de private para public para permitir acesso de outras classes*/
    public MovimentacaoController(MenuView menuView, CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO, OraculoCotacao oraculo){
        this(carteiraDAO, movimentacaoDAO, oraculo);
        this.menuView = Objects.requireNonNull(menuView, "MenuView não pode ser nula.");
        this.consoleUtils = menuView.getConsoleUtils();
        this.view = new MovimentacaoView(this.consoleUtils, this);
    }

    /** Utilitário de console para leitura de entradas e exibição de mensagens formatadas. */
    private final ConsoleUtils console;

    /** Componente de menu reutilizável usado para renderizar o submenu de Movimentações. */
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
     *
     * <p>Erros de validação e exceções de execução lançados pelo controlador são
     * capturados e exibidos ao usuário sem encerrar o loop. O loop termina quando
     * o usuário escolhe a opção {@code 0} (Voltar).</p>
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

    /**
     * Fluxo de registro de uma operação de compra ou venda: solicita o identificador da carteira,
     * a data (opcional, assume a data atual) e a quantidade, delega o cadastro ao controlador
     * e exibe a movimentação registrada.
     *
     * @param tipo Tipo da operação a ser registrada ({@link TipoOperacao#COMPRA} ou {@link TipoOperacao#VENDA}).
     */
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

    /**
     * Fluxo de consulta de cotação: solicita uma data (opcional, assume a data atual),
     * consulta o Oráculo através do controlador e exibe a data e o valor da cotação.
     */
    private void consultarCotacao() {
        LocalDate data = lerDataOpcional(I18n.get("transactionView.prompt.quoteDate"));
        Cotacao cotacao = controller.consultarCotacao(data);

        System.out.println(ConsoleUtils.NEGRITO + I18n.get("report.oracle.display.title") + ConsoleUtils.RESET);
        System.out.println(I18n.get("report.oracle.display.date") + " " + cotacao.getData());
        System.out.println(I18n.get("report.oracle.display.price") + " R$ " + cotacao.getValor());
    }

    /**
     * Solicita e lê um número decimal obrigatório, aceitando vírgula ou ponto como separador.
     * O prompt é repetido até que um valor válido seja informado.
     *
     * @param prompt Texto de instrução exibido antes da leitura.
     * @return Valor decimal convertido.
     */
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

    /**
     * Solicita e lê uma data opcional no formato ISO ({@code yyyy-MM-dd}).
     * Se o usuário apenas pressionar ENTER, a data atual é utilizada.
     * O prompt é repetido enquanto o formato informado for inválido.
     *
     * @param prompt Texto de instrução exibido antes da leitura.
     * @return Data informada pelo usuário ou {@link LocalDate#now()} se a entrada for vazia.
     */
    private static final DateTimeFormatter FORMATO_BR = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private LocalDate lerDataOpcional(String prompt) {
        while (true) {
            String entrada = console.lerStringOpcional(prompt);
            if (entrada == null || entrada.isBlank()) {
                return LocalDate.now();
            }
            try {
                String texto = entrada.trim();
                return texto.contains("/")
                        ? LocalDate.parse(texto, FORMATO_BR)
                        : LocalDate.parse(texto);
            } catch (DateTimeParseException e) {
                console.exibirErro(I18n.get("transactionView.error.invalidDate"));
            }
        }
    }

    /**
     * Formata uma {@link Movimentacao} em uma linha de texto no idioma ativo.
     *
     * @param m Movimentação a ser formatada.
     * @return Texto contendo identificador do movimento, carteira, data, tipo da operação e quantidade.
     */
    private static String formatar(Movimentacao m) {
        return I18n.getComParametros("transactionView.item",
                String.valueOf(m.getIdMovimento()),
                String.valueOf(m.getIdCarteira()),
                m.getDataOperacao().toString(),
                m.getTipoOperacao().name(),
                m.getQuantidade().toPlainString());
    }
}