package ifbank.console;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Scanner;

import edu.ifsp.ifbank.ConnectionProvider;

public class TransferApp {
	private static final Locale BRAZIL = new Locale("pt", "br");
	
	private void execute(String[] args) {
		int contaOrigem = 0;
		int contaDestino = 0;
		double valor = 0;
		char keepRunning = 'S';
		
		
		try (Connection conn = ConnectionProvider.getConnection()) {
			conn.setAutoCommit(false);

		
			Scanner sc = new Scanner(System.in);
			sc.useLocale(BRAZIL);			
			
			while (keepRunning == 'S') {
				System.out.println("Transferências on-line");
				
				System.out.print("Conta de origem: ");
				contaOrigem = sc.nextInt();
				
				System.out.print("Conta de destino: ");
				contaDestino = sc.nextInt();
	
				System.out.print("Valor (formato: 0,00): ");
				valor = sc.nextDouble();
				
				/* TODO: buscar nome do titular
				 * - Use a variável `contaOrigem` para buscar o nome do titular da conta no banco de dados
				 * - Se a conta não existir, mostre uma mensagem de erro e reinicie o programa (basta 
				 * executar o comando `continue`, para para voltar ao início `while`).
				 */
				String titularOrigem = "titular1";
				
				
				
				/* TODO: buscar nome do titular
				 * - Use a variável `contaDestino` para buscar o nome do titular da conta no banco de dados
				 * - Se a conta não existir, mostre uma mensagem de erro e reinicie o programa.
				 */
				String titularDestino = "titular2";

				try (PreparedStatement ps = conn.prepareStatement(
						"select p.nome from pessoa p inner join conta c on c.titular = p.id where c.numero = ?;")) {

					titularOrigem = consultarCliente(ps, contaOrigem);
					titularDestino = consultarCliente(ps, contaDestino);
				}
				
				if (titularOrigem == null) {
					System.err.println("Conta Origem não encontrada.");
					continue;
				}

				if (titularDestino == null) {
					System.err.println("Conta Destino não encontrada.");
					continue;
				}
				
				
				System.out.println();
				System.out.println("== Transferindo ==");
				System.out.printf("%s -> %s\n", titularOrigem, titularDestino);
				System.out.println("Valor: " + String.format(BRAZIL, "%.2f", valor));
				
				/* TODO: realizar transferência
				 * Execute aqui o procedimento para realizar a transação que representa a transferência bancária
				 */
				try (
						PreparedStatement saque = conn
								.prepareStatement("UPDATE conta SET saldo = (saldo - ?) WHERE numero = ?;");
						PreparedStatement deposito = conn
								.prepareStatement("UPDATE conta SET saldo = (saldo + ?) WHERE numero = ?;");
						PreparedStatement movimentacao = conn
								.prepareStatement("INSERT INTO movimentacao (origem, destino, valor) VALUES (?, ?, ?);");) {
					
				
					saque.setDouble(1, valor);
					saque.setInt(2, contaOrigem);
					saque.executeUpdate();
					
					deposito.setDouble(1, valor);
					deposito.setInt(2, contaDestino);
					deposito.executeUpdate();
					
					movimentacao.setInt(1, contaOrigem);
					movimentacao.setInt(2, contaDestino);
					movimentacao.setDouble(3, valor);
					movimentacao.executeUpdate();				
					
					conn.commit();

				} catch(SQLException e) {
					conn.rollback();
					throw e;
				}

				
				System.out.print("Continuar [S/N]? ");
				keepRunning = sc.next().toUpperCase().charAt(0);
			}
			
			sc.close();
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		/* TODO: feche a conexão com o banco de dados
		 */
		
		System.out.println("-- fim --");
	}

	private String consultarCliente(PreparedStatement ps, int numeroConta) throws SQLException {
		String nome = null;
		
		ps.setInt(1, numeroConta);					
		try (ResultSet rs = ps.executeQuery();) {
			if (rs.next()) {
				nome = rs.getString("nome");
			}
		}
		
		return nome;
	}
	
	public static void main(String[] args) {
		new TransferApp().execute(args);
	}
	
}
