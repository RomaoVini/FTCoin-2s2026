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

public final class OracleSimpleClient
   {
   private final OraculoService oraculo;

   public OracleSimpleClient(String host, int port)
      {
      this.oraculo = new OraculoService(host, port);
      }

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

   private List<Cotacao> getValues(LocalDate since, LocalDate to)
      {
      return oraculo.obterCotacoes(since, to);
      }

   private void go(LocalDate firstDate, LocalDate lastDate)
      {
      printList(getValues(firstDate, lastDate));
      }

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
