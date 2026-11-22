package br.ftcoin.service;

import br.ftcoin.dto.ExchangeRate;
import br.ftcoin.model.Cotacao;
import br.ftcoin.network.NetworkRequest;
import br.ftcoin.network.NetworkResponse;
import br.ftcoin.util.I18n;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Integração com o servidor proprietário de cotações FTCoin via TCP Socket.
 * A comunicação, o protocolo de serialização e a conversão de respostas ficam
 * encapsulados nesta classe.
 *
 * <p>As cotações já consultadas são armazenadas em um cache em memória
 * ({@link ConcurrentHashMap}) indexado por {@link LocalDate}, evitando requisições
 * repetidas ao servidor remoto para a mesma data.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class OraculoService implements OraculoCotacao {

    /** Nome do grupo cliente usado na autenticação junto ao servidor. */
    private static final String GROUP_NAME = "POO2_2s26_A02";

    /** Credencial do grupo cliente usada na autenticação junto ao servidor. */
    private static final String GROUP_PASSWORD = "W90OL8wsC8";

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
        this(DEFAULT_HOST, DEFAULT_PORT, GROUP_NAME, GROUP_PASSWORD);
    }

    /**
     * Cria o serviço para um servidor específico, usando as credenciais do projeto.
     *
     * @param host Endereço do servidor.
     * @param port Porta do servidor.
     * @throws IllegalArgumentException se o endereço ou a porta forem inválidos.
     */
    public OraculoService(String host, int port) {
        this(host, port, GROUP_NAME, GROUP_PASSWORD);
    }

    /**
     * Cria o serviço para o servidor e as credenciais informados.
     *
     * @param host          Endereço do servidor.
     * @param port          Porta do servidor.
     * @param groupName     Nome do grupo cliente.
     * @param groupPassword Credencial do grupo cliente.
     * @throws IllegalArgumentException se algum parâmetro for inválido.
     */
    public OraculoService(String host, int port, String groupName, String groupPassword) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException(I18n.get("oracle.error.hostRequired"));
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(I18n.get("oracle.error.invalidPort"));
        }
        if (groupName == null || groupName.isBlank() || groupPassword == null || groupPassword.isBlank()) {
            throw new IllegalArgumentException(I18n.get("oracle.error.credentialsRequired"));
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
     * @param data Data da cotação.
     * @return Cotação válida para a data.
     * @throws OraculoException se a data não tiver cotação ou se a comunicação/
     *                          resposta do servidor for inválida.
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
     * @param data Data da cotação desejada.
     * @return Cotação correspondente à data.
     * @throws OraculoException se não houver cotação para a data ou se o valor recebido for inválido.
     */
    private Cotacao consultarCotacaoRemota(LocalDate data) {
        List<ExchangeRate> rates = solicitarCotacoes(data, data);
        for (ExchangeRate rate : rates) {
            if (rate != null && data.equals(rate.getDate())) {
                try {
                    return new Cotacao(rate.getDate(), rate.getValue());
                } catch (IllegalArgumentException exception) {
                    throw new OraculoException(
                            I18n.getComParametros("oracle.error.invalidQuoteForDate", data.toString()),
                            exception);
                }
            }
        }
        throw new OraculoException(I18n.getComParametros("oracle.error.notFoundForDate", data.toString()));
    }

    /**
     * Consulta e converte as cotações de um intervalo de datas.
     * As cotações recebidas também são adicionadas ao cache.
     *
     * @param desde Primeiro dia do intervalo.
     * @param ate   Último dia do intervalo.
     * @return Cotações recebidas para o intervalo.
     * @throws OraculoException se o intervalo for inválido ou a consulta falhar.
     */
    public List<Cotacao> obterCotacoes(LocalDate desde, LocalDate ate) {
        if (desde == null || ate == null || desde.isAfter(ate)) {
            throw new OraculoException(I18n.get("oracle.error.invalidRange"));
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
            throw new OraculoException(I18n.get("oracle.error.invalidQuotesList"), exception);
        }
    }

    /**
     * Envia uma requisição ao servidor via socket TCP e lê a resposta serializada.
     *
     * @param desde Primeiro dia do intervalo solicitado.
     * @param ate   Último dia do intervalo solicitado.
     * @return Lista de cotações retornada pelo servidor.
     * @throws OraculoException se houver falha de comunicação, resposta inesperada,
     *                          mensagem de erro do servidor ou lista de cotações ausente.
     */
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
                    throw new OraculoException(I18n.get("oracle.error.unexpectedResponse"));
                }
                response = networkResponse;
            }
        } catch (IOException | ClassNotFoundException exception) {
            throw new OraculoException(I18n.get("oracle.error.connectionFailed"), exception);
        }

        if (response.getErrorMessage() != null && !response.getErrorMessage().isBlank()) {
            throw new OraculoException(
                    I18n.getComParametros("oracle.error.serverRefused", response.getErrorMessage()));
        }
        if (response.getExchangeRates() == null) {
            throw new OraculoException(I18n.get("oracle.error.invalidQuotesList"));
        }
        return response.getExchangeRates();
    }
}