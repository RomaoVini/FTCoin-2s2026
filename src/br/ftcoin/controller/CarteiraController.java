package br.ftcoin.controller;

import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.dao.DAOFactory;
import br.ftcoin.exception.ValidacaoException;
import br.ftcoin.model.Carteira;
import br.ftcoin.view.CarteiraView;
import br.ftcoin.view.MenuView;

import java.util.List;
import java.util.Objects;

/**
 * Controlador responsável por gerenciar as operações de negócio e fluxo
 * relacionadas à entidade {@link Carteira} (cadastro, consulta, edição e exclusão).
 * Traduz violações de regra de negócio em {@link ValidacaoException} internacionalizadas.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class CarteiraController {

    private final CarteiraDAO dao;
    private CarteiraView view;

    /**
     * Constrói o controlador extraindo o DAO diretamente de uma fábrica abstrata.
     *
     * @param factory Fábrica concreta de DAOs (Memória ou MariaDB).
     * @throws NullPointerException se {@code factory} for nulo.
     */
    public CarteiraController(DAOFactory factory) {
        this(Objects.requireNonNull(factory, "DAOFactory não pode ser nula.").getCarteiraDAO());
    }

    /**
     * Constrói o controlador injetando diretamente uma implementação de {@link CarteiraDAO}.
     * Útil para injeção em testes unitários sem dependência de interface gráfica/CLI.
     *
     * @param dao Implementação de acesso a dados de Carteira.
     * @throws NullPointerException se {@code dao} for nulo.
     */
    public CarteiraController(CarteiraDAO dao) {
        this.dao = Objects.requireNonNull(dao, "CarteiraDAO não pode ser nulo.");
    }

    /**
     * Constrói o controlador completo para execução na interface de linha de comando (CLI),
     * utilizado pelo {@link AppController}.
     *
     * @param menuView View genérica de menus da aplicação.
     * @param dao      Implementação de acesso a dados de Carteira.
     * @throws NullPointerException se {@code menuView} ou {@code dao} forem nulos.
     */
    public CarteiraController(MenuView menuView, CarteiraDAO dao) {
        this(dao);
        Objects.requireNonNull(menuView, "MenuView não pode ser nula.");
        this.view = new CarteiraView(menuView.getConsoleUtils(), this);
    }

    /**
     * Inicia a exibição e o loop de interação do submenu de Carteiras na CLI.
     *
     * @throws IllegalStateException se o controlador tiver sido instanciado sem uma {@link CarteiraView}.
     */
    public void exibirMenuCarteira() {
        if (this.view == null) {
            throw new IllegalStateException("CarteiraView não foi inicializada neste controlador.");
        }
        view.exibirMenu();
    }

    /**
     * Valida os dados e cadastra uma nova carteira no repositório ativo.
     *
     * @param id        Identificador numérico único da carteira (deve ser maior que zero).
     * @param titular   Nome do proprietário da carteira.
     * @param corretora Nome da corretora custodiante.
     * @return A entidade {@link Carteira} recém-cadastrada.
     * @throws ValidacaoException se algum campo for inválido ou se o ID já estiver em uso.
     */
    public Carteira cadastrar(int id, String titular, String corretora) {
        validarId(id);
        validarTexto(titular, "wallet.error.holderRequired");
        validarTexto(corretora, "wallet.error.brokerRequired");

        if (dao.consultar(id).isPresent()) {
            throw new ValidacaoException("wallet.error.duplicateId", String.valueOf(id));
        }

        Carteira carteira = new Carteira(id, titular.trim(), corretora.trim());
        dao.incluir(carteira);
        return carteira;
    }

    /**
     * Atualiza os dados cadastrais (titular e corretora) de uma carteira existente.
     *
     * @param id        Identificador da carteira a ser alterada.
     * @param titular   Novo nome do titular.
     * @param corretora Novo nome da corretora.
     * @return A entidade {@link Carteira} atualizada.
     * @throws ValidacaoException se os dados forem inválidos ou a carteira não for encontrada.
     */
    public Carteira atualizar(int id, String titular, String corretora) {
        validarId(id);
        validarTexto(titular, "wallet.error.holderRequired");
        validarTexto(corretora, "wallet.error.brokerRequired");
        buscarOuFalhar(id);

        Carteira carteira = new Carteira(id, titular.trim(), corretora.trim());
        dao.editar(carteira);
        return carteira;
    }

    /**
     * Remove uma carteira do repositório, bloqueando a exclusão caso existam
     * movimentações financeiras vinculadas a ela.
     *
     * @param id Identificador da carteira a ser excluída.
     * @throws ValidacaoException se o ID for inválido, não existir ou possuir histórico vinculado.
     */
    public void excluir(int id) {
        validarId(id);
        buscarOuFalhar(id);

        try {
            dao.excluir(id);
        } catch (IllegalStateException e) {
            throw new ValidacaoException("wallet.error.hasTransactions", String.valueOf(id));
        }
    }

    /**
     * Busca uma carteira específica pelo seu identificador.
     *
     * @param id Identificador da carteira.
     * @return A entidade {@link Carteira} encontrada.
     * @throws ValidacaoException se o ID for inválido ou não estiver cadastrado.
     */
    public Carteira buscarPorId(int id) {
        validarId(id);
        return buscarOuFalhar(id);
    }

    /**
     * Retorna todas as carteiras cadastradas no repositório.
     *
     * @return Lista contendo todas as carteiras.
     */
    public List<Carteira> listarTodas() {
        return dao.consultarTodos();
    }

    private Carteira buscarOuFalhar(int id) {
        return dao.consultar(id).orElseThrow(() ->
                new ValidacaoException("wallet.error.notFound", String.valueOf(id)));
    }

    private static void validarId(int id) {
        if (id <= 0) {
            throw new ValidacaoException("wallet.error.invalidId", String.valueOf(id));
        }
    }

    private static void validarTexto(String valor, String chaveErro) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException(chaveErro);
        }
    }
}