package br.ftcoin.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import br.ftcoin.dto.ExchangeRate;
import br.ftcoin.model.Cotacao;
import br.ftcoin.network.NetworkRequest;
import br.ftcoin.network.NetworkResponse;

/**
 * Integração com o servidor proprietário de cotações FTCoin.
 * A comunicação, protocolo e conversão de respostas ficam encapsulados nesta classe.
 */
public final class OraculoService implements OraculoCotacao {
   private static final String GROUP_NAME = "POO2_2s26_A02";
   private static final String GROUP_PASSWORD = "W90OL8wsC8";
   private static final String DEFAULT_HOST = "143.106.243.64";
   private static final int DEFAULT_PORT = 1234;
   private static final int CONNECTION_TIMEOUT_MILLIS = 5000;
   private static final int READ_TIMEOUT_MILLIS = 5000;

   private final String host;
   private final int port;
   private final String groupName;
   private final String groupPassword;

   public OraculoService() {
      this(DEFAULT_HOST, DEFAULT_PORT, GROUP_NAME, GROUP_PASSWORD);
   }

   public OraculoService(String host, int port) {
      this(host, port, GROUP_NAME, GROUP_PASSWORD);
   }

   public OraculoService(String host, int port, String groupName, String groupPassword) {
      if (host == null || host.isBlank()) {
         throw new IllegalArgumentException("Host do oráculo obrigatório");
      }
      if (port < 1 || port > 65535) {
         throw new IllegalArgumentException("Porta do oráculo inválida");
      }
      if (groupName == null || groupName.isBlank() || groupPassword == null || groupPassword.isBlank()) {
         throw new IllegalArgumentException("Credenciais do oráculo obrigatórias");
      }

      this.host = host;
      this.port = port;
      this.groupName = groupName;
      this.groupPassword = groupPassword;
   }

   /**
    * Obtém do servidor a cotação correspondente à data solicitada.
    *
    * @param data data da cotação
    * @return cotação válida para a data
    * @throws OraculoException se a data não tiver cotação ou se a comunicação/
    *                           resposta do servidor for inválida
    */
   @Override
   public Cotacao obterCotacao(LocalDate data) {
      if (data == null) {
         throw new OraculoException("A data da cotação é obrigatória.");
      }

      List<ExchangeRate> rates = solicitarCotacoes(data, data);
      for (ExchangeRate rate : rates) {
         if (rate != null && data.equals(rate.getDate())) {
            try {
               return new Cotacao(rate.getDate(), rate.getValue());
            } catch (IllegalArgumentException exception) {
               throw new OraculoException("O servidor retornou uma cotação inválida para " + data + ".", exception);
            }
         }
      }
      throw new OraculoException("Não há cotação disponível para " + data + ".");
   }

   /**
    * Consulta um intervalo para clientes de diagnóstico que exibem históricos.
    */
   public List<Cotacao> obterCotacoes(LocalDate desde, LocalDate ate) {
      if (desde == null || ate == null || desde.isAfter(ate)) {
         throw new OraculoException("Intervalo de datas inválido.");
      }

      List<ExchangeRate> rates = solicitarCotacoes(desde, ate);
      try {
         return rates.stream()
               .filter(Objects::nonNull)
               .map(rate -> new Cotacao(rate.getDate(), rate.getValue()))
               .toList();
      } catch (RuntimeException exception) {
         throw new OraculoException("O servidor retornou cotações inválidas.", exception);
      }
   }

   private List<ExchangeRate> solicitarCotacoes(LocalDate desde, LocalDate ate) {
      NetworkResponse response;
      try (Socket socket = new Socket()) {
         socket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT_MILLIS);
         socket.setSoTimeout(READ_TIMEOUT_MILLIS);
         try (ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
               ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
            output.writeObject(new NetworkRequest(groupName, groupPassword, desde, ate));
            output.flush();
            Object received = input.readObject();
            if (!(received instanceof NetworkResponse networkResponse)) {
               throw new OraculoException("O servidor retornou uma resposta inesperada.");
            }
            response = networkResponse;
         }
      } catch (IOException | ClassNotFoundException exception) {
         throw new OraculoException("Não foi possível consultar o oráculo de cotações.", exception);
      }

      if (response.getErrorMessage() != null && !response.getErrorMessage().isBlank()) {
         throw new OraculoException("O oráculo recusou a consulta: " + response.getErrorMessage());
      }
      if (response.getExchangeRates() == null) {
         throw new OraculoException("O servidor retornou uma lista de cotações inválida.");
      }
      return response.getExchangeRates();
   }
}
