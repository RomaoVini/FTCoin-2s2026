package br.ftcoin.controller;

import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.dao.MovimentacaoDAO;
import br.ftcoin.dto.RelatorioCarteiraDTO;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Carteira;
import br.ftcoin.model.CarteiraFinanceira;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.service.CalculoFinanceiroService;
import br.ftcoin.service.OraculoCotacao;
import br.ftcoin.service.OraculoException;
import br.ftcoin.view.MenuView;
import br.ftcoin.view.RelatorioView;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Controlador dos relatórios de carteira.
 *
 * <p>Orquestra as camadas do padrão MVC: busca os dados nos DAOs, aciona o
 * Model ({@link CarteiraFinanceira} e {@link CalculoFinanceiroService}) para
 * os cálculos e entrega à {@link RelatorioView} dados já formatados em
 * {@link RelatorioCarteiraDTO}. Este controlador não realiza cálculos de
 * negócio: apenas ordena, orquestra e formata.</p>
 *
 * @author Equipe FT_Coin
 * @version 1.0
 */
public final class RelatorioController {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Comparator<Carteira> POR_ID =
            Comparator.comparingInt(Carteira::getId);

    // PRIMARY ignora acento e maiúscula; o id desempata nomes repetidos
    private static final Comparator<Carteira> POR_TITULAR =
            Comparator.comparing(Carteira::getTitular, criarCollator())
                      .thenComparingInt(Carteira::getId);

    private final CarteiraDAO carteiraDAO;
    private final MovimentacaoDAO movimentacaoDAO;
    private final OraculoCotacao oraculo;
    private final CalculoFinanceiroService calculo;
    private final RelatorioView view;

    /** Última cotação atual obtida com sucesso; serve de fallback se o Oráculo falhar. */
    private Cotacao ultimaCotacaoConhecida;

    /**
     * Cria o controlador e a View de relatórios associada a ele.
     *
     * @param menuView        view de menus, da qual se reaproveita o console
     * @param carteiraDAO     acesso aos dados das carteiras
     * @param movimentacaoDAO acesso aos dados das movimentações
     * @param oraculo         fonte de cotações da FTCoin
     * @throws NullPointerException se algum argumento for nulo
     */
    public RelatorioController(MenuView menuView, CarteiraDAO carteiraDAO,
                               MovimentacaoDAO movimentacaoDAO, OraculoCotacao oraculo) {
        Objects.requireNonNull(menuView, "MenuView não pode ser nula.");
        this.carteiraDAO = Objects.requireNonNull(carteiraDAO, "CarteiraDAO não pode ser nulo.");
        this.movimentacaoDAO = Objects.requireNonNull(movimentacaoDAO, "MovimentacaoDAO não pode ser nulo.");
        this.oraculo = Objects.requireNonNull(oraculo, "Oráculo não pode ser nulo.");
        this.calculo = new CalculoFinanceiroService(this.oraculo);
        this.view = new RelatorioView(menuView.getConsoleUtils(), this);
    }

    /**
     * Abre o menu de relatórios.
     */
    public void exibirMenuRelatorios() {
        view.exibirMenu();
    }

    // ---------- Listagem de carteiras ordenadas ----------

    /**
     * Lista as carteiras cadastradas em ordem numérica crescente de identificador.
     *
     * @return nova lista ordenada (vazia se não houver carteiras)
     */
    public List<Carteira> listarCarteirasPorId() {
        return ordenar(POR_ID);
    }

    /**
     * Lista as carteiras cadastradas em ordem alfabética do nome do titular.
     * A comparação ignora acentos e maiúsculas, e o identificador desempata
     * nomes repetidos.
     *
     * @return nova lista ordenada (vazia se não houver carteiras)
     */
    public List<Carteira> listarCarteirasPorTitular() {
        return ordenar(POR_TITULAR);
    }

    private List<Carteira> ordenar(Comparator<Carteira> criterio) {
        List<Carteira> lista = new ArrayList<>(carteiraDAO.consultarTodos());
        lista.sort(criterio);
        return lista;
    }

    // ---------- Saldo atual da carteira ----------

    /**
     * Gera o relatório de saldo atual de uma carteira: quantidade de FTCoins
     * e valor a custo médio. Uma carteira sem movimentações tem saldo zero e
     * não consulta o Oráculo.
     *
     * @param idCarteira identificador da carteira
     * @return saldo atual já formatado
     * @throws ValidacaoException se a carteira não existir
     * @throws OraculoException   se o Oráculo falhar ao fornecer as cotações históricas
     */
    public RelatorioCarteiraDTO gerarSaldoAtual(int idCarteira) {
        Carteira carteira = buscarCarteira(idCarteira);

        List<Movimentacao> movimentacoes = movimentacaoDAO.consultarPorCarteira(idCarteira);
        if (movimentacoes.isEmpty()) {
            return new RelatorioCarteiraDTO(carteira.getId(), carteira.getTitular(), carteira.getCorretora(),
                    quantidade(BigDecimal.ZERO), moeda(BigDecimal.ZERO));
        }

        CarteiraFinanceira financeiro = calculo.calcularCarteira(movimentacoes, LocalDate.now());
        return new RelatorioCarteiraDTO(carteira.getId(), carteira.getTitular(), carteira.getCorretora(),
                quantidade(financeiro.getSaldoQuantidade()), moeda(financeiro.getSaldoFinanceiro()));
    }

    // ---------- Histórico de movimentações ----------

