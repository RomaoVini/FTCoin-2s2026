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
import java.time.format.FormatStyle;
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
 * {@link RelatorioCarteiraDTO}. Os cálculos financeiros são delegados às
 * classes do Model; este controlador coordena as consultas e a apresentação.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 * @see RelatorioView
 * @see RelatorioCarteiraDTO
 */
public final class RelatorioController {

    /** Localidade usada na ordenação alfabética de nomes em português. */
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    /** Critério de ordenação das carteiras por identificador crescente. */
    private static final Comparator<Carteira> POR_ID =
            Comparator.comparingInt(Carteira::getId);

    /**
     * Critério de ordenação das carteiras pelo nome do titular, ignorando acentos
     * e maiúsculas; o identificador desempata nomes repetidos.
     */
    private static final Comparator<Carteira> POR_TITULAR =
            Comparator.comparing(Carteira::getTitular, criarCollator())
                      .thenComparingInt(Carteira::getId);

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

    /**
     * Obtém todas as carteiras do DAO e as ordena segundo o critério informado,
     * sem alterar a coleção original.
     *
     * @param criterio comparador que define a ordem das carteiras
     * @return nova lista ordenada (vazia se não houver carteiras)
     */
    private List<Carteira> ordenar(Comparator<Carteira> criterio) {
        List<Carteira> lista = new ArrayList<>(carteiraDAO.consultarTodos());
        lista.sort(criterio);
        return lista;
    }

    // ---------- Saldo atual da carteira ----------

    /**
     * Gera o relatório de saldo atual de uma carteira: quantidade de FTCoins,
     * valor a custo médio, custo médio unitário e valoração a mercado.
     * Uma carteira sem movimentações tem saldo zero e não consulta o oráculo.
     *
     * @param idCarteira identificador da carteira
     * @return saldo atual da carteira já formatado
     * @throws ValidacaoException se a carteira não existir
     * @throws OraculoException se o oráculo falhar ao fornecer uma cotação necessária
     */
    public RelatorioCarteiraDTO gerarSaldoAtual(int idCarteira) {
        Carteira carteira = buscarCarteira(idCarteira);

        List<Movimentacao> movimentacoes = movimentacaoDAO.consultarPorCarteira(idCarteira);
        if (movimentacoes.isEmpty()) {
            String zeroMoeda = moeda(BigDecimal.ZERO);
            return new RelatorioCarteiraDTO(
                    carteira.getId(),
                    carteira.getTitular(),
                    carteira.getCorretora(),
                    quantidade(BigDecimal.ZERO),
                    zeroMoeda,
                    zeroMoeda,
                    zeroMoeda);
        }

        CarteiraFinanceira financeiro = calculo.calcularCarteira(movimentacoes, LocalDate.now());
        return new RelatorioCarteiraDTO(
                carteira.getId(),
                carteira.getTitular(),
                carteira.getCorretora(),
                quantidade(financeiro.getSaldoQuantidade()),
                moeda(financeiro.getSaldoFinanceiro()),
                moeda(financeiro.getCustoMedio()),
                moeda(financeiro.getSaldoMercado()));
    }

    // ---------- Histórico de movimentações ----------

