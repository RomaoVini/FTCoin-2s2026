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
 * Este programa � o m�dulo cliente de um sistema cliente/servidor para demonstra��o do uso de fluxos (streams) de objetos
 * enviados e recebidos pela rede, com uso de uma API propriet�ria para acesso ao servidor.
 *
 * Este m�dulo prepara uma requisi��o de cota��es de moeda, informando a feixa de datas das cota��es desejadas e a envia
 * para o servidor, que responde com uma lista (eventualmente vazia) de objetos do tipo ExchangeRate.
 *
 * Este cliente ser� usado temporariamente pelos alunos para testes de conex�o e API; posteriormente, suas funcionalidades
 * ser�o incorporadas, com devidas adapta��es, ao projeto FTCoin, ver. 2026.
 *
 * ************************************************************************************************************************
 */

package br.ftcoin.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

import br.ftcoin.dto.ExchangeRate;
import br.ftcoin.network.NetworkRequest;
import br.ftcoin.network.NetworkResponse;

public final class OracleSimpleClient
   {
   private final static String GROUP_NAME     = "POO2_2s26_A02";
   private final static String GROUP_PASSWORD = "W90OL8wsC8";
   private final String        host;
   private final int           port;

   public OracleSimpleClient(String host, int port)
      {
      this.host = host;
      this.port = port;
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

   private List<ExchangeRate> getValues(LocalDate since, LocalDate to) throws Exception
      {
      final NetworkRequest  request  = new NetworkRequest(GROUP_NAME, GROUP_PASSWORD, since, to);
      final NetworkResponse response = sendRequest(request);

      if (response.errorMessage != null)
         {
         throw new RuntimeException(response.errorMessage);
         }

      return (response.getExchangeRates());
      }

   private void go(LocalDate firstDate, LocalDate lastDate) throws Exception
      {
      printList(getValues(firstDate, lastDate));
      }

   private void printList(List<ExchangeRate> records)
      {
      if (!records.isEmpty())
         {
         for (final ExchangeRate record : records)
            {
            System.out.printf("%s, %.2f\n", record.getDate(), record.getValue());
            }
         }
      else
         {
         System.out.printf("No records found");
         }
      }

   private NetworkResponse sendRequest(NetworkRequest request) throws Exception
      {
      try (Socket socket = new Socket(host, port); ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream()))
         {
         outputStream.writeObject(request);
         outputStream.flush();
         return ((NetworkResponse) inputStream.readObject());
         }
      }
   }
