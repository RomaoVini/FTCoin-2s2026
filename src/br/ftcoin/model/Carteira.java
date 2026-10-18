
package br.ftcoin.model;

/**
 * Representa uma carteira cadastrada no FT Coin.
 */
public class Carteira {

    private int id;
    private String titular;
    private String corretora;

    public Carteira(int id, String titular, String corretora) {
        setId(id);
        setTitular(titular);
        setCorretora(corretora);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "O identificador deve ser positivo."
            );
        }
        this.id = id;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        if (titular == null || titular.isBlank()) {
            throw new IllegalArgumentException(
                "O nome do titular é obrigatório."
            );
        }
        this.titular = titular;
    }

    public String getCorretora() {
        return corretora;
    }

    public void setCorretora(String corretora) {
        if (corretora == null || corretora.isBlank()) {
            throw new IllegalArgumentException(
                "A corretora é obrigatória."
            );
        }
        this.corretora = corretora;
    }
}

