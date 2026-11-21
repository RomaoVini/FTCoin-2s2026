/*
 * ************************************************************************************************************************
 * Prof. Dr. André F. de Angelis
 * Unicamp - Universidade Estadual de Campinas
 * FT - Faculdade de Tecnologia
 * ************************************************************************************************************************
 * Escrito em Java 20, usando IDE Eclipse 2023-06, em ambiente Windows 11
 *
 * Versão 1.0
 * 07.Outubro.2026
 *
 * ************************************************************************************************************************
 *
 * Esta classe é o DTO de um sistema cliente/servidor para demonstração do uso de fluxos (streams) de objetos
 * enviados e recebidos pela rede, com uso de uma API proprietária para acesso ao servidor.
 *
 * Esta classe carrega dados da cotação de uma moeda (FTCoin) entre o servidor e o cliente (Data Transfer Object - DTO)
 *
 * Projeto FTCoin, ver. 2026.
 *
 * ************************************************************************************************************************
 */

package br.ftcoin.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO serializável que representa a cotação recebida do servidor de cotações.
 */
public final class ExchangeRate implements Serializable
   {
   /** Versão de serialização desta classe; deve ser a mesma no cliente e no servidor. */
   private static final long serialVersionUID = 1L;

   /** Data da cotação. */
   LocalDate                 date;

   /** Valor da moeda (FTCoin) na data da cotação. */
   BigDecimal                value;

   /**
    * Cria uma cotação no formato usado pelo protocolo de rede.
    *
    * @param date data da cotação
    * @param value valor da moeda nessa data
    */
   public ExchangeRate(LocalDate date, BigDecimal value)
      {
      super();
      this.date  = date;
      this.value = value;
      }

   /**
    * Retorna a data da cotação.
    *
    * @return data da cotação
    */
   public final LocalDate getDate()
      {
      return date;
      }

   /**
    * Retorna o valor da moeda na data da cotação.
    *
    * @return valor da cotação
    */
   public final BigDecimal getValue()
      {
      return value;
      }

   }