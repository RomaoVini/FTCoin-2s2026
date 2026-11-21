package br.ftcoin.model;

import br.ftcoin.exception.ValidacaoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Acumulador das métricas financeiras de uma carteira.
 *
 * <p>Processa movimentações em ordem cronológica e mantém saldo de moedas,
 * custo médio, totais comprados e vendidos e resultados realizado e de mercado.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class CarteiraFinanceira {

    /** Número de casas decimais usado no cálculo do custo médio. */
    private static final int ESCALA_CUSTO_MEDIO = 20;

    /** Quantidade de moedas atualmente mantida na carteira. */
    private BigDecimal saldoQuantidade = BigDecimal.ZERO;

    /** Custo médio de aquisição por moeda. */
    private BigDecimal custoMedio = BigDecimal.ZERO;

    /** Valor financeiro total acumulado em compras. */
    private BigDecimal totalComprado = BigDecimal.ZERO;

    /** Valor financeiro total acumulado em vendas. */
    private BigDecimal totalVendido = BigDecimal.ZERO;

    /** Resultado operacional da última movimentação registrada. */
    private BigDecimal resultadoOperacional = BigDecimal.ZERO;

    /** Soma dos resultados operacionais de todas as vendas registradas. */
    private BigDecimal resultadoAcumulado = BigDecimal.ZERO;

    /** Cotação atual usada para valorar o saldo a preço de mercado (pode ser nula até ser definida). */
    private Cotacao cotacaoAtual;

    /**
     * Atualiza as métricas para uma movimentação e sua cotação histórica.
     *
     * @param movimentacao movimentação a registrar
     * @param cotacao cotação válida na data da movimentação
     * @return o resultado operacional da movimentação (zero para compras)
     * @throws NullPointerException se a movimentação ou a cotação forem nulas
     * @throws ValidacaoException se uma venda exceder o saldo disponível
     */
    public BigDecimal registrarMovimentacao(Movimentacao movimentacao, Cotacao cotacao) {
        Objects.requireNonNull(movimentacao, "Movimentação obrigatória");
        Objects.requireNonNull(cotacao, "Cotação da movimentação obrigatória");

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
        }

        saldoQuantidade = saldoNovo;
        return resultadoOperacional;
    }

    /**
     * Calcula o saldo de uma sequência de movimentações em ordem cronológica.
     *
     * @param movimentacoes histórico de movimentações
     * @return saldo de moedas após aplicar todas as operações
     * @throws NullPointerException se o histórico ou algum de seus elementos for nulo
     * @throws ValidacaoException se uma venda exceder o saldo disponível
     */
    public static BigDecimal calcularSaldoQuantidade(List<Movimentacao> movimentacoes) {
        Objects.requireNonNull(movimentacoes, "Histórico de movimentações obrigatório");
        BigDecimal saldo = BigDecimal.ZERO;
        for (Movimentacao movimentacao : movimentacoes.stream()
                .sorted(Comparator.comparing(Movimentacao::getDataOperacao)
                        .thenComparingInt(Movimentacao::getIdMovimento))
                .toList()) {
            Objects.requireNonNull(movimentacao, "Movimentação obrigatória");
            saldo = aplicarQuantidade(saldo, movimentacao);
        }
        return saldo;
    }

    /**
     * Aplica a quantidade de uma movimentação ao saldo informado.
     *
     * @param saldoAnterior saldo de moedas antes da movimentação
     * @param movimentacao movimentação a aplicar (soma se compra, subtrai se venda)
     * @return novo saldo de moedas
     * @throws ValidacaoException se o novo saldo ficar negativo
     */
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
     * Define a cotação atual usada no cálculo do valor de mercado da carteira.
     *
     * @param cotacaoAtual cotação mais recente da moeda
     * @throws NullPointerException se a cotação for nula
     */
    public void atualizarCotacaoAtual(Cotacao cotacaoAtual) {
        this.cotacaoAtual = Objects.requireNonNull(cotacaoAtual, "Cotação atual obrigatória");
    }

    /**
     * Retorna a quantidade de moedas atualmente mantida na carteira.
     *
     * @return saldo de moedas
     */
    public BigDecimal getSaldoQuantidade() {
        return saldoQuantidade;
    }

    /**
     * Retorna o custo médio de aquisição por moeda.
     *
     * @return custo médio unitário
     */
    public BigDecimal getCustoMedio() {
        return custoMedio;
    }

    /**
     * Retorna o valor financeiro do saldo pelo custo médio de aquisição.
     *
     * @return saldo em moeda fiduciária pelo custo médio
     */
    public BigDecimal getSaldoFinanceiro() {
        return saldoQuantidade.multiply(custoMedio);
    }

    /**
     * Retorna o valor de mercado do saldo usando a cotação atual.
     *
     * @return saldo em moeda fiduciária pela cotação atual
     * @throws IllegalStateException se a cotação atual ainda não tiver sido definida
     */
    public BigDecimal getSaldoMercado() {
        return saldoQuantidade.multiply(obterCotacaoAtual().getValor());
    }

    /**
     * Retorna o resultado operacional da última movimentação registrada.
     *
     * @return resultado da última operação (zero para compras)
     */
    public BigDecimal getResultadoOperacional() {
        return resultadoOperacional;
    }

    /**
     * Retorna a soma dos resultados operacionais das vendas registradas.
     *
     * @return resultado acumulado das operações
     */
    public BigDecimal getResultadoAcumulado() {
        return resultadoAcumulado;
    }

    /**
     * Calcula o resultado total considerando as vendas e o valor de mercado atual.
     *
     * @return resultado realizado e não realizado da carteira
     * @throws IllegalStateException se a cotação atual ainda não tiver sido definida
     */
    public BigDecimal getResultadoMercado() {
        return totalVendido.add(getSaldoMercado()).subtract(totalComprado);
    }

    /**
     * Retorna a cotação atual, garantindo que ela já tenha sido definida.
     *
     * @return cotação atual
     * @throws IllegalStateException se a cotação atual ainda não tiver sido definida
     */
    private Cotacao obterCotacaoAtual() {
        if (cotacaoAtual == null) {
            throw new IllegalStateException("Cotação atual ainda não foi definida");
        }
        return cotacaoAtual;
    }
}