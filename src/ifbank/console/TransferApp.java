package ifbank.console;

import java.util.Locale;
import java.util.Scanner;

public class TransferApp {
	private static final Locale BRAZIL = new Locale("pt", "br");
	
	private void execute(String[] args) {
		int contaOrigem = 0;
		int contaDestino = 0;
		double valor = 0;
		char keepRunning = 'S';
		
		
		/* TODO: abrir uma conexão com o banco de dados 
		 * A conexão deve ficar aberta enquanto o programa estiver sendo executado.
		 */		
		
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
			
			
			System.out.println();
			System.out.println("== Transferindo ==");
			System.out.printf("%s -> %s\n", titularOrigem, titularDestino);
			System.out.println("Valor: " + String.format(BRAZIL, "%.2f", valor));
			
			/* TODO: realizar transferência
			 * Execute aqui o procedimento para realizar a transação que representa a transferência bancária
			 */

			
			System.out.print("Continuar [S/N]? ");
			keepRunning = sc.next().toUpperCase().charAt(0);
		}
		
		sc.close();
		
		/* TODO: feche a conexão com o banco de dados
		 */
		
		System.out.println("-- fim --");
	}
	
	public static void main(String[] args) {
		new TransferApp().execute(args);
	}
	
}
