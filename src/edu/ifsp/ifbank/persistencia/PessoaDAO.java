package edu.ifsp.ifbank.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.ifsp.ifbank.ConnectionProvider;
import edu.ifsp.ifbank.modelo.Pessoa;

public class PessoaDAO {
	
	public Pessoa findByConta(int numeroConta) throws PersistenceException {
		Pessoa pessoa = null;
		
		try (Connection conn = ConnectionProvider.getConnection()) {
		
			try (PreparedStatement ps = conn.prepareStatement(
					"SELECT p.id, p.nome "
					+ "FROM pessoa p INNER JOIN conta c ON c.titular = p.id "
					+ "WHERE c.numero = ?;")) {
				
				ps.setInt(1, numeroConta);
				
				try (ResultSet rs = ps.executeQuery();) {
					if (rs.next()) {
						pessoa = new Pessoa();

						pessoa.setId(rs.getInt("id"));
						pessoa.setNome(rs.getString("nome"));
					}
				}
			}

		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
		
		return pessoa;
	}
	
}
