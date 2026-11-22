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
import br.ftcoin.util.I18n;
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
 * os cálculos e entrega à {@link RelatorioView} dados já formatados conforme o
 * idioma ativo em {@link RelatorioCarteiraDTO}.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 * @see RelatorioView
 * @see RelatorioCarteiraDTO
 */
public final class RelatorioController {

    /** Critério de ordenação das carteiras por identificador crescente. */
    private static final Comparator<Carteira> POR_ID =
            Comparator.comparingInt(Carteira::getId);

    /** Acesso aos dados das carteiras. */
    private final CarteiraDAO carteiraDAO;

    /** Acesso aos dados das movimentações. */
    private final MovimentacaoDAO movimentacaoDAO;

    /** Fonte de cotações históricas e atuais da FTCoin. */
    private final OraculoCotacao oraculo;

    /** Serviço do Model responsável pelos cálculos financeiros da carteira. */
    private final CalculoFinanceiroService calculo;

    /** View de relatórios associada a este controlador. */
    private final RelatorioView view;

    /** Última cotação atual obtida com sucesso; serve de fallback se o Oráculo falhar. */
    private Cotacao ultimaCotacaoConhecida;

    /**
     * Cria o controlador e a View de relatórios associada a ele.
     *
     * @param menuView        View de menus, da qual se reaproveita o console.
     * @param carteiraDAO     Acesso aos dados das carteiras.
     * @param movimentacaoDAO Acesso aos dados das movimentações.
     * @param oraculo         Fonte de cotações da FTCoin.
     * @throws NullPointerException se algum argumento for nulo.
     */
    public RelatorioController(MenuView menuView, CarteiraDAO carteiraDAO,
                               MovimentacaoDAO movimentacaoDAO, OraculoCotacao oraculo) {
        Objects.requireNonNull(menuView, () -> I18n.get("generic.error.nullParameter"));
        this.carteiraDAO = Objects.requireNonNull(carteiraDAO, () -> I18n.get("generic.error.nullParameter"));
        this.movimentacaoDAO = Objects.requireNonNull(movimentacaoDAO, () -> I18n.get("generic.error.nullParameter"));
        this.oraculo = Objects.requireNonNull(oraculo, () -> I18n.get("generic.error.nullParameter"));
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
     * @return Nova lista ordenada (vazia se não houver carteiras).
     */
    public List<Carteira> listarCarteirasPorId() {
        return ordenar(POR_ID);
    }

    /**
     * Lista as carteiras cadastradas em ordem alfabética do nome do titular
     * respeitando as regras de colação do {@link Locale} ativo.
     *
     * @return Nova lista ordenada (vazia se não houver carteiras).
     */
    public List<Carteira> listarCarteirasPorTitular() {
        Comparator<Carteira> porTitularDinamico =
                Comparator.comparing(Carteira::getTitular, criarCollator())
                          .thenComparingInt(Carteira::getId);
        return ordenar(porTitularDinamico);
    }

    /**
     * Obtém todas as carteiras do DAO e as ordena segundo o critério informado,
     * sem alterar a coleção original.
     *
     * @param criterio Comparador que define a ordem das carteiras.
     * @return Nova lista ordenada (vazia se não houver carteiras).
     */
    private List<Carteira> ordenar(Comparator<Carteira> criterio) {
        List<Carteira> lista = new ArrayList<>(carteiraDAO.consultarTodos());
        lista.sort(criterio);
        return lista;
    }

    // ---------- Saldo atual da carteira ----------

    /**
     * Gera o relatório de saldo atual de uma carteira: quantidade de FTCoins
     * e valor a custo médio, formatados no idioma ativo.
     *
     * @param idCarteira Identificador da carteira.
     * @return Saldo atual já formatado.
     * @throws ValidacaoException se a carteira não existir.
     * @throws OraculoException   se o Oráculo falhar ao fornecer as cotações históricas.
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
     * ordem cronológica, com datas e cotações formatadas conforme o idioma ativo.
     *
     * @param idCarteira Identificador da carteira.
     * @return Histórico formatado; a lista de movimentos é vazia se não houver operações.
     * @throws ValidacaoException se a carteira não existir.
     * @throws OraculoException   se o Oráculo falhar ao fornecer uma cotação.
     */
    public RelatorioCarteiraDTO.Historico gerarHistorico(int idCarteira) {
        Carteira carteira = buscarCarteira(idCarteira);

        List<RelatorioCarteiraDTO.Movimento> linhas = new ArrayList<>();
        for (Movimentacao m : movimentacoesEmOrdem(idCarteira)) {
            Cotacao cotacao = oraculo.obterCotacao(m.getDataOperacao());
            linhas.add(new RelatorioCarteiraDTO.Movimento(
                    formatarData(m.getDataOperacao()),
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
     * @return Um relatório por carteira.
     * @throws ValidacaoException se não houver carteiras cadastradas.
     * @throws OraculoException   se o Oráculo falhar e não houver cotação anterior em cache.
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
                throw e;
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

    /**
     * Apura o ganho ou perda de uma única carteira: processa suas movimentações em
     * ordem cronológica, registra cada operação no Model e valoriza a posição final
     * pela cotação atual.
     *
     * @param carteira      Carteira a ser apurada.
     * @param atual         Cotação atual usada na valoração a mercado.
     * @param desatualizada {@code true} se a cotação atual veio do cache por falha do Oráculo.
     * @return Relatório de ganho/perda da carteira, já formatado no idioma ativo.
     * @throws OraculoException se o Oráculo falhar ao fornecer a cotação de alguma operação.
     */
    private RelatorioCarteiraDTO.GanhoPerda apurar(Carteira carteira, Cotacao atual, boolean desatualizada) {
        CarteiraFinanceira financeiro = new CarteiraFinanceira();
        List<RelatorioCarteiraDTO.OperacaoApurada> operacoes = new ArrayList<>();

        for (Movimentacao m : movimentacoesEmOrdem(carteira.getId())) {
            Cotacao cotacao = oraculo.obterCotacao(m.getDataOperacao());
            financeiro.registrarMovimentacao(m, cotacao);
            operacoes.add(new RelatorioCarteiraDTO.OperacaoApurada(
                    formatarData(m.getDataOperacao()),
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
                formatarData(atual.getData()),
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

    private static Locale localeAtivo() {
        return (I18n.getIdiomaAtual() != null)
                ? I18n.getIdiomaAtual().getLocale()
                : Locale.getDefault();
    }

    /**
     * Formata uma data conforme a convenção regional do idioma ativo
     * ({@code MM/dd/yyyy} para inglês e {@code dd/MM/yyyy} para português e espanhol).
     *
     * @param data Data a ser formatada.
     * @return Data formatada como texto.
     */
    private static String formatarData(LocalDate data) {
        Locale locale = localeAtivo();
        String padrao = "en".equalsIgnoreCase(locale.getLanguage()) ? "MM/dd/yyyy" : "dd/MM/yyyy";
        return data.format(DateTimeFormatter.ofPattern(padrao, locale));
    }

    /**
     * Formata uma quantidade de FTCoins usando os separadores numéricos do idioma ativo.
     *
     * @param valor Quantidade a ser formatada.
     * @return Quantidade formatada.
     */
    private static String quantidade(BigDecimal valor) {
        NumberFormat nf = NumberFormat.getNumberInstance(localeAtivo());
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(8);
        return nf.format(valor.stripTrailingZeros());
    }

    /**
     * Formata um valor monetário segundo o padrão regional do idioma ativo
     * (ex.: {@code R$ 1.234,56} em pt-BR, {@code $1,234.56} em en-US, {@code 1.234,56 €} em es-ES).
     *
     * @param valor Valor a ser formatado.
     * @return Valor monetário formatado.
     */
    private static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(localeAtivo()).format(valor);
    }

    /**
     * Cria o {@link Collator} configurado para o idioma ativo, com força
     * {@link Collator#PRIMARY} para ignorar acentos e maiúsculas na ordenação.
     *
     * @return Collator configurado para o {@link Locale} ativo.
     */
    private static Collator criarCollator() {
        Collator c = Collator.getInstance(localeAtivo());
        c.setStrength(Collator.PRIMARY);
        return c;
    }
}