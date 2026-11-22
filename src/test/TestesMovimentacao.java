package test;

import br.ftcoin.controller.MovimentacaoController;
import br.ftcoin.dao.CarteiraDAOMemoria;
import br.ftcoin.dao.MovimentacaoDAOMemoria;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Carteira;
import br.ftcoin.model.Cotacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.service.OraculoCotacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/*
 * Testes sem JUnit e sem Maven: basta executar o main desta classe.
 * Cada teste imprime OK ou FALHA; no fim mostra o resumo.
 */
public class TestesMovimentacao {

    private static final LocalDate DATA = LocalDate.of(2026, 2, 10);
    private static final BigDecimal QTD = new BigDecimal("1.5");

    private static int total = 0;
    private static int falhas = 0;

    // ---------- Mini framework ----------

    interface Codigo {
        void executar() throws Exception;
    }

    static void teste(String nome, Codigo codigo) {
        total++;
        try {
            codigo.executar();
            System.out.println("OK     " + nome);
        } catch (Throwable e) {
            falhas++;
            System.out.println("FALHA  " + nome + "  ->  " + e);
        }
    }

    static void verdadeiro(boolean condicao) {
        if (!condicao) {
            throw new AssertionError("esperava true");
        }
    }

    static void falso(boolean condicao) {
        if (condicao) {
            throw new AssertionError("esperava false");
        }
    }

    static void igual(Object esperado, Object atual) {
        if (!Objects.equals(esperado, atual)) {
            throw new AssertionError("esperado=" + esperado + " atual=" + atual);
        }
    }

    static void lancaValidacao(Codigo codigo) {
        try {
            codigo.executar();
        } catch (ValidacaoException e) {
            return;
        } catch (Exception e) {
            throw new AssertionError("exceção diferente da esperada: " + e);
        }
        throw new AssertionError("esperava ValidacaoException, mas nada foi lançado");
    }

    // ---------- Main ----------

    public static void main(String[] args) {
        testesModel();
        testesController();

        System.out.println("\n" + (total - falhas) + "/" + total + " testes passaram.");
        System.exit(falhas == 0 ? 0 : 1);
    }

    // ---------- Testes do model ----------

    private static Movimentacao valida() {
        return new Movimentacao(1, DATA, TipoOperacao.COMPRA, QTD);
    }