    /**
     * Gera o histórico de movimentações (compras e vendas) de uma carteira em
     * ordem cronológica, com a cotação da data de cada operação.
     *
     * @param idCarteira identificador da carteira
     * @return histórico formatado; a lista de movimentos é vazia se não houver operações
     * @throws ValidacaoException se a carteira não existir
     * @throws OraculoException   se o Oráculo falhar ao fornecer uma cotação
     */
    public RelatorioCarteiraDTO.Historico gerarHistorico(int idCarteira) {
        Carteira carteira = buscarCarteira(idCarteira);

        List<RelatorioCarteiraDTO.Movimento> linhas = new ArrayList<>();
        for (Movimentacao m : movimentacoesEmOrdem(idCarteira)) {
            Cotacao cotacao = oraculo.obterCotacao(m.getDataOperacao());
            linhas.add(new RelatorioCarteiraDTO.Movimento(
                    m.getDataOperacao().format(FORMATO_DATA),
                    chaveDoTipo(m),
                    quantidade(m.getQuantidade()),
                    moeda(cotacao.getValor())));
        }
        return new RelatorioCarteiraDTO.Historico(
                carteira.getId(), carteira.getTitular(), carteira.getCorretora(), linhas);
    }

    // ---------- Ganho/perda e valoração a mercado ----------

    /**
     * Gera o relatório de ganho ou perda de todas as carteiras, em ordem de
     * identificador. Cada carteira traz a apuração operação a operação e a
     * valoração a mercado pela cotação atual.
     *
     * <p>Se o Oráculo estiver indisponível, usa a última cotação atual obtida
     * com sucesso e sinaliza o fato em
     * {@link RelatorioCarteiraDTO.GanhoPerda#cotacaoDesatualizada()}.</p>
     *
     * @return um relatório por carteira
     * @throws ValidacaoException se não houver carteiras cadastradas
     * @throws OraculoException   se o Oráculo falhar e não houver cotação anterior em cache
     */
    public List<RelatorioCarteiraDTO.GanhoPerda> gerarGanhoPerdaTodas() {
        List<Carteira> carteiras = listarCarteirasPorId();
        if (carteiras.isEmpty()) {
            throw new ValidacaoException("report.wallets.empty");
        }

        Cotacao atual;
        boolean desatualizada = false;
        try {
            atual = oraculo.obterCotacao(LocalDate.now());
            ultimaCotacaoConhecida = atual;
        } catch (OraculoException e) {
            if (ultimaCotacaoConhecida == null) {
                throw e; // sem Oráculo e sem cotação anterior: a View mostra a mensagem
            }
            atual = ultimaCotacaoConhecida;
            desatualizada = true;
        }

        List<RelatorioCarteiraDTO.GanhoPerda> relatorio = new ArrayList<>();
        for (Carteira carteira : carteiras) {
            relatorio.add(apurar(carteira, atual, desatualizada));
        }
        return relatorio;
    }

    private RelatorioCarteiraDTO.GanhoPerda apurar(Carteira carteira, Cotacao atual, boolean desatualizada) {
        CarteiraFinanceira financeiro = new CarteiraFinanceira();
        List<RelatorioCarteiraDTO.OperacaoApurada> operacoes = new ArrayList<>();

        for (Movimentacao m : movimentacoesEmOrdem(carteira.getId())) {
            Cotacao cotacao = oraculo.obterCotacao(m.getDataOperacao());
            financeiro.registrarMovimentacao(m, cotacao);
            operacoes.add(new RelatorioCarteiraDTO.OperacaoApurada(
                    m.getDataOperacao().format(FORMATO_DATA),
                    chaveDoTipo(m),
                    quantidade(m.getQuantidade()),
                    moeda(cotacao.getValor()),
                    quantidade(financeiro.getSaldoQuantidade()),
                    moeda(financeiro.getResultadoOperacional()),
                    moeda(financeiro.getResultadoAcumulado()),
                    moeda(financeiro.getCustoMedio())));
        }
        financeiro.atualizarCotacaoAtual(atual);

        return new RelatorioCarteiraDTO.GanhoPerda(
                carteira.getId(), carteira.getTitular(), carteira.getCorretora(),
                operacoes,
                quantidade(financeiro.getSaldoQuantidade()),
                moeda(financeiro.getCustoMedio()),
                moeda(financeiro.getResultadoAcumulado()),
                moeda(financeiro.getSaldoMercado()),
                moeda(financeiro.getResultadoMercado()),
                moeda(atual.getValor()),
                atual.getData().format(FORMATO_DATA),
                desatualizada);
    }

    // ---------- Utilitários ----------

    private Carteira buscarCarteira(int idCarteira) {
        return carteiraDAO.consultar(idCarteira)
                .orElseThrow(() -> new ValidacaoException("wallet.error.notFound", idCarteira));
    }

    private List<Movimentacao> movimentacoesEmOrdem(int idCarteira) {
        List<Movimentacao> movimentacoes = new ArrayList<>(movimentacaoDAO.consultarPorCarteira(idCarteira));
        movimentacoes.sort(Comparator.comparing(Movimentacao::getDataOperacao)
                .thenComparingInt(Movimentacao::getIdMovimento));
        return movimentacoes;
    }

    private static String chaveDoTipo(Movimentacao movimentacao) {
        return movimentacao.isCompra() ? "report.wallet.history.type.buy" : "report.wallet.history.type.sell";
    }

    private static String quantidade(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    private static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    private static Collator criarCollator() {
        Collator c = Collator.getInstance(PT_BR);
        c.setStrength(Collator.PRIMARY);
        return c;
    }
}
