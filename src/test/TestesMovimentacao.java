package test;

import br.ftcoin.controller.MovimentacaoController;
import br.ftcoin.dao.MovimentacaoDAO;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.model.Movimentacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/*
 * Testes sem JUnit e sem Maven: basta executar o main desta classe.
 * Cada teste imprime OK ou FALHA; no fim mostra o resumo.
 */
public class TestesMovimentacao {

    private static final LocalDate DATA = LocalDate.of(2026, 10, 18);
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

        teste("quantidade zero é aceita", () -> {
            Movimentacao m = new Movimentacao(1, DATA, TipoOperacao.COMPRA, BigDecimal.ZERO);
            igual(0, m.getQuantidade().compareTo(BigDecimal.ZERO));
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

        teste("quantidade negativa lança exceção", () ->
                lancaValidacao(() -> new Movimentacao(1, DATA, TipoOperacao.COMPRA, new BigDecimal("-0.01"))));

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

        teste("toString contém os dados", () -> {
            String s = new Movimentacao(5, 2, DATA, TipoOperacao.VENDA, QTD).toString();
            verdadeiro(s.contains("id=5"));
            verdadeiro(s.contains("carteira=2"));
            verdadeiro(s.contains("VENDA"));
            verdadeiro(s.contains("1.5"));
        });
    }

    // ---------- Testes do controller ----------

    private static void testesController() {
        System.out.println("\n=== CONTROLLER ===");

        teste("cadastrar persiste e preenche o id", () -> {
            DAOFalso dao = new DAOFalso();
            MovimentacaoController c = new MovimentacaoController(dao);

            Movimentacao m = c.cadastrar(1, DATA, TipoOperacao.COMPRA, QTD);

            verdadeiro(m.getIdMovimento() > 0);
            igual(1, dao.dados.size());
            verdadeiro(dao.consultar(m.getIdMovimento()).isPresent());
        });

        teste("cadastrar inválido lança exceção e não persiste", () -> {
            DAOFalso dao = new DAOFalso();
            MovimentacaoController c = new MovimentacaoController(dao);

            lancaValidacao(() -> c.cadastrar(0, DATA, TipoOperacao.COMPRA, QTD));
            lancaValidacao(() -> c.cadastrar(1, null, TipoOperacao.COMPRA, QTD));
            lancaValidacao(() -> c.cadastrar(1, DATA, null, QTD));
            lancaValidacao(() -> c.cadastrar(1, DATA, TipoOperacao.COMPRA, new BigDecimal("-1")));

            verdadeiro(dao.dados.isEmpty());
        });

        teste("atualizar altera o registro existente", () -> {
            DAOFalso dao = new DAOFalso();
            MovimentacaoController c = new MovimentacaoController(dao);
            Movimentacao original = c.cadastrar(1, DATA, TipoOperacao.COMPRA, QTD);

            c.atualizar(original.getIdMovimento(), 2, LocalDate.of(2026, 1, 1),
                    TipoOperacao.VENDA, new BigDecimal("9"));

            Movimentacao salva = dao.dados.get(original.getIdMovimento());
            igual(2, salva.getIdCarteira());
            igual(TipoOperacao.VENDA, salva.getTipoOperacao());
            igual(new BigDecimal("9"), salva.getQuantidade());
            igual(1, dao.dados.size());
        });

        teste("atualizar inexistente lança exceção", () -> {
            MovimentacaoController c = new MovimentacaoController(new DAOFalso());
            lancaValidacao(() -> c.atualizar(99, 1, DATA, TipoOperacao.COMPRA, QTD));
        });

        teste("atualizar com id inválido lança exceção", () -> {
            MovimentacaoController c = new MovimentacaoController(new DAOFalso());
            lancaValidacao(() -> c.atualizar(0, 1, DATA, TipoOperacao.COMPRA, QTD));
            lancaValidacao(() -> c.atualizar(-3, 1, DATA, TipoOperacao.COMPRA, QTD));
        });

        teste("atualizar com dados inválidos não altera o registro", () -> {
            DAOFalso dao = new DAOFalso();
            MovimentacaoController c = new MovimentacaoController(dao);
            Movimentacao original = c.cadastrar(1, DATA, TipoOperacao.COMPRA, QTD);

            lancaValidacao(() -> c.atualizar(original.getIdMovimento(), 1, DATA,
                    TipoOperacao.COMPRA, new BigDecimal("-1")));

            igual(QTD, dao.dados.get(original.getIdMovimento()).getQuantidade());
        });

        teste("excluir remove o registro", () -> {
            DAOFalso dao = new DAOFalso();
            MovimentacaoController c = new MovimentacaoController(dao);
            Movimentacao m = c.cadastrar(1, DATA, TipoOperacao.COMPRA, QTD);

            c.excluir(m.getIdMovimento());

            verdadeiro(dao.dados.isEmpty());
        });

        teste("excluir inexistente lança exceção", () -> {
            MovimentacaoController c = new MovimentacaoController(new DAOFalso());
            lancaValidacao(() -> c.excluir(42));
        });

        teste("buscarPorId retorna o registro", () -> {
            MovimentacaoController c = new MovimentacaoController(new DAOFalso());
            Movimentacao m = c.cadastrar(1, DATA, TipoOperacao.VENDA, QTD);

            Movimentacao achada = c.buscarPorId(m.getIdMovimento());

            igual(m.getIdMovimento(), achada.getIdMovimento());
            igual(TipoOperacao.VENDA, achada.getTipoOperacao());
        });

        teste("buscarPorId inexistente lança exceção", () -> {
            MovimentacaoController c = new MovimentacaoController(new DAOFalso());
            lancaValidacao(() -> c.buscarPorId(1));
        });

        teste("listarTodas retorna tudo", () -> {
            MovimentacaoController c = new MovimentacaoController(new DAOFalso());
            c.cadastrar(1, DATA, TipoOperacao.COMPRA, QTD);
            c.cadastrar(2, DATA, TipoOperacao.VENDA, QTD);

            igual(2, c.listarTodas().size());
        });

        teste("listarTodas vazia retorna lista vazia", () -> {
            MovimentacaoController c = new MovimentacaoController(new DAOFalso());
            verdadeiro(c.listarTodas().isEmpty());
        });

        teste("listarPorCarteira filtra corretamente", () -> {
            MovimentacaoController c = new MovimentacaoController(new DAOFalso());
            c.cadastrar(1, DATA, TipoOperacao.COMPRA, QTD);
            c.cadastrar(1, DATA, TipoOperacao.VENDA, QTD);
            c.cadastrar(2, DATA, TipoOperacao.COMPRA, QTD);

            List<Movimentacao> da1 = c.listarPorCarteira(1);

            igual(2, da1.size());
            verdadeiro(da1.stream().allMatch(m -> m.getIdCarteira() == 1));
            igual(1, c.listarPorCarteira(2).size());
            verdadeiro(c.listarPorCarteira(3).isEmpty());
        });

        teste("listarPorCarteira com id inválido lança exceção", () -> {
            MovimentacaoController c = new MovimentacaoController(new DAOFalso());
            lancaValidacao(() -> c.listarPorCarteira(0));
            lancaValidacao(() -> c.listarPorCarteira(-1));
        });
    }

    // ---------- DAO falso em memória ----------

    private static class DAOFalso implements MovimentacaoDAO {

        final Map<Integer, Movimentacao> dados = new LinkedHashMap<>();
        private int proximoId = 1;

        @Override
        public void incluir(Movimentacao m) {
            m.setIdMovimento(proximoId++);
            dados.put(m.getIdMovimento(), m);
        }

        @Override
        public Optional<Movimentacao> consultar(Integer id) {
            return Optional.ofNullable(dados.get(id));
        }

        @Override
        public List<Movimentacao> consultarTodos() {
            return new ArrayList<>(dados.values());
        }

        @Override
        public void editar(Movimentacao m) {
            dados.put(m.getIdMovimento(), m);
        }

        @Override
        public void excluir(Integer id) {
            dados.remove(id);
        }

        @Override
        public List<Movimentacao> consultarPorCarteira(int idCarteira) {
            List<Movimentacao> lista = new ArrayList<>();
            for (Movimentacao m : dados.values()) {
                if (m.getIdCarteira() == idCarteira) {
                    lista.add(m);
                }
            }
            return lista;
        }
    }
}