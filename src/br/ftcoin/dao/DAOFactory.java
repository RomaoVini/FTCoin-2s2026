package br.ftcoin.dao;

/**
 * Classe abstrata que aplica o padrão de projeto <b>Abstract Factory</b>
 * para criar famílias de objetos de acesso a dados (DAOs).
 *
 * <p>Permite que a aplicação alterne entre a persistência
 * em memória local ({@link MemoriaDAOFactory}) e a persistência relacional
 * no banco MariaDB ({@link MariaDBDAOFactory}) sem acoplar os controladores
 * às implementações concretas.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public abstract class DAOFactory {

    /** Constante identificadora do modo de persistência em memória RAM. */
    public static final String TIPO_MEMORIA = "MEMORIA";

    /** Constante identificadora do modo de persistência em banco de dados MariaDB. */
    public static final String TIPO_MARIADB = "MARIADB";

    /**
     * Cria e retorna a implementação concreta de {@link CarteiraDAO}
     * correspondente à fábrica ativa.
     *
     * @return implementação ativa de {@link CarteiraDAO}
     */
    public abstract CarteiraDAO getCarteiraDAO();

    /**
     * Cria e retorna a implementação concreta de {@link MovimentacaoDAO}
     * correspondente à fábrica ativa.
     *
     * @return implementação ativa de {@link MovimentacaoDAO}
     */
    public abstract MovimentacaoDAO getMovimentacaoDAO();

    /**
     * Instancia a fábrica concreta adequada ao modo de persistência informado.
     *
     * <p>Caso o parâmetro seja nulo, vazio ou diferente de {@code "MARIADB"},
     * a fábrica de memória ({@link MemoriaDAOFactory}) será adotada como padrão.</p>
     *
     * @param tipoPersistencia identificador do modo de armazenamento
     *                         ({@code "MEMORIA"} ou {@code "MARIADB"})
     * @return instância concreta de {@link DAOFactory}
     */
    public static DAOFactory getFactory(String tipoPersistencia) {
        if (TIPO_MARIADB.equalsIgnoreCase(tipoPersistencia != null ? tipoPersistencia.trim() : "")) {
            return new MariaDBDAOFactory();
        }
        return new MemoriaDAOFactory();
    }
}