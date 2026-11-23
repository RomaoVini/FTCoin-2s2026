package br.ftcoin.dto;

import java.util.List;

/**
 * Objeto de transferência de dados (DTO) dos relatórios de carteira.
 *
 * <p>Os valores já chegam à View formatados como texto pelo
 * {@link br.ftcoin.controller.RelatorioController}, de modo que a View apenas
 * os exibe, sem realizar cálculos ou aplicar regras de negócio.</p>
 *
 * <p>O próprio registro representa o relatório de saldo atual; os registros
 * aninhados representam os demais relatórios.</p>
 *
 * @param id               identificador da carteira
 * @param titular          nome do titular da carteira
 * @param corretora        corretora da carteira
 * @param saldoQuantidade  quantidade atual de FTCoins (Sd. qtd)
 * @param saldoFinanceiro  valor total da posição pelo custo médio (quantidade × custo médio)
 * @param custoMedio       custo médio unitário de aquisição
 * @param saldoMercado     valor total da posição pela cotação atual
 */
public record RelatorioCarteiraDTO(int id, String titular, String corretora,
                                   String saldoQuantidade, String saldoFinanceiro,
                                   String custoMedio, String saldoMercado) {

    /**
     * Linha do histórico de movimentações de uma carteira.
     *
     * @param data         data da operação
     * @param tipoChave    chave de internacionalização do tipo (compra ou venda)
     * @param quantidade   quantidade movimentada
     * @param cotacao      cotação da FTCoin na data da operação
     * @param custoMedio   custo médio unitário após a operação
     * @param saldoMercado valor de mercado do saldo após a operação, usando a cotação da data
     */
    public record Movimento(String data, String tipoChave, String quantidade, String cotacao,
                            String custoMedio, String saldoMercado) { }

    /**
     * Relatório de histórico de movimentações de uma carteira.
     *
     * @param id         identificador da carteira
     * @param titular    nome do titular
     * @param corretora  corretora da carteira
     * @param movimentos movimentações em ordem cronológica (vazia se não houver nenhuma)
     */
    public record Historico(int id, String titular, String corretora, List<Movimento> movimentos) { }

    /**
     * Linha da apuração de ganho ou perda, uma para cada operação da carteira.
     *
     * @param data               data formatada da operação
     * @param tipoChave          chave de internacionalização do tipo da operação
     * @param quantidade         quantidade movimentada, já formatada
     * @param preco              cotação da FTCoin na data da operação, já formatada
     * @param saldo              saldo de FTCoins após a operação, já formatado
     * @param resultadoOperacao  resultado da operação, zero para compras
     * @param resultadoAcumulado resultado acumulado das vendas até a operação
     * @param custoMedio         custo médio unitário após a operação
     */
    public record OperacaoApurada(String data, String tipoChave, String quantidade, String preco,
                                  String saldo, String resultadoOperacao,
                                  String resultadoAcumulado, String custoMedio) { }

    /**
     * Relatório de ganho ou perda e valoração a mercado de uma carteira.
     *
     * @param id                   identificador da carteira
     * @param titular              nome do titular
     * @param corretora            corretora da carteira
     * @param operacoes            apuração operação a operação (vazia se não houver movimentações)
     * @param quantidade           quantidade atual de FTCoins
     * @param custoMedio           custo médio atual
     * @param resultadoAcumulado   resultado já realizado pelas vendas (Res. Acum.)
     * @param saldoMercado         cotação atual multiplicada pela quantidade atual (Sd. Merc.)
     * @param resultadoPotencial   resultado total caso a posição atual fosse vendida pela cotação atual
     * @param cotacaoAtual         cotação atual utilizada na valoração a mercado
     * @param dataCotacao          data da cotação utilizada
     * @param cotacaoDesatualizada {@code true} se foi usada a última cotação conhecida
     *                             porque o oráculo falhou ao fornecer a cotação atual
     */
    public record GanhoPerda(int id, String titular, String corretora,
                             List<OperacaoApurada> operacoes,
                             String quantidade, String custoMedio, String resultadoAcumulado,
                             String saldoMercado, String resultadoPotencial,
                             String cotacaoAtual, String dataCotacao,
                             boolean cotacaoDesatualizada) { }
}