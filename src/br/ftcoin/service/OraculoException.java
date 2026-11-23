package br.ftcoin.service;

/**
 * Falha de domínio ao obter ou interpretar uma cotação.
 */
public class OraculoException extends RuntimeException {
   /** Identificador de versão para serialização. */
   private static final long serialVersionUID = 1L;

   /**
    * Cria uma exceção para uma falha ao consultar ou interpretar uma cotação.
    *
    * @param message descrição da falha
    */
   public OraculoException(String message) {
      super(message);
   }

   /**
    * Cria uma exceção para uma falha ao consultar ou interpretar uma cotação.
    *
    * @param message descrição da falha
    * @param cause causa original da falha
    */
   public OraculoException(String message, Throwable cause) {
      super(message, cause);
   }
}