    private static void testesModel() {
        System.out.println("=== MODEL ===");

        teste("construtor sem id usa idMovimento = 0", () -> {
            Movimentacao m = valida();
            igual(0, m.getIdMovimento());
            igual(1, m.getIdCarteira());
            igual(DATA, m.getDataOperacao());
            igual(TipoOperacao.COMPRA, m.getTipoOperacao());
            igual(QTD, m.getQuantidade());
        });

        teste("construtor completo guarda os valores", () -> {
            Movimentacao m = new Movimentacao(7, 3, DATA, TipoOperacao.VENDA, QTD);
            igual(7, m.getIdMovimento());
            igual(3, m.getIdCarteira());
            igual(TipoOperacao.VENDA, m.getTipoOperacao());
        });

        teste("construtor com char converte C e V (aceita minúscula)", () -> {
            igual(TipoOperacao.COMPRA, new Movimentacao(1, 1, DATA, 'C', QTD).getTipoOperacao());
            igual(TipoOperacao.VENDA, new Movimentacao(1, 1, DATA, 'V', QTD).getTipoOperacao());
            igual(TipoOperacao.VENDA, new Movimentacao(1, 1, DATA, 'v', QTD).getTipoOperacao());
        });

        teste("idMovimento negativo lança exceção", () ->
                lancaValidacao(() -> new Movimentacao(-1, 1, DATA, TipoOperacao.COMPRA, QTD)));

        teste("idCarteira zero lança exceção", () ->
                lancaValidacao(() -> new Movimentacao(0, DATA, TipoOperacao.COMPRA, QTD)));

        teste("idCarteira negativo lança exceção", () ->
                lancaValidacao(() -> new Movimentacao(-5, DATA, TipoOperacao.COMPRA, QTD)));

        teste("data nula lança exceção", () ->
                lancaValidacao(() -> new Movimentacao(1, null, TipoOperacao.COMPRA, QTD)));

        teste("tipo nulo lança exceção", () ->
                lancaValidacao(() -> new Movimentacao(1, DATA, (TipoOperacao) null, QTD)));

        teste("tipo char inválido lança exceção", () ->
                lancaValidacao(() -> new Movimentacao(1, 1, DATA, 'X', QTD)));

        teste("quantidade nula lança exceção", () ->
                lancaValidacao(() -> new Movimentacao(1, DATA, TipoOperacao.COMPRA, null)));

        teste("quantidade zero ou negativa lança exceção", () -> {
            lancaValidacao(() -> new Movimentacao(1, DATA, TipoOperacao.COMPRA, BigDecimal.ZERO));
            lancaValidacao(() -> new Movimentacao(1, DATA, TipoOperacao.COMPRA, new BigDecimal("-0.01")));
        });

        teste("setters válidos atualizam os campos", () -> {
            Movimentacao m = valida();
            m.setIdMovimento(10);
            m.setIdCarteira(2);
            m.setDataOperacao(LocalDate.of(2026, 1, 1));
            m.setTipoOperacao(TipoOperacao.VENDA);
            m.setQuantidade(new BigDecimal("9"));

            igual(10, m.getIdMovimento());
            igual(2, m.getIdCarteira());
            igual(LocalDate.of(2026, 1, 1), m.getDataOperacao());
            igual(TipoOperacao.VENDA, m.getTipoOperacao());
            igual(new BigDecimal("9"), m.getQuantidade());
        });

        teste("setters inválidos lançam exceção e preservam o estado", () -> {
            Movimentacao m = new Movimentacao(4, 1, DATA, TipoOperacao.COMPRA, QTD);

            lancaValidacao(() -> m.setIdMovimento(-1));
            lancaValidacao(() -> m.setIdCarteira(0));
            lancaValidacao(() -> m.setDataOperacao(null));
            lancaValidacao(() -> m.setTipoOperacao(null));
            lancaValidacao(() -> m.setQuantidade(null));
            lancaValidacao(() -> m.setQuantidade(new BigDecimal("-1")));

            igual(4, m.getIdMovimento());
            igual(1, m.getIdCarteira());
            igual(DATA, m.getDataOperacao());
            igual(TipoOperacao.COMPRA, m.getTipoOperacao());
            igual(QTD, m.getQuantidade());
        });

        teste("isCompra e isVenda", () -> {
            Movimentacao compra = new Movimentacao(1, DATA, TipoOperacao.COMPRA, QTD);
            Movimentacao venda = new Movimentacao(1, DATA, TipoOperacao.VENDA, QTD);
            verdadeiro(compra.isCompra());
            falso(compra.isVenda());
            verdadeiro(venda.isVenda());
            falso(venda.isCompra());
        });
    }

    // ---------- Apoio ----------

    private static MovimentacaoController novoController(CarteiraDAOMemoria carteiraDAO,
                                                         MovimentacaoDAOMemoria movimentacaoDAO) {
        OraculoCotacao oraculoSimulado = data -> new Cotacao(data, new BigDecimal("10.00"));
        return new MovimentacaoController(carteiraDAO, movimentacaoDAO, oraculoSimulado);
    }

    // ---------- Testes do controller ----------

