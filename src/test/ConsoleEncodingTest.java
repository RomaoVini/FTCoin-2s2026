package test;

import br.ftcoin.util.ConsoleEncoding;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleEncodingTest {

    @Test
    void getCharset_retornaCharsetValidoENaoNulo() {
        Charset charset = ConsoleEncoding.getCharset();

        assertNotNull(charset);
        assertTrue(Charset.isSupported(charset.name()));
    }
}