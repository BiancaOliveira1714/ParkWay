package br.com.parkway;

public class Main {

    public static void main(String[] args) {

        try {

            // Primeiro testa a conexão com o banco
            if (Conexao.conectar() != null) {

                System.out.println(
                        "ParkWay iniciado com sucesso!"
                );


                // Depois inicia o servidor
                Servidor.iniciar();


            } else {

                System.out.println(
                        "Não foi possível conectar ao banco."
                );
            }


        } catch (Exception erro) {

            System.out.println(
                    "Erro ao iniciar o ParkWay:"
            );


            System.out.println(
                    erro.getMessage()
            );
        }
    }
}