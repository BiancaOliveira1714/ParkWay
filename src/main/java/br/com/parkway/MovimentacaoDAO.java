package br.com.parkway;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimentacaoDAO {

    public boolean registrarEntrada(int clienteId, int veiculoId,
                                   int estacionamentoId, int vagaId) {

        String sql = """
            INSERT INTO movimentacoes
            (cliente_id, veiculo_id, estacionamento_id, vaga_id, entrada)
            VALUES (?, ?, ?, ?, NOW())
        """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clienteId);
            stmt.setInt(2, veiculoId);
            stmt.setInt(3, estacionamentoId);
            stmt.setInt(4, vagaId);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean registrarSaida(int movimentacaoId) {

        String sql = """
            UPDATE movimentacoes
            SET saida = NOW()
            WHERE id = ? AND saida IS NULL
        """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movimentacaoId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Movimentacao> historicoCliente(int clienteId) {

        List<Movimentacao> historico = new ArrayList<>();

        String sql = """
            SELECT m.*, v.placa, v.modelo, c.nome
            FROM movimentacoes m
            JOIN veiculos v ON m.veiculo_id = v.id
            JOIN clientes c ON m.cliente_id = c.id
            WHERE m.cliente_id = ?
            ORDER BY m.entrada DESC
        """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Movimentacao m = new Movimentacao();

                m.setId(rs.getInt("id"));
                m.setClienteId(rs.getInt("cliente_id"));
                m.setVeiculoId(rs.getInt("veiculo_id"));
                m.setEstacionamentoId(rs.getInt("estacionamento_id"));
                m.setVagaId(rs.getInt("vaga_id"));
                m.setEntrada(rs.getTimestamp("entrada"));
                m.setSaida(rs.getTimestamp("saida"));

                historico.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historico;
    }

    public List<Movimentacao> listarTodas() {

        List<Movimentacao> movimentacoes = new ArrayList<>();

        String sql = """
            SELECT * FROM movimentacoes
            ORDER BY entrada DESC
        """;

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Movimentacao m = new Movimentacao();

                m.setId(rs.getInt("id"));
                m.setClienteId(rs.getInt("cliente_id"));
                m.setVeiculoId(rs.getInt("veiculo_id"));
                m.setEstacionamentoId(rs.getInt("estacionamento_id"));
                m.setVagaId(rs.getInt("vaga_id"));
                m.setEntrada(rs.getTimestamp("entrada"));
                m.setSaida(rs.getTimestamp("saida"));

                movimentacoes.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return movimentacoes;
    }
}