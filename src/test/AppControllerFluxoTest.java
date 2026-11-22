package test;

import br.ftcoin.enums.Idioma;
import br.ftcoin.util.I18n;
import br.ftcoin.controller.AppController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de integração do programa em memória, usando o {@link AppController} real com a
 * digitação simulada: garante que os menus de Carteira, Movimentação e Relatórios
 * enxergam os MESMOS dados (compartilham o mesmo repositório) e que as operações com data
 * funcionam de ponta a ponta. Não precisa de banco nem de rede.
 */
class AppControllerFluxoTest {

    private final InputStream entradaOriginal = System.in;
    private final PrintStream saidaOriginal = System.out;
    private ByteArrayOutputStream capturada;

    @BeforeEach
    void setUp() {
        I18n.setIdioma(Idioma.PORTUGUES);
        capturada = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturada, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restaurar() {
        System.setIn(entradaOriginal);
        System.setOut(saidaOriginal);
        I18n.setIdioma(Idioma.PORTUGUES);
    }

    @Test
    void carteiraCadastradaNoMenuCarteiraApareceNosRelatorios() {
        // 1 = Carteira > 1 = cadastrar > id, titular, corretora > ENTER > 0 voltar
        // 3 = Relatórios > 1 = por identificador > ENTER > 0 voltar; 0 = sair
        String tela = rodar("1\n1\n1\nAna Lima\nCorretora X\n\n0\n3\n1\n\n0\n0\n");

        assertTrue(tela.contains(I18n.get("report.wallets.byId.title")), "a tela do relatório deveria abrir");
        assertTrue(tela.lastIndexOf("Ana Lima") > tela.indexOf(I18n.get("report.wallets.byId.title")),
                "a carteira cadastrada deveria aparecer no relatório");
        assertFalse(tela.contains(I18n.get("report.wallets.empty")),
                "o relatório não deveria dizer que não há carteiras");
    }

    @Test
    void carteiraCadastradaNoMenuCarteiraPodeReceberMovimentacoesComData() {
        // cadastra a carteira 1; em Movimentação (2): compra (1) na carteira 1, data 2026-02-01, 1000 moedas;
        // depois uma venda (2) de 500 em 2026-01-01, que não tem saldo e deve ser recusada
        String tela = rodar("1\n1\n1\nAna Lima\nCorretora X\n\n0\n"
                + "2\n1\n1\n2026-02-01\n1000\n\n"
                + "2\n1\n2026-01-01\n500\n\n0\n0\n");

        assertTrue(tela.contains(I18n.get("transaction.buy.success")), "a compra deveria ser registrada");
        assertTrue(tela.contains(I18n.get("transaction.sell.insufficientBalance")),
                "a venda sem saldo deveria ser recusada");
    }

    @Test
    void oProgramaEncerraNormalmenteAoEscolherSair() {
        String tela = rodar("0\n");

        assertTrue(tela.contains(I18n.get("mainMenu.exit")));
    }

    private String rodar(String digitado) {
        System.setIn(new ByteArrayInputStream(digitado.getBytes(StandardCharsets.UTF_8)));
        new AppController("MEMORIA").iniciar();
        return capturada.toString(StandardCharsets.UTF_8);
    }
}