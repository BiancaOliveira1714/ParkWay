
package br.com.parkway;

import java.sql.*;
import java.util.*;

public class EstacionamentoDAO {

    public List<Map<String, Object>> listar() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();

        String sql = """
            SELECT e.id, e.nome, e.capacidade,
                   COUNT(v.id) AS total,
                   SUM(CASE WHEN v.status = 'LIVRE' THEN 1 ELSE 0 END) AS livres,
                   SUM(CASE WHEN v.status = 'OCUPADA' THEN 1 ELSE 0 END) AS ocupadas
            FROM estacionamentos e
            LEFT JOIN vagas v ON v.estacionamento_id = e.id
            GROUP BY e.id, e.nome, e.capacidade
            ORDER BY e.id
        """;

        try (Connection con = Conexao.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", rs.getInt("id"));
                item.put("nome", rs.getString("nome"));
                item.put("capacidade", rs.getInt("capacidade"));
                item.put("total", rs.getInt("total"));
                item.put("livres", rs.getInt("livres"));
                item.put("ocupadas", rs.getInt("ocupadas"));
                lista.add(item);
            }
        }

        return lista;
    }
}

