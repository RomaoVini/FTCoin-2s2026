package br.ftcoin.util;

import br.ftcoin.enums.Idioma;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes da internacionalização: os três idiomas (português, inglês e espanhol) devem ter
 * exatamente as mesmas chaves, nenhuma mensagem vazia e os mesmos parâmetros; e a troca
 * de idioma em {@link I18n} deve mudar os textos exibidos.
 */
class MensagensI18nTest {

    private static final Pattern PARAMETRO = Pattern.compile("\\{(\\d+)\\}");

    @BeforeEach
    @AfterEach
    void voltarParaPortugues() {
        I18n.setIdioma(Idioma.PORTUGUES);
    }

    @Test
    void osTresIdiomasTemExatamenteAsMesmasChaves() {
        Set<String> pt = chaves(Idioma.PORTUGUES);

        assertEquals(new TreeSet<>(pt), new TreeSet<>(chaves(Idioma.INGLES)));
        assertEquals(new TreeSet<>(pt), new TreeSet<>(chaves(Idioma.ESPANHOL)));
        assertFalse(pt.isEmpty());
    }

    @Test
    void nenhumaMensagemEstaVazia() {
        for (Idioma idioma : Idioma.values()) {
            ResourceBundle bundle = bundle(idioma);
            for (String chave : bundle.keySet()) {
                assertFalse(bundle.getString(chave).isBlank(), idioma + " tem mensagem vazia em " + chave);
            }
        }
    }

    @Test
    void osParametrosDeCadaMensagemSaoOsMesmosNosTresIdiomas() {
        ResourceBundle pt = bundle(Idioma.PORTUGUES);
        ResourceBundle en = bundle(Idioma.INGLES);
        ResourceBundle es = bundle(Idioma.ESPANHOL);

        for (String chave : pt.keySet()) {
            Set<String> esperado = parametros(pt.getString(chave));
            assertEquals(esperado, parametros(en.getString(chave)), "parâmetros diferentes em inglês: " + chave);
            assertEquals(esperado, parametros(es.getString(chave)), "parâmetros diferentes em espanhol: " + chave);
        }
    }

    @Test
    void trocarDeIdiomaMudaOsTextosExibidos() {
        I18n.setIdioma(Idioma.PORTUGUES);
        String pt = I18n.get("mainMenu.wallet");
        I18n.setIdioma(Idioma.INGLES);
        String en = I18n.get("mainMenu.wallet");
        I18n.setIdioma(Idioma.ESPANHOL);
        String es = I18n.get("mainMenu.wallet");

        assertEquals("Carteira", pt);
        assertEquals("Wallet", en);
        assertEquals("Billetera", es);
        assertEquals(Idioma.ESPANHOL, I18n.getIdiomaAtual());
    }

    @Test
    void idiomaNuloVoltaParaPortugues() {
        I18n.setIdioma(Idioma.INGLES);

        I18n.setIdioma(null);

        assertEquals(Idioma.PORTUGUES, I18n.getIdiomaAtual());
    }

    @Test
    void chaveInexistenteDevolveUmMarcadorEmVezDeQuebrar() {
        assertEquals("!chave.que.nao.existe!", I18n.get("chave.que.nao.existe"));
        assertEquals("", I18n.get(null));
        assertEquals("", I18n.get("  "));
    }

    @Test
    void parametrosSaoSubstituidosNaMensagem() {
        String mensagem = I18n.getComParametros("wallet.error.notFound", "42");

        assertTrue(mensagem.contains("42"));
        assertFalse(mensagem.contains("{0}"));
    }

    @Test
    void mensagemDeErroDoDominioSaiNoIdiomaAtivo() {
        I18n.setIdioma(Idioma.INGLES);

        assertEquals("Error: Insufficient balance to perform this operation!",
                I18n.get("transaction.sell.insufficientBalance"));
    }

    private static ResourceBundle bundle(Idioma idioma) {
        return ResourceBundle.getBundle("messages", idioma.getLocale());
    }

    private static Set<String> chaves(Idioma idioma) {
        return new HashSet<>(bundle(idioma).keySet());
    }

    private static Set<String> parametros(String mensagem) {
        Set<String> numeros = new TreeSet<>();
        Matcher m = PARAMETRO.matcher(mensagem);
        while (m.find()) {
            numeros.add(m.group(1));
        }
        return numeros;
    }
}