package ifbank.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;

import edu.ifsp.ifbank.modelo.Pessoa;
import edu.ifsp.ifbank.persistencia.PersistenceException;
import edu.ifsp.ifbank.persistencia.PessoaDAO;
import edu.ifsp.ifbank.persistencia.TransferenciaDAO;



public class TransferApp {
	private static final Locale BRAZIL = new Locale("pt", "br");
	private JLabel titularOrigemLabel;
	private JFormattedTextField contaOrigemText;
	private JLabel titularDestinoLabel;
	private JFormattedTextField contaDestinoText;
	private JFormattedTextField valorText;
	private JLabel status;
	private JButton transfer;
	private JButton exit;
	private JFrame frame;


	private void createAndShowGUI() {
		frame = new JFrame("Transferência entre Contas");
		
		frame.getContentPane().setLayout(new BorderLayout());
		JPanel panel = buildPanel();		
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		status = new JLabel(" ");
		status.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
		frame.getContentPane().add(status, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				confirmExit();
			}			
		});		
		
		frame.setVisible(true);
	}

	private void onContaOrigemChanged(PropertyChangeEvent evt) {
		Number numeroConta = (Number)contaOrigemText.getValue();
		System.out.println("origem: " + numeroConta);
		if (numeroConta != null) {
			mostrarTitular(numeroConta.intValue(), titularOrigemLabel);
		}
	}

	private void onContaDestinoChanged(PropertyChangeEvent e) {
		Number numeroConta = (Number)contaDestinoText.getValue();
		System.out.println("destino: " + numeroConta);
		if (numeroConta != null) {
			mostrarTitular(numeroConta.intValue(), titularDestinoLabel);
		}
	}

	private void onTransferClick(ActionEvent e) {

		Number contaOrigem = (Number) contaOrigemText.getValue();
		Number contaDestino = (Number) contaDestinoText.getValue();
		Number valor = (Number) valorText.getValue();		
				
		if (contaOrigem == null || contaDestino == null || valor == null) {
			JOptionPane.showMessageDialog(frame, "Todos os campos devem ser preenchidos.", "Aviso", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		SwingWorker<Void, Void> worker = new SwingWorker<>() {

			@Override
			protected Void doInBackground() throws Exception {

				TransferenciaDAO dao = new TransferenciaDAO();
				dao.transferir(
						contaOrigem.intValue(), 
						contaDestino.intValue(), 
						valor.doubleValue());
				
				return null;
			}

			protected void done() {
				try {
					get();
					transfer.setEnabled(true);
					status.setText("Transferência realizada com sucesso.");
				} catch (InterruptedException | ExecutionException e) {
					JOptionPane.showMessageDialog(frame, e, "Erro", JOptionPane.ERROR_MESSAGE);
				}
			};
		};
		
		transfer.setEnabled(false);
		status.setText("Processando...");
		worker.execute();
	}
	
	private void mostrarTitular(int numeroConta, JLabel targetLabel) {
		
		/* É necessário usar um `SwingWorker` para que a comunicação com 
		 * o banco de dados (possivelmente lenta) ocorra em uma `thread`
		 * separada, evitando travamentos da interface gráfica.
		 */
		SwingWorker<String, Void> worker = new SwingWorker<>() {
			@Override
			protected String doInBackground() throws PersistenceException {
				
				PessoaDAO dao = new PessoaDAO();
				Pessoa pessoa = dao.findByConta(numeroConta);

				return pessoa.getNome();
			}

			@Override
			protected void done() {
				try {
					String nome = get();
					if (nome != null) {
						targetLabel.setText(nome);
					} else {
						targetLabel.setText("<conta não encontrada>");
					}
				} catch (InterruptedException | ExecutionException e) {
					JOptionPane.showMessageDialog(frame, e, "Erro", JOptionPane.ERROR_MESSAGE);
				}
			}

		};

		worker.execute();
	}
	
	private void confirmExit() {
		int answer = JOptionPane.showConfirmDialog(frame,
				"Se você sair do aplicativo, a transação atual será cancelada. Deseja continuar?",
				"Cancelar",
				JOptionPane.YES_NO_OPTION);

		if (answer == JOptionPane.YES_OPTION) {
			frame.setVisible(false);			
			frame.dispose();
		}
	}
	
	private JPanel buildPanel() {
		JPanel panel = new JPanel();
		
		JLabel contaOrigemLabel = new JLabel("Origem:");
		titularOrigemLabel = new JLabel();
		NumberFormat accountFormat = NumberFormat.getIntegerInstance(BRAZIL);
		contaOrigemText = new JFormattedTextField(accountFormat);
		contaOrigemText.setColumns(8);
		contaOrigemText.setHorizontalAlignment(SwingConstants.RIGHT);
		contaOrigemText.addPropertyChangeListener("value", this::onContaOrigemChanged);

		JLabel contaDestinoLabel = new JLabel("Destino:");
		titularDestinoLabel = new JLabel();
		contaDestinoText = new JFormattedTextField(accountFormat);
		contaDestinoText.setColumns(8);
		contaDestinoText.setHorizontalAlignment(SwingConstants.RIGHT);
		contaDestinoText.addPropertyChangeListener("value", this::onContaDestinoChanged);		
		
		JLabel valorLabel = new JLabel("Valor:");
		NumberFormat amountFormat = NumberFormat.getNumberInstance(BRAZIL);
		amountFormat.setMinimumFractionDigits(2);
		valorText = new JFormattedTextField(amountFormat);
		valorText.setColumns(8);
		valorText.setHorizontalAlignment(SwingConstants.RIGHT);
				
		transfer = new JButton("Transferir");
		transfer.addActionListener(this::onTransferClick);
		exit = new JButton("Sair");
		exit.addActionListener((ActionEvent e) -> {confirmExit();});		
		
		
		/* configuração do layout */
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc;
		
		/* conta de origem */
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(10, 5, 0, 10);
		panel.add(contaOrigemLabel, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 5, 0, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(contaOrigemText, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.gridwidth = 10;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 5, 0, 10);
		panel.add(titularOrigemLabel, gbc);
		
		/* conta de destino */
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(10, 5, 0, 10);
		panel.add(contaDestinoLabel, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 5, 0, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(contaDestinoText, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.gridwidth = 10;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 5, 0, 10);
		panel.add(titularDestinoLabel, gbc);


		/* valor */
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(10, 5, 0, 10);
		panel.add(valorLabel, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 5, 0, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(valorText, gbc);

		
		/* botão `transferir` */
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(10, 10, 10, 10);		
		panel.add(transfer, gbc);

		/* botão `cancelar` */
		gbc = new GridBagConstraints();
		gbc.gridx = 6;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(10, 10, 10, 10);		
		panel.add(exit, gbc);
		
		return panel;
	}

	
	public static void main(String[] args) {
			TransferApp app = new TransferApp();
			app.createAndShowGUI();					
	}
}





