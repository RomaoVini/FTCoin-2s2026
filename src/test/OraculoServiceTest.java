package br.ftcoin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import br.ftcoin.dto.ExchangeRate;
import br.ftcoin.model.Cotacao;
import br.ftcoin.network.NetworkResponse;

class OraculoServiceTest {
   @Test
   void retornaCotacaoDaDataSolicitada() throws Exception {
      LocalDate data = LocalDate.of(2026, 11, 9);
      NetworkResponse resposta = new NetworkResponse(
            List.of(new ExchangeRate(data, new BigDecimal("123.45"))), null);

      Cotacao cotacao = consultar(resposta, data);

      assertEquals(data, cotacao.getData());
      assertEquals(new BigDecimal("123.45"), cotacao.getValor());
   }

   @Test
   void converteErroDoServidorEmExcecaoDeDominio() throws Exception {
      LocalDate data = LocalDate.of(2026, 11, 9);
      NetworkResponse resposta = new NetworkResponse(null, "consulta recusada");

      OraculoException exception = assertThrows(OraculoException.class, () -> consultar(resposta, data));
      assertTrue(exception.getMessage().contains("recusou"));
   }

   @Test
   void converteRespostaInvalidaEmExcecaoDeDominio() throws Exception {
      LocalDate data = LocalDate.of(2026, 11, 9);
      NetworkResponse resposta = new NetworkResponse(
            List.of(new ExchangeRate(data, BigDecimal.ZERO)), null);

      OraculoException exception = assertThrows(OraculoException.class, () -> consultar(resposta, data));
      assertTrue(exception.getMessage().contains("cotação inválida"));
   }

   private Cotacao consultar(NetworkResponse resposta, LocalDate data) throws Exception {
      try (ServerSocket servidor = new ServerSocket(0)) {
         CompletableFuture<Void> respostaEnviada = CompletableFuture.runAsync(() -> {
            try (Socket socket = servidor.accept();
                  ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
                  ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())) {
               entrada.readObject();
               saida.writeObject(resposta);
               saida.flush();
            } catch (Exception exception) {
               throw new IllegalStateException(exception);
            }
         });

         Cotacao cotacao = new OraculoService("127.0.0.1", servidor.getLocalPort(), "grupo", "senha")
               .obterCotacao(data);
         respostaEnviada.get(2, TimeUnit.SECONDS);
         return cotacao;
      }
   }
}
