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

public final class ExchangeRate implements Serializable
   {
   private static final long serialVersionUID = 1L;

   LocalDate                 date;
   BigDecimal                value;

   public ExchangeRate(LocalDate date, BigDecimal value)
      {
      super();
      this.date  = date;
      this.value = value;
      }

   public final LocalDate getDate()
      {
      return date;
      }

   public final BigDecimal getValue()
      {
      return value;
      }

   }
