package br.ftcoin.dto;

import java.util.List;


public record RelatorioCarteiraDTO(int id, String titular, String corretora,
                                   String saldoQuantidade, String saldoFinanceiro) {

    public record Movimento(String data, String tipoChave, String quantidade, String cotacao) { }

    public record Historico(int id, String titular, String corretora, List<Movimento> movimentos) { }

    public record OperacaoApurada(String data, String tipoChave, String quantidade, String preco,
                                  String saldo, String resultadoOperacao,
                                  String resultadoAcumulado, String custoMedio) { }

    public record GanhoPerda(int id, String titular, String corretora,
                             List<OperacaoApurada> operacoes,
                             String quantidade, String custoMedio, String resultadoAcumulado,
                             String saldoMercado, String resultadoPotencial,
                             String cotacaoAtual, String dataCotacao,
                             boolean cotacaoDesatualizada) { }
}