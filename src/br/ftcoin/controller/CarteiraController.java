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
 * Valida os dados das operações e lança {@link ValidacaoException} com mensagens
 * internacionalizadas quando uma regra de negócio não é atendida.
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class CarteiraController {

    /** Repositório de acesso a dados das carteiras. */
    private final CarteiraDAO dao;

    /** View do submenu de carteiras; pode ser nula quando o controlador é usado sem a CLI. */
    private CarteiraView view;

    /**
     * Constrói o controlador obtendo o DAO de uma fábrica.
     *
     * @param factory fábrica concreta de DAOs (memória ou MariaDB)
     * @throws NullPointerException se {@code factory} for nulo.
     */
    public CarteiraController(DAOFactory factory) {
        this(Objects.requireNonNull(factory, "DAOFactory não pode ser nula.").getCarteiraDAO());
    }

    /**
     * Constrói o controlador injetando diretamente uma implementação de {@link CarteiraDAO}.
     * Pode ser usado independentemente da interface CLI.
     *
     * @param dao implementação de acesso a dados de carteiras
     * @throws NullPointerException se {@code dao} for nulo
     */
    public CarteiraController(CarteiraDAO dao) {
        this.dao = Objects.requireNonNull(dao, "CarteiraDAO não pode ser nulo.");
    }

    /**
     * Constrói o controlador completo para execução na interface de linha de comando (CLI),
     * utilizado pelo {@link AppController}.
     *
     * @param menuView view genérica de menus da aplicação
     * @param dao implementação de acesso a dados de carteiras
     * @throws NullPointerException se {@code menuView} ou {@code dao} forem nulos
     */
    public CarteiraController(MenuView menuView, CarteiraDAO dao) {
        this(dao);
        Objects.requireNonNull(menuView, "MenuView não pode ser nula.");
        this.view = new CarteiraView(menuView.getConsoleUtils(), this);
    }

    /**
     * Solicita à view que exiba o submenu de carteiras e processe as opções da CLI.
     *
     * @throws IllegalStateException se o controlador tiver sido criado sem uma
     *                               {@link CarteiraView}
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
     * @param id identificador numérico único da carteira (deve ser maior que zero)
     * @param titular nome do proprietário da carteira
     * @param corretora nome da corretora custodiante
     * @return entidade {@link Carteira} recém-cadastrada
     * @throws ValidacaoException se algum campo for inválido ou o ID já estiver em uso
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
     * @param id identificador da carteira a ser alterada
     * @param titular novo nome do titular
     * @param corretora novo nome da corretora
     * @return entidade {@link Carteira} atualizada
     * @throws ValidacaoException se os dados forem inválidos ou a carteira não existir
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
     * @param id identificador da carteira a ser excluída
     * @throws ValidacaoException se o ID for inválido, não existir ou possuir movimentações vinculadas
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
     * @param id identificador da carteira
     * @return entidade {@link Carteira} encontrada
     * @throws ValidacaoException se o ID for inválido ou não estiver cadastrado
     */
    public Carteira buscarPorId(int id) {
        validarId(id);
        return buscarOuFalhar(id);
    }

    /**
     * Retorna todas as carteiras cadastradas no repositório.
     *
     * @return lista contendo todas as carteiras cadastradas
     */
    public List<Carteira> listarTodas() {
        return dao.consultarTodos();
    }

    /**
     * Consulta uma carteira no DAO e falha caso ela não exista.
     *
     * @param id identificador da carteira
     * @return carteira encontrada
     * @throws ValidacaoException se não houver carteira com o identificador informado
     */
    private Carteira buscarOuFalhar(int id) {
        return dao.consultar(id).orElseThrow(() ->
                new ValidacaoException("wallet.error.notFound", String.valueOf(id)));
    }

    /**
     * Garante que o identificador da carteira seja estritamente positivo.
     *
     * @param id identificador a ser validado
     * @throws ValidacaoException se {@code id} for menor ou igual a zero
     */
    private static void validarId(int id) {
        if (id <= 0) {
            throw new ValidacaoException("wallet.error.invalidId", String.valueOf(id));
        }
    }

    /**
     * Garante que um campo textual obrigatório não seja nulo nem esteja em branco.
     *
     * @param valor texto a ser validado
     * @param chaveErro chave de internacionalização da mensagem de erro
     * @throws ValidacaoException se {@code valor} for nulo ou em branco
     */
    private static void validarTexto(String valor, String chaveErro) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException(chaveErro);
        }
    }
}