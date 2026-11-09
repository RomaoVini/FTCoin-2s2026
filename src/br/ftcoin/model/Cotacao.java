package br.ftcoin.model;

import java.time.LocalDate;
import java.math.BigDecimal;

public final class Cotacao {
   private final LocalDate data;
   private final BigDecimal valor;

   public Cotacao(LocalDate data, BigDecimal valor) {
      if (data == null || valor == null || valor.signum() <= 0)
         throw new IllegalArgumentException("Cotação inválida");
      this.data = data;
      this.valor = valor;
   }

   public LocalDate getData() {
    return data;
   }

   public BigDecimal getValor() {
    return valor;
   }
}