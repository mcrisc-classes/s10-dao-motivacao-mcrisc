package ifbank.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

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

import edu.ifsp.ifbank.ConnectionProvider;



public class TransferApp {
	private static Logger logger = Logger.getLogger("edu.ifsp.ifbank");
	
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
	
	private Connection conn;


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
				try (
						PreparedStatement saque = conn
								.prepareStatement("UPDATE conta SET saldo = (saldo - ?) WHERE numero = ?;");
						PreparedStatement deposito = conn
								.prepareStatement("UPDATE conta SET saldo = (saldo + ?) WHERE numero = ?;");
						PreparedStatement movimentacao = conn
								.prepareStatement("INSERT INTO movimentacao (origem, destino, valor) VALUES (?, ?, ?);");) {
					
				
					saque.setDouble(1, valor.doubleValue());
					saque.setInt(2, contaOrigem.intValue());
					saque.executeUpdate();
					
					deposito.setDouble(1, valor.doubleValue());
					deposito.setInt(2, contaDestino.intValue());
					deposito.executeUpdate();
					
					movimentacao.setInt(1, contaOrigem.intValue());
					movimentacao.setInt(2, contaDestino.intValue());
					movimentacao.setDouble(3, valor.doubleValue());
					movimentacao.executeUpdate();				
					
					conn.commit();

				} catch(SQLException e) {
					conn.rollback();
					throw e;
				}
				
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
			protected String doInBackground() throws SQLException {
				String nome = null;

				try (PreparedStatement ps = conn.prepareStatement(
						"select p.nome from pessoa p inner join conta c on c.titular = p.id where c.numero = ?;")) {
					ps.setInt(1, numeroConta);
					
					try (ResultSet rs = ps.executeQuery();) {
						if (rs.next()) {
							nome = rs.getString("nome");
						}
					}
				}

				return nome;
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
			releaseConnection();
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


	private void connectDatabase() throws SQLException {
		conn = ConnectionProvider.getConnection();
		conn.setAutoCommit(false);
		logger.info("Database: connected");		
	}

	private void releaseConnection() {
		if (conn != null) {
			try {
				conn.close();
				logger.info("Database connection released.");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		TransferApp app = new TransferApp();
		try {
			app.connectDatabase();
			app.createAndShowGUI();			
		} catch (SQLException e) {			
			e.printStackTrace();
		}
		
	}
}
