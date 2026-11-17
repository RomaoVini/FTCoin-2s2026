package br.ftcoin.model;

import br.ftcoin.exception.ValidacaoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Acumulador das métricas financeiras de uma carteira.
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
     * Atualiza as métricas para uma movimentação e sua cotação histórica.
     *
     * @return o resultado operacional da movimentação (zero para compras)
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

    private static BigDecimal aplicarQuantidade(BigDecimal saldoAnterior, Movimentacao movimentacao) {
        BigDecimal saldoNovo = movimentacao.isCompra()
                ? saldoAnterior.add(movimentacao.getQuantidade())
                : saldoAnterior.subtract(movimentacao.getQuantidade());
        if (saldoNovo.signum() < 0) {
            throw new ValidacaoException("transaction.sell.insufficientBalance");
        }
        return saldoNovo;
    }

    public void atualizarCotacaoAtual(Cotacao cotacaoAtual) {
        this.cotacaoAtual = Objects.requireNonNull(cotacaoAtual, "Cotação atual obrigatória");
    }

    public BigDecimal getSaldoQuantidade() {
        return saldoQuantidade;
    }

    public BigDecimal getCustoMedio() {
        return custoMedio;
    }

    public BigDecimal getSaldoFinanceiro() {
        return saldoQuantidade.multiply(custoMedio);
    }

    public BigDecimal getSaldoMercado() {
        return saldoQuantidade.multiply(obterCotacaoAtual().getValor());
    }

    public BigDecimal getResultadoOperacional() {
        return resultadoOperacional;
    }

    public BigDecimal getResultadoAcumulado() {
        return resultadoAcumulado;
    }

    public BigDecimal getResultadoMercado() {
        return totalVendido.add(getSaldoMercado()).subtract(totalComprado);
    }

    private Cotacao obterCotacaoAtual() {
        if (cotacaoAtual == null) {
            throw new IllegalStateException("Cotação atual ainda não foi definida");
        }
        return cotacaoAtual;
    }
}
