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
 * Este programa demonstra o acesso ao servidor proprietário por meio de OraculoService.
 *
 * O protocolo de rede é encapsulado pelo serviço; este cliente apenas exibe as cotações recebidas.
 *
 * Projeto FTCoin, ver. 2026.
 *
 * ************************************************************************************************************************
 */

package br.ftcoin.client;

import java.time.LocalDate;
import java.util.List;

import br.ftcoin.model.Cotacao;
import br.ftcoin.service.OraculoService;

/**
 * Cliente de linha de comando que demonstra a consulta de um histórico de cotações.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class OracleSimpleClient
   {
   /** Serviço que encapsula a comunicação de rede com o servidor de cotações. */
   private final OraculoService oraculo;

   /**
    * Cria o cliente configurado para consultar o servidor informado.
    *
    * @param host endereço do servidor de cotações
    * @param port porta do servidor de cotações
    * @throws IllegalArgumentException se o endereço estiver vazio ou a porta estiver fora do
    *                                  intervalo válido
    */
   public OracleSimpleClient(String host, int port)
      {
      this.oraculo = new OraculoService(host, port);
      }

   /**
    * Executa o cliente para consultar e exibir as cotações desde a data atual menos sete dias
    * até a data atual, incluindo ambas as datas.
    *
    * @param args argumentos opcionais; devem conter o endereço do servidor e a porta, nessa
    *             ordem. Quando não forem informados dois argumentos, utiliza os valores padrão
    */
   public static void main(String[] args)
      {
      // data range to be requested
      final LocalDate lastDate   = LocalDate.now();
      final LocalDate firstDate  = lastDate.minusDays(7);

      // default values; use line arguments to modify
      String          serverIP   = "143.106.243.64";
      int             serverPort = 1234;

      try
         {
         if (args.length != 2)
            {
            System.out.println("Usage: java OracleSimpleClient <host> <port>");
            System.out.println("Using defaults: " + serverIP + ", " + serverPort);
            }
         else
            {
            serverIP   = args[0];
            serverPort = Integer.parseInt(args[1]);
            }

         final OracleSimpleClient client = new OracleSimpleClient(serverIP, serverPort);
         client.go(firstDate, lastDate);

         }
      catch (final Exception exception)
         {
         System.err.println("Client encountered error: " + exception.getMessage());
         System.exit(1);
         }
      }

   /**
    * Obtém do servidor as cotações de um intervalo de datas.
    *
    * @param since data inicial do intervalo (inclusiva)
    * @param to    data final do intervalo (inclusiva)
    * @return lista de cotações recebidas para o período; pode ser vazia
    * @throws br.ftcoin.service.OraculoException se o intervalo for inválido ou a consulta falhar
    */
   private List<Cotacao> getValues(LocalDate since, LocalDate to)
      {
      return oraculo.obterCotacoes(since, to);
      }

   /**
    * Consulta as cotações do intervalo informado e as exibe no console.
    *
    * @param firstDate data inicial da consulta (inclusiva)
    * @param lastDate  data final da consulta (inclusiva)
    * @throws br.ftcoin.service.OraculoException se o intervalo for inválido ou a consulta falhar
    */
   private void go(LocalDate firstDate, LocalDate lastDate)
      {
      printList(getValues(firstDate, lastDate));
      }

   /**
    * Imprime cada cotação no formato {@code data, valor}; se a lista estiver vazia,
    * informa que nenhum registro foi encontrado.
    *
    * @param records cotações a serem exibidas
    */
   private void printList(List<Cotacao> records)
      {
      if (!records.isEmpty())
         {
         for (final Cotacao record : records)
            {
            System.out.printf("%s, %.2f\n", record.getData(), record.getValor());
            }
         }
      else
         {
         System.out.printf("No records found");
         }
      }

   }