    private static void testesController() {
        System.out.println("\n=== CONTROLLER ===");

        teste("cadastrar persiste e preenche o id", () -> {
            CarteiraDAOMemoria carteiras = new CarteiraDAOMemoria();
            carteiras.incluir(new Carteira(1, "Ana", "Corretora"));
            MovimentacaoDAOMemoria dao = new MovimentacaoDAOMemoria();
            MovimentacaoController c = novoController(carteiras, dao);

            Movimentacao m = c.cadastrar(1, DATA, TipoOperacao.COMPRA, QTD);

            verdadeiro(m.getIdMovimento() > 0);
            igual(1, dao.consultarTodos().size());
            verdadeiro(dao.consultar(m.getIdMovimento()).isPresent());
        });

        teste("cadastrar para carteira inexistente ou dados inválidos lança exceção", () -> {
            CarteiraDAOMemoria carteiras = new CarteiraDAOMemoria();
            carteiras.incluir(new Carteira(1, "Ana", "Corretora"));
            MovimentacaoDAOMemoria dao = new MovimentacaoDAOMemoria();
            MovimentacaoController c = novoController(carteiras, dao);

            lancaValidacao(() -> c.cadastrar(99, DATA, TipoOperacao.COMPRA, QTD));
            lancaValidacao(() -> c.cadastrar(0, DATA, TipoOperacao.COMPRA, QTD));
            lancaValidacao(() -> c.cadastrar(1, null, TipoOperacao.COMPRA, QTD));
            lancaValidacao(() -> c.cadastrar(1, DATA, null, QTD));
            lancaValidacao(() -> c.cadastrar(1, DATA, TipoOperacao.COMPRA, new BigDecimal("-1")));
            lancaValidacao(() -> c.cadastrar(1, DATA, TipoOperacao.VENDA, QTD));

            verdadeiro(dao.consultarTodos().isEmpty());
        });

        teste("venda com saldo suficiente é permitida e sem saldo é bloqueada", () -> {
            CarteiraDAOMemoria carteiras = new CarteiraDAOMemoria();
            carteiras.incluir(new Carteira(1, "Ana", "Corretora"));
            MovimentacaoDAOMemoria dao = new MovimentacaoDAOMemoria();
            MovimentacaoController c = novoController(carteiras, dao);

            c.cadastrar(1, DATA, TipoOperacao.COMPRA, new BigDecimal("10"));
            c.cadastrar(1, DATA.plusDays(1), TipoOperacao.VENDA, new BigDecimal("4"));
            lancaValidacao(() -> c.cadastrar(1, DATA.plusDays(2), TipoOperacao.VENDA, new BigDecimal("7")));

            igual(2, dao.consultarTodos().size());
        });

        teste("consultarCotacao retorna cotação do oráculo", () -> {
            MovimentacaoController c = novoController(new CarteiraDAOMemoria(), new MovimentacaoDAOMemoria());
            Cotacao cotacao = c.consultarCotacao(DATA);
            igual(DATA, cotacao.getData());
            igual(new BigDecimal("10.00"), cotacao.getValor());
        });

        teste("listarTodas retorna tudo", () -> {
            CarteiraDAOMemoria carteiras = new CarteiraDAOMemoria();
            carteiras.incluir(new Carteira(1, "Ana", "Corretora"));
            carteiras.incluir(new Carteira(2, "Bruno", "Corretora"));
            MovimentacaoController c = novoController(carteiras, new MovimentacaoDAOMemoria());

            c.cadastrar(1, DATA, TipoOperacao.COMPRA, QTD);
            c.cadastrar(2, DATA, TipoOperacao.COMPRA, QTD);

            igual(2, c.listarTodas().size());
        });

        teste("listarTodas vazia retorna lista vazia", () -> {
            MovimentacaoController c = novoController(new CarteiraDAOMemoria(), new MovimentacaoDAOMemoria());
            verdadeiro(c.listarTodas().isEmpty());
        });

        teste("listarPorCarteira filtra corretamente", () -> {
            CarteiraDAOMemoria carteiras = new CarteiraDAOMemoria();
            carteiras.incluir(new Carteira(1, "Ana", "Corretora"));
            carteiras.incluir(new Carteira(2, "Bruno", "Corretora"));
            MovimentacaoController c = novoController(carteiras, new MovimentacaoDAOMemoria());

            c.cadastrar(1, DATA, TipoOperacao.COMPRA, QTD);
            c.cadastrar(1, DATA.plusDays(1), TipoOperacao.VENDA, new BigDecimal("0.5"));
            c.cadastrar(2, DATA, TipoOperacao.COMPRA, QTD);

            List<Movimentacao> da1 = c.listarPorCarteira(1);

            igual(2, da1.size());
            verdadeiro(da1.stream().allMatch(m -> m.getIdCarteira() == 1));
            igual(1, c.listarPorCarteira(2).size());
            verdadeiro(c.listarPorCarteira(3).isEmpty());
        });

        teste("listarPorCarteira com id inválido lança exceção", () -> {
            MovimentacaoController c = novoController(new CarteiraDAOMemoria(), new MovimentacaoDAOMemoria());
            lancaValidacao(() -> c.listarPorCarteira(0));
            lancaValidacao(() -> c.listarPorCarteira(-1));
        });
    }
}