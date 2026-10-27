
package br.unicamp.ft.ftcoin.view;

import br.unicamp.ft.ftcoin.controller.MovimentacaoController;
import br.unicamp.ft.ftcoin.model.Movimentacao;
import br.unicamp.ft.ftcoin.enums.TipoOperacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class MovimentacaoView {

    private final MovimentacaoController controller;
    private final Scanner scanner;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MovimentacaoView(MovimentacaoController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenu() {
        int opcao;
        do {
            System.out.println("\n--- GERENCIAR MOVIMENTAÇÕES ---");
            System.out.println("1. Inserir Nova Movimentação");
            System.out.println("2. Listar Movimentações por Carteira");
            System.out.println("3. Excluir Movimentação");
            System.out.println("0. Voltar / Sair");
            System.out.print("Escolha uma opção: ");
            
            opcao = scanner.nextInt();
            scanner.nextLine(); // Limpa o buffer

            try {
                switch (opcao) {
                    case 1 -> inserir();
                    case 2 -> listarPorCarteira();
                    case 3 -> excluir();
                    case 0 -> System.out.println("Saindo...");
                    default -> System.out.println("Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }

        } while (opcao != 0);
    }

    private void inserir() {
        System.out.print("ID da Carteira: ");
        int idCarteira = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Data da Operação (dd/MM/yyyy): ");
        String dataStr = scanner.nextLine();
        LocalDate data = LocalDate.parse(dataStr, formatter);

        System.out.print("Tipo (C para Compra, V para Venda): ");
        char tipoChar = scanner.next().toUpperCase().charAt(0);
        TipoOperacao tipo = TipoOperacao.fromCodigo(tipoChar);

        System.out.print("Quantidade: ");
        BigDecimal quantidade = scanner.next();

        controller.cadastrarMovimentacao(idCarteira, data, tipo, quantidade);
        System.out.println("Movimentação cadastrada com sucesso!");
    }

    private void listarPorCarteira() {
        System.out.print("ID da Carteira: ");
        int idCarteira = scanner.nextInt();

        List<Movimentacao> lista = controller.listarPorCarteira(idCarteira);
        if (lista.isEmpty()) {
            System.out.println("Nenhuma movimentação encontrada para esta carteira.");
        } else {
            lista.forEach(System.out::println);
        }
    }

    private void excluir() {
        System.out.print("ID da Movimentação a excluir: ");
        int id = scanner.nextInt();

        controller.excluirMovimentacao(id);
        System.out.println("Movimentação excluída com sucesso!");
    }
}
