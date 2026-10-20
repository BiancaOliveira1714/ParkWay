package br.com.parkway;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Servidor {

    private static final VagaDAO vagaDAO =
            new VagaDAO();


    // Aqui a gente inicia o servidor do ParkWay
    public static void iniciar()
            throws IOException {

        HttpServer servidor =
                HttpServer.create(
                        new InetSocketAddress(8080),
                        0
                );


        // Quando acessar /api/vagas,
        // mostra as vagas que estão no banco
        servidor.createContext(
                "/api/vagas",
                Servidor::listarVagas
        );


        // Quando acessar /api/ocupar,
        // ocupa uma vaga
        servidor.createContext(
                "/api/ocupar",
                Servidor::ocuparVaga
        );


        // Quando acessar /api/liberar,
        // libera uma vaga
        servidor.createContext(
                "/api/liberar",
                Servidor::liberarVaga
        );


        // Entrega o arquivo CSS
        servidor.createContext(
                "/style.css",
                Servidor::estilo
        );


        // Entrega o arquivo JavaScript
        servidor.createContext(
                "/script.js",
                Servidor::javascript
        );


        // Entrega a página principal
        servidor.createContext(
                "/",
                Servidor::paginaInicial
        );


        servidor.setExecutor(null);

        servidor.start();


        System.out.println(
                "Servidor do ParkWay iniciado!"
        );

        System.out.println(
                "Acesse: http://localhost:8080"
        );
    }


    // Mostra as vagas em formato JSON
    private static void listarVagas(
            HttpExchange troca)
            throws IOException {

        List<Vaga> vagas =
                vagaDAO.listarVagas();


        StringBuilder json =
                new StringBuilder("[");


        for (
            int i = 0;
            i < vagas.size();
            i++
        ) {

            Vaga vaga = vagas.get(i);


            json.append("{")
                    .append("\"id\":")
                    .append(vaga.getId())
                    .append(",")

                    .append("\"numero\":")
                    .append(vaga.getNumero())
                    .append(",")

                    .append("\"status\":\"")
                    .append(vaga.getStatus())
                    .append("\"")

                    .append("}");


            if (i < vagas.size() - 1) {

                json.append(",");
            }
        }


        json.append("]");


        enviarResposta(
                troca,
                json.toString(),
                "application/json"
        );
    }


    // Ocupa uma vaga
    private static void ocuparVaga(
            HttpExchange troca)
            throws IOException {

        String caminho =
                troca.getRequestURI().getPath();


        String[] partes =
                caminho.split("/");


        if (partes.length < 4) {

            enviarResposta(
                    troca,
                    "ID da vaga não informado.",
                    "text/plain"
            );

            return;
        }


        try {

            int id =
                    Integer.parseInt(partes[3]);


            vagaDAO.ocuparVaga(id);


            enviarResposta(
                    troca,
                    "Vaga ocupada com sucesso!",
                    "text/plain"
            );

        } catch (Exception erro) {

            enviarResposta(
                    troca,
                    "ID da vaga inválido.",
                    "text/plain"
            );
        }
    }


    // Libera uma vaga
    private static void liberarVaga(
            HttpExchange troca)
            throws IOException {

        String caminho =
                troca.getRequestURI().getPath();


        String[] partes =
                caminho.split("/");


        if (partes.length < 4) {

            enviarResposta(
                    troca,
                    "ID da vaga não informado.",
                    "text/plain"
            );

            return;
        }


        try {

            int id =
                    Integer.parseInt(partes[3]);


            vagaDAO.liberarVaga(id);


            enviarResposta(
                    troca,
                    "Vaga liberada com sucesso!",
                    "text/plain"
            );

        } catch (Exception erro) {

            enviarResposta(
                    troca,
                    "ID da vaga inválido.",
                    "text/plain"
            );
        }
    }


    // Aqui o Java entrega a página inicial
    private static void paginaInicial(
            HttpExchange troca)
            throws IOException {

        Path arquivo =
                Path.of(
                        "web",
                        "index.html"
                );


        if (!Files.exists(arquivo)) {

            enviarResposta(
                    troca,
                    "Página do ParkWay não encontrada.",
                    "text/plain"
            );

            return;
        }


        String html =
                Files.readString(
                        arquivo,
                        StandardCharsets.UTF_8
                );


        enviarResposta(
                troca,
                html,
                "text/html"
        );
    }


    // Aqui o Java entrega o arquivo CSS
    private static void estilo(
            HttpExchange troca)
            throws IOException {

        Path arquivo =
                Path.of(
                        "web",
                        "style.css"
                );


        String css =
                Files.readString(
                        arquivo,
                        StandardCharsets.UTF_8
                );


        enviarResposta(
                troca,
                css,
                "text/css"
        );
    }


    // Aqui o Java entrega o arquivo JavaScript
    private static void javascript(
            HttpExchange troca)
            throws IOException {

        Path arquivo =
                Path.of(
                        "web",
                        "script.js"
                );


        String js =
                Files.readString(
                        arquivo,
                        StandardCharsets.UTF_8
                );


        enviarResposta(
                troca,
                js,
                "application/javascript"
        );
    }


    // Método responsável por enviar
    // as respostas para o navegador
    private static void enviarResposta(
            HttpExchange troca,
            String resposta,
            String tipo)
            throws IOException {

        byte[] dados =
                resposta.getBytes(
                        StandardCharsets.UTF_8
                );


        troca.getResponseHeaders().set(
                "Content-Type",
                tipo + "; charset=UTF-8"
        );


        troca.sendResponseHeaders(
                200,
                dados.length
        );


        try (
            OutputStream saida =
                    troca.getResponseBody()
        ) {

            saida.write(dados);
        }
    }
}