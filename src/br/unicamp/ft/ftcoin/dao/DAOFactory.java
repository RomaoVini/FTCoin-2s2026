package br.unicamp.ft.ftcoin.dao;

import br.unicamp.ft.ftcoin.dao.mariadb.MariaDBDAOFactory;
import br.unicamp.ft.ftcoin.dao.memory.MemoriaDAOFactory;

/**
 * Padrão Abstract Factory para criação das famílias de DAOs (Memória ou MariaDB).
 */
public abstract class DAOFactory {

    public abstract CarteiraDAO getCarteiraDAO();
    public abstract MovimentacaoDAO getMovimentacaoDAO();
    public abstract OraculoDAO getOraculoDAO();

    /**
     * Método Factory que instancia a fábrica concreta correta baseada no tipo de persistência.
     * 
     * @param tipoPersistencia "MEMORIA" ou "MARIADB"
     * @return Instância da fábrica correspondente
     */
    public static DAOFactory getFactory(String tipoPersistencia) {
        if ("MARIADB".equalsIgnoreCase(tipoPersistencia)) {
            return new MariaDBDAOFactory();
        }
        return new MemoriaDAOFactory(); // Padrão: Memória
    }
}