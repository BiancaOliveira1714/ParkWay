package br.com.parkway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class VagaDAO {

    // Aqui a gente busca todas as vagas do estacionamento
    public List<Vaga> listarVagas() {

        List<Vaga> vagas = new ArrayList<>();

        String sql =
                "SELECT id, numero, status " +
                "FROM vagas " +
                "WHERE estacionamento_id = 1 " +
                "ORDER BY numero";

        try (
            Connection conexao = Conexao.conectar();

            PreparedStatement comando =
                    conexao.prepareStatement(sql);

            ResultSet resultado =
                    comando.executeQuery()
        ) {

            // Passa por todas as vagas que vieram do banco
            while (resultado.next()) {

                int id =
                        resultado.getInt("id");

                int numero =
                        resultado.getInt("numero");

                String status =
                        resultado.getString("status");

                Vaga vaga =
                        new Vaga(
                                id,
                                numero,
                                status
                        );

                vagas.add(vaga);
            }

        } catch (Exception erro) {

            System.out.println(
                    "Erro ao buscar as vagas:"
            );

            System.out.println(
                    erro.getMessage()
            );
        }

        return vagas;
    }


    // Aqui a gente ocupa uma vaga
    public void ocuparVaga(int id) {

        String sql =
                "UPDATE vagas " +
                "SET status = 'OCUPADA' " +
                "WHERE id = ?";

        try (
            Connection conexao =
                    Conexao.conectar();

            PreparedStatement comando =
                    conexao.prepareStatement(sql)
        ) {

            comando.setInt(1, id);

            comando.executeUpdate();

            System.out.println(
                    "Vaga ocupada com sucesso!"
            );

        } catch (Exception erro) {

            System.out.println(
                    "Erro ao ocupar a vaga:"
            );

            System.out.println(
                    erro.getMessage()
            );
        }
    }


    // Aqui a gente libera uma vaga
    public void liberarVaga(int id) {

        String sql =
                "UPDATE vagas " +
                "SET status = 'DISPONIVEL' " +
                "WHERE id = ?";

        try (
            Connection conexao =
                    Conexao.conectar();

            PreparedStatement comando =
                    conexao.prepareStatement(sql)
        ) {

            comando.setInt(1, id);

            comando.executeUpdate();

            System.out.println(
                    "Vaga liberada com sucesso!"
            );

        } catch (Exception erro) {

            System.out.println(
                    "Erro ao liberar a vaga:"
            );

            System.out.println(
                    erro.getMessage()
            );
        }
    }
}