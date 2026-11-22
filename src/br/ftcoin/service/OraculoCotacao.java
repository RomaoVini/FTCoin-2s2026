package br.ftcoin.service;

import br.ftcoin.model.Cotacao;

import java.time.LocalDate;

/**
 * Contrato de serviço para obtenção da cotação da moeda virtual <b>FT_Coin</b>
 * em uma data específica.
 *
 * <p>Permite que implementações concretas consultem uma API remota, utilizem cache
 * em memória ou forneçam cotações simuladas em testes unitários, mantendo os
 * controladores e serviços de cálculo desacoplados da fonte de dados.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 * @see Cotacao
 * @see OraculoException
 */
public interface OraculoCotacao {

    /**
     * Obtém a cotação da moeda virtual na data informada.
     *
     * @param data Data de referência desejada (não pode ser nula).
     * @return Instância válida de {@link Cotacao} para a data solicitada (nunca nula).
     * @throws NullPointerException se {@code data} for nula.
     * @throws OraculoException     se não houver cotação para a data, se a fonte externa
     *                              estiver indisponível ou retornar dados inválidos.
     */
    Cotacao obterCotacao(LocalDate data);
}