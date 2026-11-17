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
 * Esta classe implementa o protocolo de requisi��o de rede para o Projeto FT Coin ver. 2026.
 *
 * ************************************************************************************************************************
 */

package br.ftcoin.network;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Requisição serializável enviada ao servidor para obter cotações em um intervalo.
 */
public final class NetworkRequest implements Serializable
   {
   private static final long serialVersionUID = 1L;

   private final String      groupName;
   private final String      groupPassword;
   private final LocalDate   firstDate;
   private final LocalDate   lastDate;

   /**
    * Cria uma requisição de cotações para o grupo e o intervalo informados.
    *
    * @param groupName nome do grupo cliente
    * @param groupPassword credencial do grupo cliente
    * @param firstDate primeiro dia do intervalo
    * @param lastDate último dia do intervalo
    */
   public NetworkRequest(String groupName, String groupPassword, LocalDate firstDate, LocalDate lastDate)
      {
      super();
      this.groupName     = groupName;
      this.groupPassword = groupPassword;
      this.firstDate     = firstDate;
      this.lastDate      = lastDate;
      }

   /**
    * Retorna o início do intervalo solicitado.
    *
    * @return primeira data do intervalo
    */
   public final LocalDate getFirstDate()
      {
      return firstDate;
      }

   /**
    * Retorna o nome do grupo cliente.
    *
    * @return nome do grupo
    */
   public final String getGroupName()
      {
      return groupName;
      }

   /**
    * Retorna a credencial do grupo cliente.
    *
    * @return senha do grupo
    */
   public final String getGroupPassword()
      {
      return groupPassword;
      }

   /**
    * Retorna o fim do intervalo solicitado.
    *
    * @return última data do intervalo
    */
   public final LocalDate getLastDate()
      {
      return lastDate;
      }

   }
