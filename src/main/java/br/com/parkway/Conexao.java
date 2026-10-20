package br.com.parkway;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexao {

    // Informações do banco de dados do ParkWay
    private static final String URL =
            "jdbc:mysql://localhost:3306/parkway";

    private static final String USUARIO =
            "root";

    private static final String SENHA =
            "";

    // Aqui a gente faz a conexão do Java com o MySQL
    public static Connection conectar() {

        try {

            Connection conexao = DriverManager.getConnection(
                    URL,
                    USUARIO,
                    SENHA
            );

            System.out.println("Conectou no banco!");

            return conexao;

        } catch (Exception erro) {

            System.out.println(
                    "Erro ao conectar ao banco:"
            );

            System.out.println(
                    erro.getMessage()
            );

            return null;
        }
    }
}