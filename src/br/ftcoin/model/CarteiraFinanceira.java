package br.ftcoin.model;

import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.util.I18n;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Acumulador de domínio responsável pelas métricas financeiras e contábeis
 * de uma carteira de moedas virtuais ao longo de sua linha do tempo.
 *
 * <p>Mantém o saldo de moedas, o custo médio ponderado de aquisição, os totais
 * comprados/vendidos, o resultado operacional realizado nas vendas e a valoração
 * a mercado com base na cotação atual.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class CarteiraFinanceira {

    private static final int ESCALA_CUSTO_MEDIO = 20;

    private BigDecimal saldoQuantidade = BigDecimal.ZERO;
    private BigDecimal custoMedio = BigDecimal.ZERO;
    private BigDecimal totalComprado = BigDecimal.ZERO;
    private BigDecimal totalVendido = BigDecimal.ZERO;
    private BigDecimal resultadoOperacional = BigDecimal.ZERO;
    private BigDecimal resultadoAcumulado = BigDecimal.ZERO;
    private Cotacao cotacaoAtual;

    /**
     * Atualiza as métricas financeiras da carteira para uma movimentação e sua cotação histórica.
     *
     * <p>Nas compras, recalcula o custo médio ponderado. Nas vendas, apura o lucro ou prejuízo
     * realizado com base no custo médio vigente e zera o custo médio caso toda a posição seja liquidada.</p>
     *
     * @param movimentacao Movimentação a registrar (compra ou venda).
     * @param cotacao      Cotação válida na data da movimentação.
     * @return O resultado operacional da movimentação ({@link BigDecimal#ZERO} para compras).
     * @throws NullPointerException se {@code movimentacao} ou {@code cotacao} forem nulos.
     * @throws ValidacaoException   se a operação for uma venda superior ao saldo disponível.
     */
    public BigDecimal registrarMovimentacao(Movimentacao movimentacao, Cotacao cotacao) {
        Objects.requireNonNull(movimentacao, () -> I18n.get("generic.error.nullParameter"));
        Objects.requireNonNull(cotacao, () -> I18n.get("generic.error.nullParameter"));

        BigDecimal saldoAnterior = saldoQuantidade;
        BigDecimal saldoNovo = aplicarQuantidade(saldoAnterior, movimentacao);
        BigDecimal valorMovimentado = movimentacao.getQuantidade().multiply(cotacao.getValor());
        resultadoOperacional = BigDecimal.ZERO;

        if (movimentacao.isCompra()) {
            BigDecimal saldoFinanceiroAnterior = saldoAnterior.multiply(custoMedio);
            custoMedio = saldoFinanceiroAnterior.add(valorMovimentado)
                    .divide(saldoNovo, ESCALA_CUSTO_MEDIO, RoundingMode.HALF_EVEN);
            totalComprado = totalComprado.add(valorMovimentado);
        } else {
            resultadoOperacional = valorMovimentado.subtract(movimentacao.getQuantidade().multiply(custoMedio));
            totalVendido = totalVendido.add(valorMovimentado);
            resultadoAcumulado = resultadoAcumulado.add(resultadoOperacional);
            if (saldoNovo.signum() == 0) {
                custoMedio = BigDecimal.ZERO;
            }
        }

        saldoQuantidade = saldoNovo;
        return resultadoOperacional;
    }

    /**
     * Calcula o saldo final de moedas de uma sequência de movimentações ordenadas cronologicamente.
     *
     * @param movimentacoes Histórico de movimentações da carteira.
     * @return Saldo de moedas após aplicar todas as operações em ordem cronológica.
     * @throws NullPointerException se {@code movimentacoes} ou algum de seus elementos for nulo.
     * @throws ValidacaoException   se uma venda exceder o saldo disponível em qualquer ponto da linha do tempo.
     */
    public static BigDecimal calcularSaldoQuantidade(List<Movimentacao> movimentacoes) {
        Objects.requireNonNull(movimentacoes, () -> I18n.get("generic.error.nullParameter"));
        BigDecimal saldo = BigDecimal.ZERO;
        List<Movimentacao> ordenadas = movimentacoes.stream()
                .map(m -> Objects.requireNonNull(m, () -> I18n.get("generic.error.nullParameter")))
                .sorted(Comparator.comparing(Movimentacao::getDataOperacao)
                        .thenComparingInt(Movimentacao::getIdMovimento))
                .toList();

        for (Movimentacao movimentacao : ordenadas) {
            saldo = aplicarQuantidade(saldo, movimentacao);
        }
        return saldo;
    }

    private static BigDecimal aplicarQuantidade(BigDecimal saldoAnterior, Movimentacao movimentacao) {
        BigDecimal saldoNovo = movimentacao.isCompra()
                ? saldoAnterior.add(movimentacao.getQuantidade())
                : saldoAnterior.subtract(movimentacao.getQuantidade());
        if (saldoNovo.signum() < 0) {
            throw new ValidacaoException("transaction.sell.insufficientBalance");
        }
        return saldoNovo;
    }

    /**
     * Define a cotação atual de referência utilizada para o cálculo de valoração a mercado.
     *
     * @param cotacaoAtual Cotação atual válida.
     * @throws NullPointerException se {@code cotacaoAtual} for nula.
     */
    public void atualizarCotacaoAtual(Cotacao cotacaoAtual) {
        this.cotacaoAtual = Objects.requireNonNull(cotacaoAtual, () -> I18n.get("generic.error.nullParameter"));
    }

    /**
     * Retorna a quantidade de moedas atualmente mantida na carteira.
     *
     * @return Saldo de moedas.
     */
    public BigDecimal getSaldoQuantidade() {
        return saldoQuantidade;
    }

    /**
     * Retorna o custo médio unitário de aquisição por moeda.
     *
     * @return Custo médio unitário ({@link BigDecimal#ZERO} se não houver posição aberta).
     */
    public BigDecimal getCustoMedio() {
        return custoMedio;
    }

    /**
     * Retorna o valor financeiro do saldo avaliado pelo custo médio de aquisição.
     *
     * @return Saldo em moeda fiduciária pelo custo médio.
     */
    public BigDecimal getSaldoFinanceiro() {
        return saldoQuantidade.multiply(custoMedio);
    }

    /**
     * Retorna o valor de mercado do saldo atual utilizando a cotação atual informada.
     *
     * @return Saldo em moeda fiduciária pela cotação atual.
     * @throws IllegalStateException se a cotação atual ainda não tiver sido definida.
     */
    public BigDecimal getSaldoMercado() {
        return saldoQuantidade.multiply(obterCotacaoAtual().getValor());
    }

    /**
     * Retorna o resultado operacional da última movimentação registrada.
     *
     * @return Resultado da última operação ({@link BigDecimal#ZERO} para compras).
     */
    public BigDecimal getResultadoOperacional() {
        return resultadoOperacional;
    }

    /**
     * Retorna a soma dos resultados operacionais realizados em todas as vendas registradas.
     *
     * @return Resultado acumulado das vendas.
     */
    public BigDecimal getResultadoAcumulado() {
        return resultadoAcumulado;
    }

    /**
     * Calcula o resultado financeiro total (realizado em vendas + não realizado na posição atual).
     *
     * @return Resultado total da carteira considerando a cotação atual.
     * @throws IllegalStateException se a cotação atual ainda não tiver sido definida.
     */
    public BigDecimal getResultadoMercado() {
        return totalVendido.add(getSaldoMercado()).subtract(totalComprado);
    }

    private Cotacao obterCotacaoAtual() {
        if (cotacaoAtual == null) {
            throw new IllegalStateException(I18n.get("generic.error.nullParameter"));
        }
        return cotacaoAtual;
    }
}