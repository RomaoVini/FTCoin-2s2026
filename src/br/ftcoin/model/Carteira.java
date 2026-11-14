package br.ftcoin.model;

import br.ftcoin.exception.ValidacaoException;

import java.util.Objects;

/**
 * Entidade de domínio que representa uma carteira de moedas virtuais no sistema FT_Coin.
 * Garante a integridade de seus atributos desde a instanciação através de validações
 * internacionalizadas ({@link ValidacaoException}).
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public class Carteira {

    private final int id;
    private String titular;
    private String corretora;

    /**
     * Constrói uma nova instância válida de {@link Carteira}.
     *
     * @param id        Identificador numérico único da carteira (deve ser maior que zero).
     * @param titular   Nome completo do proprietário da carteira.
     * @param corretora Nome da corretora custodiante.
     * @throws ValidacaoException se o {@code id} for menor ou igual a zero, ou se {@code titular}/{@code corretora} forem nulos ou vazios.
     */
    public Carteira(int id, String titular, String corretora) {
        if (id <= 0) {
            throw new ValidacaoException("wallet.error.invalidId", String.valueOf(id));
        }
        this.id = id;
        setTitular(titular);
        setCorretora(corretora);
    }

    /**
     * Retorna o identificador único da carteira.
     *
     * @return Identificador numérico da carteira.
     */
    public int getId() {
        return id;
    }

    /**
     * Retorna o nome do titular da carteira.
     *
     * @return Nome do titular.
     */
    public String getTitular() {
        return titular;
    }

    /**
     * Define o nome do titular da carteira após validar e remover espaços excedentes.
     *
     * @param titular Nome do titular.
     * @throws ValidacaoException se o {@code titular} for nulo ou composto apenas por espaços em branco.
     */
    public void setTitular(String titular) {
        if (titular == null || titular.isBlank()) {
            throw new ValidacaoException("wallet.error.holderRequired");
        }
        this.titular = titular.trim();
    }

    /**
     * Retorna o nome da corretora vinculada à carteira.
     *
     * @return Nome da corretora.
     */
    public String getCorretora() {
        return corretora;
    }

    /**
     * Define o nome da corretora após validar e remover espaços excedentes.
     *
     * @param corretora Nome da corretora.
     * @throws ValidacaoException se a {@code corretora} for nula ou composta apenas por espaços em branco.
     */
    public void setCorretora(String corretora) {
        if (corretora == null || corretora.isBlank()) {
            throw new ValidacaoException("wallet.error.brokerRequired");
        }
        this.corretora = corretora.trim();
    }

    /**
     * Compara duas carteiras com base exclusivamente em seu identificador único ({@code id}).
     *
     * @param o Objeto a ser comparado.
     * @return {@code true} se ambos representarem a carteira com o mesmo {@code id}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carteira carteira)) return false;
        return id == carteira.id;
    }

    /**
     * Gera o código hash baseado no identificador único da carteira.
     *
     * @return Código hash do {@code id}.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Retorna uma representação textual estruturada da carteira.
     *
     * @return String formatada com ID, titular e corretora.
     */
    @Override
    public String toString() {
        return String.format("Carteira{id=%d, titular='%s', corretora='%s'}", id, titular, corretora);
    }
}