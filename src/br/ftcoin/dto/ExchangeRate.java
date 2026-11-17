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
 * Este classe � o DTO de um sistema cliente/servidor para demonstra��o do uso de fluxos (streams) de objetos
 * enviados e recebidos pela rede, com uso de uma API propriet�ria para acesso ao servidor.
 *
 * Esta classe carrega dados da cota��o de uma moeda (FTCoin) entre o servidor e o cliente (Data Transfer Object - DTO)
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
   private static final long serialVersionUID = 1L;

   LocalDate                 date;
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
