
package br.ftcoin.model;

/**
 * Representa uma carteira de criptomoedas do sistema FT Coin.
 *
 * Cada carteira possui um identificador, um titular e uma corretora.
 */
public class Carteira {

    private int id;
    private String titular;
    private String corretora;

    /**
     * Cria uma carteira com identificador, titular e corretora.
     *
     * @param id identificador da carteira
     * @param titular nome do titular da carteira
     * @param corretora nome da corretora utilizada
     */
    public Carteira(int id, String titular, String corretora) {
        setId(id);
        setTitular(titular);
        setCorretora(corretora);
    }

    /**
     * Retorna o identificador da carteira.
     *
     * @return identificador da carteira
     */
    public int getId() {
        return id;
    }

    /**
     * Define o identificador da carteira.
     *
     * @param id identificador da carteira
     */
    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "O identificador deve ser positivo."
            );
        }

        this.id = id;
    }

    /**
     * Retorna o titular da carteira.
     *
     * @return nome do titular
     */
    public String getTitular() {
        return titular;
    }

    /**
     * Define o titular da carteira.
     *
     * @param titular nome do titular
     */
    public void setTitular(String titular) {
        if (titular == null || titular.isBlank()) {
            throw new IllegalArgumentException(
                "O nome do titular é obrigatório."
            );
        }

        this.titular = titular;
    }

    /**
     * Retorna a corretora da carteira.
     *
     * @return nome da corretora
     */
    public String getCorretora() {
        return corretora;
    }

    /**
     * Define a corretora da carteira.
     *
     * @param corretora nome da corretora
     */
    public void setCorretora(String corretora) {
        if (corretora == null || corretora.isBlank()) {
            throw new IllegalArgumentException(
                "O nome da corretora é obrigatório."
            );
        }

        this.corretora = corretora;
    }
}
