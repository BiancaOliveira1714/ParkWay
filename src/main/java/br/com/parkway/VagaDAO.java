
package br.com.parkway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VagaDAO {

    // Lista vagas do estacionamento padrão
    public List<Vaga> listarVagas() {
        return listarPorEstacionamento(1);
    }

    // Lista vagas de um estacionamento específico
    public List<Vaga> listarPorEstacionamento(
            int estacionamentoId) {

        List<Vaga> vagas = new ArrayList<>();

        String sql =
                "SELECT id, numero, status FROM vagas "
                + "WHERE estacionamento_id = ? "
                + "ORDER BY numero";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, estacionamentoId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Vaga vaga = new Vaga(
                            rs.getInt("id"),
                            rs.getInt("numero"),
                            rs.getString("status")
                    );

                    vagas.add(vaga);
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Erro ao listar vagas: " + e.getMessage()
            );
        }

        return vagas;
    }

    // Ocupa uma vaga
    public void ocuparVaga(int id) throws SQLException {

        String sql =
                "UPDATE vagas SET status = 'OCUPADA' "
                + "WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Libera uma vaga
    public void liberarVaga(int id) throws SQLException {

        String sql =
                "UPDATE vagas SET status = 'DISPONIVEL' "
                + "WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
