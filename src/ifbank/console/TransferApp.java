package ifbank.console;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Scanner;

import edu.ifsp.ifbank.modelo.Pessoa;
import edu.ifsp.ifbank.persistencia.PersistenceException;
import edu.ifsp.ifbank.persistencia.PessoaDAO;
import edu.ifsp.ifbank.persistencia.TransferenciaDAO;

public class TransferApp {
	private static final Locale BRAZIL = new Locale("pt", "br");
	
	private void execute(String[] args) {
		int contaOrigem = 0;
		int contaDestino = 0;
		double valor = 0;
		char keepRunning = 'S';
		
		
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
			
			try {
				PessoaDAO pessoaDao = new PessoaDAO();
				Pessoa p = pessoaDao.findByConta(contaOrigem);
				String titularOrigem = p.getNome();
	
				p = pessoaDao.findByConta(contaDestino);
				String titularDestino = p.getNome();
				
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
				
				TransferenciaDAO dao = new TransferenciaDAO();
				dao.transferir(contaOrigem, contaDestino, valor);
				
			} catch (PersistenceException e) {
				e.printStackTrace();
			}
			
			System.out.print("Continuar [S/N]? ");
			keepRunning = sc.next().toUpperCase().charAt(0);
		}
		
		sc.close();
			
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
