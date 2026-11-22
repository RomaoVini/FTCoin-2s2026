package br.ftcoin.model;

import br.ftcoin.util.I18n;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Objeto de valor imutável que representa a cotação da moeda virtual
 * <b>FT_Coin</b> em uma data específica.
 *
 * <p>Garante em sua construção que a data de referência não seja nula e que
 * o valor unitário seja estritamente positivo.</p>
 *
 * @author Equipe FT_Coin
 * @version 2.0
 */
public final class Cotacao {

    private final LocalDate data;
    private final BigDecimal valor;

    /**
     * Cria uma cotação válida e imutável.
     *
     * @param data  Data de referência da cotação (não pode ser nula).
     * @param valor Preço unitário da moeda (deve ser estritamente positivo).
     * @throws IllegalArgumentException se a data ou o valor forem nulos, ou se o valor não for positivo.
     */
    public Cotacao(LocalDate data, BigDecimal valor) {
        if (data == null || valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException(I18n.get("oracle.error.invalidQuotesList"));
        }
        this.data = data;
        this.valor = valor;
    }

    /**
     * Retorna a data de referência à qual a cotação se refere.
     *
     * @return Data da cotação.
     */
    public LocalDate getData() {
        return data;
    }

    /**
     * Retorna o preço unitário da moeda na data de referência.
     *
     * @return Valor da cotação.
     */
    public BigDecimal getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cotacao outra)) {
            return false;
        }
        return Objects.equals(data, outra.data)
                && valor.compareTo(outra.valor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, valor.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "Cotacao{data=" + data + ", valor=" + valor.toPlainString() + "}";
    }
}