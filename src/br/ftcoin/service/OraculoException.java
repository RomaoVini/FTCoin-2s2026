package br.ftcoin.service;

/**
 * Falha de domínio ao obter ou interpretar uma cotação.
 */
public class OraculoException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   public OraculoException(String message) {
      super(message);
   }

   public OraculoException(String message, Throwable cause) {
      super(message, cause);
   }
}
