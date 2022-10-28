package edu.ifsp.ifbank.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.ifsp.ifbank.ConnectionProvider;

public class TransferenciaDAO {

	public void transferir(
			int contaOrigem, int contaDestino, double valor) throws PersistenceException {

		try (Connection conn = ConnectionProvider.getConnection()) {
			conn.setAutoCommit(false);
			
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
			
			
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
		
	}
}
