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

public class RelatorioController {

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

    // Fallback: última cotação atual obtida com sucesso nesta execução
    private Cotacao ultimaCotacaoConhecida;

    public RelatorioController(MenuView menuView, CarteiraDAO carteiraDAO,
                               MovimentacaoDAO movimentacaoDAO, OraculoCotacao oraculo) {
        Objects.requireNonNull(menuView, "MenuView não pode ser nula.");
        this.carteiraDAO = Objects.requireNonNull(carteiraDAO, "CarteiraDAO não pode ser nulo.");
        this.movimentacaoDAO = Objects.requireNonNull(movimentacaoDAO, "MovimentacaoDAO não pode ser nulo.");
        this.oraculo = Objects.requireNonNull(oraculo, "Oráculo não pode ser nulo.");
        this.calculo = new CalculoFinanceiroService(this.oraculo);
        this.view = new RelatorioView(menuView.getConsoleUtils(), this);
    }

    public void exibirMenuRelatorios() {
        view.exibirMenu();
    }

    // ---------- Task 1: carteiras ordenadas ----------

    public List<Carteira> listarCarteirasPorId() {
        return ordenar(POR_ID);
    }

    public List<Carteira> listarCarteirasPorTitular() {
        return ordenar(POR_TITULAR);
    }

    private List<Carteira> ordenar(Comparator<Carteira> criterio) {
        List<Carteira> lista = new ArrayList<>(carteiraDAO.consultarTodos());
        lista.sort(criterio);
        return lista;
    }

    // ---------- Task 2: saldo atual da carteira ----------

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

    // ---------- Task 3: histórico de movimentações ----------

    public RelatorioCarteiraDTO.Historico gerarHistorico(int idCarteira) {
        Carteira carteira = buscarCarteira(idCarteira);

        List<Movimentacao> movimentacoes = new ArrayList<>(movimentacaoDAO.consultarPorCarteira(idCarteira));
        movimentacoes.sort(Comparator.comparing(Movimentacao::getDataOperacao)
                .thenComparingInt(Movimentacao::getIdMovimento));

        List<RelatorioCarteiraDTO.Movimento> linhas = new ArrayList<>();
        for (Movimentacao m : movimentacoes) {
            Cotacao cotacao = oraculo.obterCotacao(m.getDataOperacao());
            linhas.add(new RelatorioCarteiraDTO.Movimento(
                    m.getDataOperacao().format(FORMATO_DATA),
                    m.isCompra() ? "report.wallet.history.type.buy" : "report.wallet.history.type.sell",
                    quantidade(m.getQuantidade()),
                    moeda(cotacao.getValor())));
        }
        return new RelatorioCarteiraDTO.Historico(
                carteira.getId(), carteira.getTitular(), carteira.getCorretora(), linhas);
    }

    // ---------- Task 4: ganho/perda e valoração a mercado (todas as carteiras) ----------

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
        List<Movimentacao> movimentacoes = new ArrayList<>(movimentacaoDAO.consultarPorCarteira(carteira.getId()));
        movimentacoes.sort(Comparator.comparing(Movimentacao::getDataOperacao)
                .thenComparingInt(Movimentacao::getIdMovimento));

        CarteiraFinanceira financeiro = new CarteiraFinanceira();
        List<RelatorioCarteiraDTO.OperacaoApurada> operacoes = new ArrayList<>();
        for (Movimentacao m : movimentacoes) {
            Cotacao cotacao = oraculo.obterCotacao(m.getDataOperacao());
            financeiro.registrarMovimentacao(m, cotacao);
            operacoes.add(new RelatorioCarteiraDTO.OperacaoApurada(
                    m.getDataOperacao().format(FORMATO_DATA),
                    m.isCompra() ? "report.wallet.history.type.buy" : "report.wallet.history.type.sell",
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

    // ---------- utilitários ----------

    private Carteira buscarCarteira(int idCarteira) {
        return carteiraDAO.consultar(idCarteira)
                .orElseThrow(() -> new ValidacaoException("wallet.error.notFound", idCarteira));
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