package br.ftcoin.model;

import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Cotação imutável da moeda virtual em uma data específica.
 */
public final class Cotacao {
   /** Data a que a cotação se refere. */
   private final LocalDate data;

   /** Preço positivo da moeda na data de referência. */
   private final BigDecimal valor;

   /**
    * Cria uma cotação válida.
    *
    * @param data data de referência
    * @param valor preço da moeda, que deve ser positivo
    * @throws IllegalArgumentException se a data ou o valor forem nulos, ou se o valor não for positivo
    */
   public Cotacao(LocalDate data, BigDecimal valor) {
      if (data == null || valor == null || valor.signum() <= 0)
         throw new IllegalArgumentException("Cotação inválida");
      this.data = data;
      this.valor = valor;
   }

   /**
    * Retorna a data a que a cotação se refere.
    *
    * @return data da cotação
    */
   public LocalDate getData() {
    return data;
   }

   /**
    * Retorna o preço da moeda na data de referência.
    *
    * @return valor da cotação
    */
   public BigDecimal getValor() {
    return valor;
   }
}
