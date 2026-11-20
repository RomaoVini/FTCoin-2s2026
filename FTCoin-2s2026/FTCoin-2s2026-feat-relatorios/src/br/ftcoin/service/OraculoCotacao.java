package br.ftcoin.service;

import java.time.LocalDate;

import br.ftcoin.model.Cotacao;

/**
 * Contrato para obter a cotação da FTCoin em uma data específica.
 *
 * Implementações podem consultar um serviço remoto ou fornecer cotações
 * simuladas, sem que os consumidores dependam dos detalhes da fonte.
 */
public interface OraculoCotacao {
   /**
    * Obtém a cotação da moeda virtual na data informada.
    *
    * @param data data desejada, não nula
    * @return cotação válida para a data, nunca nula
    * @throws OraculoException se não houver cotação ou se a fonte estiver
    *                          indisponível ou retornar dados inválidos
    */
   Cotacao obterCotacao(LocalDate data);
}
