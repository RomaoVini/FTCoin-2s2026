package br.ftcoin.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Resolve a codificação usada pelo console do sistema operacional.
 */
public final class ConsoleEncoding {

    private ConsoleEncoding() {
    }

    /**
     * Retorna a página de código ativa no Windows ou UTF-8 como padrão seguro.
     */
    public static Charset getCharset() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            return StandardCharsets.UTF_8;
        }

        try {
            Process process = new ProcessBuilder("cmd.exe", "/d", "/c", "chcp")
                    .redirectErrorStream(true)
                    .start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.US_ASCII))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String pagina = line.replaceAll("\\D+", "");
                    if (!pagina.isEmpty()) {
                        process.waitFor();
                        return mapearPagina(pagina);
                    }
                }
            }
        } catch (Exception ignored) {
            // Mantém o fallback abaixo.
        }
        return Charset.defaultCharset();
    }

    private static Charset mapearPagina(String pagina) {
        return switch (pagina) {
            case "437" -> Charset.forName("IBM437");
            case "850" -> Charset.forName("IBM850");
            case "1252" -> Charset.forName("windows-1252");
            case "65001" -> StandardCharsets.UTF_8;
            default -> Charset.defaultCharset();
        };
    }
}
