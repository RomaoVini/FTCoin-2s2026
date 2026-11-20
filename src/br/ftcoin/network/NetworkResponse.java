/*
 * ************************************************************************************************************************
 * Prof. Dr. Andr� F. de Angelis
 * Unicamp - Universidade Estadual de Campinas
 * FT - Faculdade de Tecnologia
 * ************************************************************************************************************************
 * Escrito em Java 20, usando IDE Eclipse 2023-06, em ambiente Windows 11
 *
 * Vers�o 1.0
 * 07.Outubro.2026
 *
 * ************************************************************************************************************************
 *
 * Esta classe implementa o protocolo de resposta do servidor de rede para o Projeto FT Coin ver. 2026.
 *
 * ************************************************************************************************************************
 */
package br.ftcoin.network;

import java.io.Serializable;
import java.util.List;

import br.ftcoin.dto.ExchangeRate;

/**
 * Resposta serializável do servidor, contendo cotações ou uma mensagem de erro.
 */
public final class NetworkResponse implements Serializable
   {
   private static final long serialVersionUID = 1L;

   /** Cotações retornadas pelo servidor, se a consulta for bem-sucedida. */
   public List<ExchangeRate> exchangeRates;
   /** Mensagem de erro retornada pelo servidor, se houver falha na consulta. */
   public String             errorMessage;

   /**
    * Cria uma resposta com as cotações e/ou a mensagem de erro recebidas.
    *
    * @param exchangeRates cotações retornadas, ou {@code null}
    * @param errorMessage mensagem de erro, ou {@code null} em caso de sucesso
    */
   public NetworkResponse(List<ExchangeRate> exchangeRates, String errorMessage)
      {
      super();
      this.exchangeRates = exchangeRates;
      this.errorMessage  = errorMessage;
      }

   /**
    * Retorna a mensagem de erro da resposta.
    *
    * @return mensagem de erro, ou {@code null} se não houver erro
    */
   public final String getErrorMessage()
      {
      return errorMessage;
      }

   /**
    * Retorna as cotações recebidas.
    *
    * @return cotações, ou {@code null} se não foram fornecidas
    */
   public final List<ExchangeRate> getExchangeRates()
      {
      return exchangeRates;
      }

   }
