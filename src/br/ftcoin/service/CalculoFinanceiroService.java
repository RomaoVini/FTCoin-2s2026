
package br.ftcoin.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import br.ftcoin.model.CarteiraFinanceira;
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

   /**
    * Calcula em uma passada cronológica o resumo financeiro de uma carteira.
    *
    * @param movimentacoes histórico da carteira
    * @param dataCotacaoAtual data da cotação atual solicitada ao Oráculo
    * @return métricas de saldo, custo médio e resultados da carteira
    */
   public CarteiraFinanceira calcularCarteira(List<Movimentacao> movimentacoes, LocalDate dataCotacaoAtual) {
      Objects.requireNonNull(movimentacoes, "Histórico de movimentações obrigatório");
      Objects.requireNonNull(dataCotacaoAtual, "Data da cotação atual obrigatória");

      CarteiraFinanceira carteira = new CarteiraFinanceira();
      movimentacoes.stream()
            .sorted(Comparator.comparing(Movimentacao::getDataOperacao)
                  .thenComparingInt(Movimentacao::getIdMovimento))
            .forEach(movimentacao -> carteira.registrarMovimentacao(
                  movimentacao, oraculo.obterCotacao(movimentacao.getDataOperacao())));
      carteira.atualizarCotacaoAtual(oraculo.obterCotacao(dataCotacaoAtual));
      return carteira;
   }
}
