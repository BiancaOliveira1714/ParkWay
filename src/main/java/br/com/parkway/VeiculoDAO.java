
package br.com.parkway;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VeiculoDAO {

    private final Connection conexao;

    public VeiculoDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public int cadastrar(Veiculo veiculo) throws SQLException {

        String sql = """
            INSERT INTO veiculos(cliente_id, placa, modelo, cor)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conexao.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, veiculo.getClienteId());
            ps.setString(2, veiculo.getPlaca().trim().toUpperCase());
            ps.setString(3, veiculo.getModelo);
            ps.setString(4, veiculo.getCor);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    public List<Veiculo> listarPorCliente(int clienteId)
            throws SQLException {

        List<Veiculo> veiculos = new ArrayList<>();

        String sql = """
            SELECT id, cliente_id, placa, modelo, cor
            FROM veiculos
            WHERE cliente_id = ?
            ORDER BY placa
            """;

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, clienteId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Veiculo veiculo = new Veiculo(
                        rs.getInt("id"),
                        rs.getInt("cliente_id"),
                        rs.getString("placa"),
                        rs.getString("modelo"),
                        rs.getString("cor")
                    );

                    veiculos.add(veiculo);
                }
            }
        }

        return veiculos;
    }
}
