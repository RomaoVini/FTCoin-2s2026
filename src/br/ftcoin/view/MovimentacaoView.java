package br.ftcoin.view;

import br.ftcoin.controller.MovimentacaoController;
import br.ftcoin.enums.TipoOperacao;
import br.ftcoin.model.Movimentacao;
import br.ftcoin.exception.ValidacaoException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class MovimentacaoView {

    private final MovimentacaoController controller;
    private final Scanner sc = new Scanner(System.in);

    public MovimentacaoView() {
        this(new MovimentacaoController());
    }

    public MovimentacaoView(MovimentacaoController controller) {
        this.controller = controller;
    }

    public void exibirMenu() {
        int opcao;
        do {
            System.out.println("\n===== MOVIMENTAÇÕES =====");
            System.out.println("1 - Cadastrar");
            System.out.println("2 - Listar todas");
            System.out.println("3 - Listar por carteira");
            System.out.println("4 - Buscar por ID");
            System.out.println("5 - Atualizar");
            System.out.println("6 - Excluir");
            System.out.println("0 - Voltar");
            opcao = lerInt("Opção: ");

            try {
                switch (opcao) {
                    case 1 -> cadastrar();
                    case 2 -> imprimir(controller.listarTodas());
                    case 3 -> imprimir(controller.listarPorCarteira(lerInt("ID da carteira: ")));
                    case 4 -> System.out.println(controller.buscarPorId(lerInt("ID da movimentação: ")));
                    case 5 -> atualizar();
                    case 6 -> excluir();
                    case 0 -> System.out.println("Voltando...");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (ValidacaoException e) {
                System.out.println("Erro de validação: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private void cadastrar() {
        int idCarteira = lerInt("ID da carteira: ");
        LocalDate data = lerData("Data (aaaa-mm-dd, vazio = hoje): ");
        TipoOperacao tipo = lerTipo();
        BigDecimal quantidade = lerDecimal("Quantidade: ");

        Movimentacao m = controller.cadastrar(idCarteira, data, tipo, quantidade);
        System.out.println("Movimentação cadastrada: " + m);
    }

    private void atualizar() {
        int id = lerInt("ID da movimentação a atualizar: ");
        Movimentacao atual = controller.buscarPorId(id);
        System.out.println("Atual: " + atual);

        int idCarteira = lerInt("Novo ID da carteira: ");
        LocalDate data = lerData("Nova data (aaaa-mm-dd, vazio = hoje): ");
        TipoOperacao tipo = lerTipo();
        BigDecimal quantidade = lerDecimal("Nova quantidade: ");

        Movimentacao m = controller.atualizar(id, idCarteira, data, tipo, quantidade);
        System.out.println("Movimentação atualizada: " + m);
    }

    private void excluir() {
        int id = lerInt("ID da movimentação a excluir: ");
        System.out.println("Registro: " + controller.buscarPorId(id));
        System.out.print("Confirma exclusão? (s/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("s")) {
            controller.excluir(id);
            System.out.println("Movimentação excluída.");
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    private void imprimir(List<Movimentacao> lista) {
        if (lista.isEmpty()) {
            System.out.println("Nenhuma movimentação encontrada.");
            return;
        }
        lista.forEach(System.out::println);
    }

    // ---------- Leitura de entrada ----------

    private int lerInt(String msg) {
        while (true) {
            System.out.print(msg);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    private BigDecimal lerDecimal(String msg) {
        while (true) {
            System.out.print(msg);
            try {
                return new BigDecimal(sc.nextLine().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Digite um valor decimal válido.");
            }
        }
    }

    private LocalDate lerData(String msg) {
        while (true) {
            System.out.print(msg);
            String txt = sc.nextLine().trim();
            if (txt.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(txt);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato aaaa-mm-dd.");
            }
        }
    }

    private TipoOperacao lerTipo() {
        while (true) {
            System.out.print("Tipo (C = compra, V = venda): ");
            String txt = sc.nextLine().trim().toUpperCase();
            if (txt.length() == 1) {
                try {
                    return TipoOperacao.fromCodigo(txt.charAt(0));
                } catch (RuntimeException e) {
                    // cai na mensagem abaixo
                }
            }
            System.out.println("Tipo inválido.");
        }
    }
}