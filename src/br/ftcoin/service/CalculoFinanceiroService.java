
package br.ftcoin.service;

import java.math.BigDecimal;
import java.util.Objects;

import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;

/**
 * Cálculos financeiros que dependem da cotação de uma movimentação.
 */
public final class CalculoFinanceiroService {
   private final OraculoCotacao oraculo;

   public CalculoFinanceiroService(OraculoCotacao oraculo) {
      this.oraculo = Objects.requireNonNull(oraculo, "Oráculo de cotações obrigatório");
   }

   /**
    * Calcula o valor monetário da quantidade movimentada na data da operação.
    *
    * @param movimentacao operação a valorar
    * @return quantidade multiplicada pela cotação da data
    */
   public BigDecimal calcularValorFiat(Movimentacao movimentacao) {
      Objects.requireNonNull(movimentacao, "Movimentação obrigatória");
      Cotacao cotacao = oraculo.obterCotacao(movimentacao.getDataOperacao());
      return movimentacao.getQuantidade().multiply(cotacao.getValor());
   }
}
