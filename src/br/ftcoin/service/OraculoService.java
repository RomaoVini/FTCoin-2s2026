package br.ftcoin.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.ftcoin.model.Cotacao;
import br.ftcoin.util.I18n;
import network.ExchangeRate;
import network.NetworkRequest;
import network.NetworkResponse;

/**
 * Integração com o servidor proprietário de cotações FTCoin.
 * A comunicação, protocolo e conversão de respostas ficam encapsulados nesta classe.
 *
 * <p>As cotações já consultadas ficam em cache em memória, evitando requisições
 * repetidas ao servidor para a mesma data.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class OraculoService implements OraculoCotacao {

   private static final Logger LOG = Logger.getLogger(OraculoService.class.getName());

   /** Nome do grupo cliente usado na autenticação junto ao servidor. */
   private static final String GROUP_NAME = "POO2_2s26_A02";

   /** Pares chave/valor lidos do arquivo local {@code oraculo.env}. */
   private static final Map<String, String> ENV_LOCAL = carregarArquivoEnv(Path.of("oraculo.env"));

   /** Credencial do grupo cliente usada na autenticação junto ao servidor. */
   private static final String GROUP_PASSWORD = obterConfiguracao("ORACULO_PASSWORD", null);

   /** Endereço padrão do servidor de cotações. */
   private static final String DEFAULT_HOST = "143.106.243.64";

   /** Porta padrão do servidor de cotações. */
   private static final int DEFAULT_PORT = 1234;

   /** Tempo máximo, em milissegundos, para estabelecer a conexão. */
   private static final int CONNECTION_TIMEOUT_MILLIS = 5000;

   /** Tempo máximo, em milissegundos, para aguardar a resposta do servidor. */
   private static final int READ_TIMEOUT_MILLIS = 5000;

   /** Endereço do servidor de cotações. */
   private final String host;

   /** Porta do servidor de cotações. */
   private final int port;

   /** Nome do grupo cliente. */
   private final String groupName;

   /** Credencial do grupo cliente. */
   private final String groupPassword;

   /** Cache thread-safe de cotações já obtidas, indexado pela data. */
   private final ConcurrentMap<LocalDate, Cotacao> cache = new ConcurrentHashMap<>();

   /**
    * Cria o serviço usando o endereço padrão do servidor e as credenciais do projeto.
    */
   public OraculoService() {
      this(DEFAULT_HOST, DEFAULT_PORT, GROUP_NAME, GROUP_PASSWORD, true);
   }

   /**
    * Cria o serviço para um servidor específico, usando as credenciais do projeto.
    *
    * @param host endereço do servidor
    * @param port porta do servidor
    * @throws IllegalArgumentException se o endereço ou a porta forem inválidos
    */
   public OraculoService(String host, int port) {
      this(host, port, GROUP_NAME, GROUP_PASSWORD, true);
   }

   /**
    * Cria o serviço para o servidor e as credenciais informados.
    *
    * @param host endereço do servidor
    * @param port porta do servidor
    * @param groupName nome do grupo cliente
    * @param groupPassword credencial do grupo cliente
    * @throws IllegalArgumentException se algum parâmetro for inválido
    */
   public OraculoService(String host, int port, String groupName, String groupPassword) {
      this(host, port, groupName, groupPassword, false);
   }

   private OraculoService(String host, int port, String groupName, String groupPassword,
         boolean allowMissingConfiguredPassword) {
      if (host == null || host.isBlank()) {
         throw new IllegalArgumentException("Host do oráculo obrigatório");
      }
      if (port < 1 || port > 65535) {
         throw new IllegalArgumentException("Porta do oráculo inválida");
      }
      if (groupName == null || groupName.isBlank()
            || (!allowMissingConfiguredPassword && (groupPassword == null || groupPassword.isBlank()))) {
         throw new IllegalArgumentException("Credenciais do oráculo obrigatórias");
      }

      this.host = host;
      this.port = port;
      this.groupName = groupName;
      this.groupPassword = groupPassword;
   }

   /**
    * Obtém do servidor a cotação correspondente à data solicitada.
    * O resultado é guardado em cache para consultas futuras da mesma data.
    *
    * @param data data da cotação
    * @return cotação válida para a data
    * @throws OraculoException se a data não tiver cotação ou se a comunicação/
    *                           resposta do servidor for inválida
    */
   @Override
   public Cotacao obterCotacao(LocalDate data) {
      if (data == null) {
         throw new OraculoException(I18n.get("oracle.error.dateRequired"));
      }

      return cache.computeIfAbsent(data, this::consultarCotacaoRemota);
   }

   /**
    * Consulta o servidor por uma única data e converte a resposta em {@link Cotacao}.
    *
    * @param data data da cotação desejada
    * @return cotação correspondente à data
    * @throws OraculoException se não houver cotação para a data ou se o valor recebido for inválido
    */
   private Cotacao consultarCotacaoRemota(LocalDate data) {
      List<ExchangeRate> rates = solicitarCotacoes(data, data);
      for (ExchangeRate rate : rates) {
         if (rate != null && data.equals(rate.getDate())) {
            try {
               return new Cotacao(rate.getDate(), rate.getValue());
            } catch (IllegalArgumentException exception) {
               throw new OraculoException(
                     I18n.getComParametros("oracle.error.invalidQuote", data), exception);
            }
         }
      }
      throw new OraculoException(I18n.getComParametros("oracle.error.quoteUnavailable", data));
   }

   /**
    * Consulta e converte as cotações de um intervalo de datas.
    * As cotações recebidas também são adicionadas ao cache.
    *
    * @param desde primeiro dia do intervalo
    * @param ate último dia do intervalo
    * @return cotações recebidas para o intervalo
    * @throws OraculoException se o intervalo for inválido ou a consulta falhar
    */
   public List<Cotacao> obterCotacoes(LocalDate desde, LocalDate ate) {
      if (desde == null || ate == null || desde.isAfter(ate)) {
         throw new OraculoException(I18n.get("oracle.error.invalidInterval"));
      }

      List<ExchangeRate> rates = solicitarCotacoes(desde, ate);
      try {
         List<Cotacao> cotacoes = rates.stream()
               .filter(Objects::nonNull)
               .map(rate -> new Cotacao(rate.getDate(), rate.getValue()))
               .toList();
         cotacoes.forEach(cotacao -> cache.putIfAbsent(cotacao.getData(), cotacao));
         return cotacoes;
      } catch (RuntimeException exception) {
         throw new OraculoException(I18n.get("oracle.error.invalidQuotes"), exception);
      }
   }

   /**
    * Envia uma requisição ao servidor via socket e lê a resposta serializada.
    *
    * @param desde primeiro dia do intervalo solicitado
    * @param ate último dia do intervalo solicitado
    * @return lista de cotações retornada pelo servidor
    * @throws OraculoException se houver falha de comunicação, resposta inesperada,
    *                           mensagem de erro do servidor ou lista de cotações ausente
    */
   private List<ExchangeRate> solicitarCotacoes(LocalDate desde, LocalDate ate) {
      if (groupPassword == null || groupPassword.isBlank()) {
         throw new OraculoException(I18n.get("oracle.error.passwordMissing"));
      }

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
               throw new OraculoException(I18n.get("oracle.error.unexpectedResponse"));
            }
            response = networkResponse;
         }
      } catch (IOException | ClassNotFoundException exception) {
         LOG.log(Level.SEVERE, "Falha de comunicação com o oráculo", exception);
         throw new OraculoException(I18n.get("oracle.error.communication"), exception);
      }

      if (response.getErrorMessage() != null && !response.getErrorMessage().isBlank()) {
         throw new OraculoException(
               I18n.getComParametros("oracle.error.rejected", response.getErrorMessage()));
      }
      if (response.getExchangeRates() == null) {
         throw new OraculoException(I18n.get("oracle.error.invalidRatesList"));
      }
      return response.getExchangeRates();
   }

   /**
    * Busca a credencial nas variáveis de ambiente do sistema e, em seguida,
    * no arquivo local {@code oraculo.env}.
    */
   private static String obterConfiguracao(String chave, String valorPadrao) {
      String envSistema = System.getenv(chave);
      if (envSistema != null && !envSistema.isBlank()) {
         return envSistema.trim();
      }

      String envArquivo = ENV_LOCAL.get(chave);
      if (envArquivo != null && !envArquivo.isBlank()) {
         return envArquivo.trim();
      }

      return valorPadrao;
   }

   /**
    * Lê pares {@code CHAVE=VALOR} do arquivo local de configuração.
    */
   private static Map<String, String> carregarArquivoEnv(Path caminho) {
      Map<String, String> mapa = new HashMap<>();
      if (!Files.exists(caminho)) {
         return mapa;
      }

      try {
         for (String linha : Files.readAllLines(caminho)) {
            String limpa = linha.trim();
            if (limpa.isEmpty() || limpa.startsWith("#") || !limpa.contains("=")) {
               continue;
            }
            String[] partes = limpa.split("=", 2);
            mapa.put(partes[0].trim(), partes[1].trim());
         }
      } catch (IOException ignored) {
         // Sem arquivo legível, a configuração dependerá somente das variáveis de ambiente.
      }
      return mapa;
   }
}