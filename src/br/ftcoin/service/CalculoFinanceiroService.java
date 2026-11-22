package br.ftcoin.service;

import br.ftcoin.model.CarteiraFinanceira;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.util.I18n;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Serviço de domínio responsável pelos cálculos financeiros que dependem
 * da cotação histórica ou atual de cada {@link Movimentacao}.
 *
 * <p>Consolida a linha do tempo cronológica de operações de uma carteira
 * para apurar saldo de moedas, custo médio de aquisição, valor de mercado
 * e lucro/prejuízo realizado e não realizado.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class CalculoFinanceiroService {

    private final OraculoCotacao oraculo;

    /**
     * Cria o serviço utilizando a fonte de cotações informada.
     *
     * @param oraculo Fonte de cotações necessária aos cálculos financeiros.
     * @throws NullPointerException se {@code oraculo} for nulo.
     */
    public CalculoFinanceiroService(OraculoCotacao oraculo) {
        this.oraculo = Objects.requireNonNull(oraculo, () -> I18n.get("generic.error.nullParameter"));
    }

    /**
     * Calcula o valor monetário (em moeda fiduciária) da quantidade movimentada
     * com base na cotação da data da operação.
     *
     * @param movimentacao Operação a ser valorada.
     * @return Quantidade multiplicada pelo valor da cotação na data da operação.
     * @throws NullPointerException se {@code movimentacao} for nula.
     */
    public BigDecimal calcularValorFiat(Movimentacao movimentacao) {
        Objects.requireNonNull(movimentacao, () -> I18n.get("generic.error.nullParameter"));
        Cotacao cotacao = oraculo.obterCotacao(movimentacao.getDataOperacao());
        return movimentacao.getQuantidade().multiply(cotacao.getValor());
    }

    /**
     * Calcula, em uma passada cronológica ordenada por data e identificador,
     * o resumo financeiro consolidado de uma carteira.
     *
     * @param movimentacoes    Histórico de movimentações da carteira.
     * @param dataCotacaoAtual Data de referência da cotação atual solicitada ao Oráculo.
     * @return Objeto {@link CarteiraFinanceira} contendo métricas de saldo, custo médio e resultados.
     * @throws NullPointerException se {@code movimentacoes} ou {@code dataCotacaoAtual} forem nulos.
     */
    public CarteiraFinanceira calcularCarteira(List<Movimentacao> movimentacoes, LocalDate dataCotacaoAtual) {
        Objects.requireNonNull(movimentacoes, () -> I18n.get("generic.error.nullParameter"));
        Objects.requireNonNull(dataCotacaoAtual, () -> I18n.get("generic.error.nullParameter"));

        CarteiraFinanceira carteira = new CarteiraFinanceira();

        movimentacoes.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Movimentacao::getDataOperacao)
                        .thenComparingInt(Movimentacao::getIdMovimento))
                .forEach(movimentacao -> carteira.registrarMovimentacao(
                        movimentacao, oraculo.obterCotacao(movimentacao.getDataOperacao())));

        carteira.atualizarCotacaoAtual(oraculo.obterCotacao(dataCotacaoAtual));
        return carteira;
    }
}