    /**
     * Gera o histórico de movimentações (compras e vendas) de uma carteira em
     * ordem cronológica, com cotação, custo médio e valoração a mercado de cada operação.
     *
     * @param idCarteira identificador da carteira
     * @return histórico formatado; a lista de movimentos é vazia se não houver operações
     * @throws ValidacaoException se a carteira não existir
     * @throws OraculoException se o oráculo falhar ao fornecer uma cotação histórica
     */
    public RelatorioCarteiraDTO.Historico gerarHistorico(int idCarteira) {
        Carteira carteira = buscarCarteira(idCarteira);

        CarteiraFinanceira financeiro = new CarteiraFinanceira();
        List<RelatorioCarteiraDTO.Movimento> linhas = new ArrayList<>();
        for (Movimentacao m : movimentacoesEmOrdem(idCarteira)) {
            Cotacao cotacao = oraculo.obterCotacao(m.getDataOperacao());
            financeiro.registrarMovimentacao(m, cotacao);
            financeiro.atualizarCotacaoAtual(cotacao);
            linhas.add(new RelatorioCarteiraDTO.Movimento(
                    data(m.getDataOperacao()),
                    chaveDoTipo(m),
                    quantidade(m.getQuantidade()),
                    moeda(cotacao.getValor()),
                    moeda(financeiro.getCustoMedio()),
                    moeda(financeiro.getSaldoMercado())));
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
     * @return um relatório por carteira
     * @throws ValidacaoException se não houver carteiras cadastradas
     * @throws OraculoException se não houver cotação atual nem cotação anterior conhecida
     *                          em cache, ou se falhar a consulta de uma cotação histórica
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
     * Apura os resultados das movimentações e o valor de mercado de uma carteira.
     *
     * @param carteira carteira a apurar
     * @param atual cotação usada para valorar o saldo atual
     * @param desatualizada indica se a cotação atual é uma cotação anterior em cache
     * @return relatório de ganho ou perda da carteira
     */
    private RelatorioCarteiraDTO.GanhoPerda apurar(Carteira carteira, Cotacao atual, boolean desatualizada) {
        CarteiraFinanceira financeiro = new CarteiraFinanceira();
        List<RelatorioCarteiraDTO.OperacaoApurada> operacoes = new ArrayList<>();

        for (Movimentacao m : movimentacoesEmOrdem(carteira.getId())) {
            Cotacao cotacao = oraculo.obterCotacao(m.getDataOperacao());
            financeiro.registrarMovimentacao(m, cotacao);
            operacoes.add(new RelatorioCarteiraDTO.OperacaoApurada(
                    data(m.getDataOperacao()),
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
                data(atual.getData()),
                desatualizada);
    }

    // ---------- Utilitários ----------

    /**
     * Busca uma carteira e falha se ela não estiver cadastrada.
     *
     * @param idCarteira identificador da carteira
     * @return carteira encontrada
     * @throws ValidacaoException se a carteira não existir
     */
    private Carteira buscarCarteira(int idCarteira) {
        return carteiraDAO.consultar(idCarteira)
                .orElseThrow(() -> new ValidacaoException("wallet.error.notFound", idCarteira));
    }

    /**
     * Recupera as movimentações de uma carteira em ordem cronológica e por ID crescente.
     *
     * @param idCarteira identificador da carteira
     * @return nova lista ordenada de movimentações
     */
    private List<Movimentacao> movimentacoesEmOrdem(int idCarteira) {
        List<Movimentacao> movimentacoes = new ArrayList<>(movimentacaoDAO.consultarPorCarteira(idCarteira));
        movimentacoes.sort(Comparator.comparing(Movimentacao::getDataOperacao)
                .thenComparingInt(Movimentacao::getIdMovimento));
        return movimentacoes;
    }

    /**
     * Retorna a chave de tradução correspondente ao tipo da operação.
     *
     * @param movimentacao movimentação a consultar
     * @return chave de tradução de compra ou venda
     */
    private static String chaveDoTipo(Movimentacao movimentacao) {
        return movimentacao.isCompra() ? "report.wallet.history.type.buy" : "report.wallet.history.type.sell";
    }

    /**
     * Converte uma quantidade para texto sem zeros finais desnecessários.
     *
     * @param valor quantidade numérica
     * @return representação decimal sem zeros finais desnecessários
     */
    private static String quantidade(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    /**
     * Retorna a localidade do idioma atualmente selecionado.
     *
     * @return localidade ativa
     */
    private static Locale localeAtivo() {
        return I18n.getIdiomaAtual().getLocale();
    }

    /**
     * Formata um valor como moeda conforme a localidade ativa.
     *
     * @param valor valor monetário
     * @return valor formatado como moeda
     */
    private static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(localeAtivo()).format(valor);
    }

    /**
     * Formata uma data no estilo médio da localidade ativa.
     *
     * @param valor data a formatar
     * @return data formatada
     */
    private static String data(LocalDate valor) {
        return valor.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(localeAtivo()));
    }

    /**
     * Cria um comparador textual em português que ignora acentos e diferenças de caixa.
     *
     * @return comparador configurado para nomes de titulares
     */
    private static Collator criarCollator() {
        Collator c = Collator.getInstance(PT_BR);
        c.setStrength(Collator.PRIMARY);
        return c;
    }
}