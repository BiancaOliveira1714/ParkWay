
package br.com.parkway;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Servidor {

    public static void iniciar() throws IOException {

        HttpServer servidor = HttpServer.create(
                new InetSocketAddress(8080), 0);

        servidor.createContext("/", Servidor::paginaInicial);
        servidor.createContext("/cadastro.html", Servidor::cadastro);
        servidor.createContext("/style.css", Servidor::estilo);
        servidor.createContext("/script.js", Servidor::javascript);

        servidor.createContext("/api/estacionamentos",
                Servidor::listarEstacionamentos);

        servidor.createContext("/api/vagas",
                Servidor::listarVagas);

        servidor.createContext("/api/entrada",
                Servidor::registrarEntrada);

        servidor.createContext("/api/ocupar",
                Servidor::ocuparVaga);

        servidor.createContext("/api/liberar",
                Servidor::liberarVaga);

        servidor.setExecutor(null);
        servidor.start();

        System.out.println("Servidor do ParkWay iniciado!");
        System.out.println("Acesse: http://localhost:8080");
    }

    // ================= ESTACIONAMENTOS =================

    private static void listarEstacionamentos(HttpExchange troca)
            throws IOException {

        String sql = """
                SELECT e.id, e.nome, e.capacidade,
                COUNT(v.id) AS total,
                SUM(CASE WHEN v.status = 'OCUPADA'
                    THEN 1 ELSE 0 END) AS ocupadas
                FROM estacionamentos e
                LEFT JOIN vagas v ON v.estacionamento_id = e.id
                GROUP BY e.id, e.nome, e.capacidade
                ORDER BY e.id
                """;

        StringBuilder json = new StringBuilder("[");

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            boolean primeiro = true;

            while (rs.next()) {

                if (!primeiro) json.append(",");
                primeiro = false;

                json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"nome\":\"").append(escapar(rs.getString("nome"))).append("\",")
                        .append("\"capacidade\":").append(rs.getInt("capacidade")).append(",")
                        .append("\"total\":").append(rs.getInt("total")).append(",")
                        .append("\"ocupadas\":").append(rs.getInt("ocupadas"))
                        .append("}");
            }

            json.append("]");
            enviarResposta(troca, json.toString(), "application/json");

        } catch (SQLException e) {
            erro(troca, "Erro ao listar estacionamentos.");
        }
    }

    // ================= VAGAS =================

    private static void listarVagas(HttpExchange troca)
            throws IOException {

        int estacionamentoId = 1;

        String query = troca.getRequestURI().getRawQuery();

        if (query != null) {
            Map<String, String> parametros = parametros(query);

            try {
                estacionamentoId = Integer.parseInt(
                        parametros.getOrDefault("estacionamento", "1"));
            } catch (NumberFormatException e) {
                erro(troca, "Estacionamento inválido.");
                return;
            }
        }

        String sql = """
                SELECT id, numero, status
                FROM vagas
                WHERE estacionamento_id = ?
                ORDER BY numero
                """;

        StringBuilder json = new StringBuilder("[");

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, estacionamentoId);

            try (ResultSet rs = stmt.executeQuery()) {

                boolean primeiro = true;

                while (rs.next()) {

                    if (!primeiro) json.append(",");
                    primeiro = false;

                    json.append("{")
                            .append("\"id\":").append(rs.getInt("id")).append(",")
                            .append("\"numero\":").append(rs.getInt("numero")).append(",")
                            .append("\"status\":\"").append(rs.getString("status")).append("\"")
                            .append("}");
                }
            }

            json.append("]");
            enviarResposta(troca, json.toString(), "application/json");

        } catch (SQLException e) {
            erro(troca, "Erro ao listar vagas.");
        }
    }

    // ================= ENTRADA =================

    private static void registrarEntrada(HttpExchange troca)
            throws IOException {

        if (!troca.getRequestMethod().equalsIgnoreCase("POST")) {
            erro(troca, "Método inválido.");
            return;
        }

        Map<String, String> dados = parametros(
                new String(troca.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8));

        String nome = dados.get("nome");
        String telefone = dados.get("telefone");
        String modelo = dados.get("modelo");
        String placa = dados.get("placa");
        String cor = dados.get("cor");

        int estacionamentoId;
        int vagaId;

        try {
            estacionamentoId = Integer.parseInt(
                    dados.get("estacionamentoId"));

            vagaId = Integer.parseInt(dados.get("vagaId"));

        } catch (Exception e) {
            erro(troca, "Estacionamento ou vaga inválidos.");
            return;
        }

        if (nome == null || modelo == null || placa == null
                || nome.isBlank() || modelo.isBlank() || placa.isBlank()) {
            erro(troca, "Preencha nome, modelo e placa.");
            return;
        }

        String sqlCliente = """
                INSERT INTO clientes(nome, telefone)
                VALUES (?, ?)
                """;

        String sqlVeiculo = """
                INSERT INTO veiculos(cliente_id, modelo, placa, cor)
                VALUES (?, ?, ?, ?)
                """;

        String sqlVaga = """
                SELECT id, status FROM vagas
                WHERE id = ? AND estacionamento_id = ?
                FOR UPDATE
                """;

        String sqlMovimentacao = """
                INSERT INTO movimentacoes
                (cliente_id, veiculo_id, estacionamento_id, vaga_id, entrada)
                VALUES (?, ?, ?, ?, NOW())
                """;

        try (Connection conn = Conexao.conectar()) {

            conn.setAutoCommit(false);

            try {

                // Verifica se a vaga está livre
                try (PreparedStatement stmt =
                             conn.prepareStatement(sqlVaga)) {

                    stmt.setInt(1, vagaId);
                    stmt.setInt(2, estacionamentoId);

                    try (ResultSet rs = stmt.executeQuery()) {

                        if (!rs.next()) {
                            conn.rollback();
                            erro(troca, "Vaga não encontrada.");
                            return;
                        }

                        if (!rs.getString("status").equals("LIVRE")) {
                            conn.rollback();
                            erro(troca, "Essa vaga já está ocupada.");
                            return;
                        }
                    }
                }

                int clienteId;

                // Cadastra cliente
                try (PreparedStatement stmt = conn.prepareStatement(
                        sqlCliente, PreparedStatement.RETURN_GENERATED_KEYS)) {

                    stmt.setString(1, nome);
                    stmt.setString(2, telefone);

                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        rs.next();
                        clienteId = rs.getInt(1);
                    }
                }

                int veiculoId;

                // Cadastra veículo
                try (PreparedStatement stmt = conn.prepareStatement(
                        sqlVeiculo, PreparedStatement.RETURN_GENERATED_KEYS)) {

                    stmt.setInt(1, clienteId);
                    stmt.setString(2, modelo);
                    stmt.setString(3, placa);
                    stmt.setString(4, cor);

                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        rs.next();
                        veiculoId = rs.getInt(1);
                    }
                }

                // Registra entrada
                try (PreparedStatement stmt =
                             conn.prepareStatement(sqlMovimentacao)) {

                    stmt.setInt(1, clienteId);
                    stmt.setInt(2, veiculoId);
                    stmt.setInt(3, estacionamentoId);
                    stmt.setInt(4, vagaId);

                    stmt.executeUpdate();
                }

                // Ocupa a vaga
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE vagas SET status='OCUPADA' WHERE id=?")) {

                    stmt.setInt(1, vagaId);
                    stmt.executeUpdate();
                }

                conn.commit();

                enviarResposta(troca,
                        "{\"sucesso\":true,\"mensagem\":\"Entrada registrada com sucesso!\"}",
                        "application/json");

            } catch (Exception e) {

                conn.rollback();

                if (e instanceof SQLException
                        && ((SQLException) e).getErrorCode() == 1062) {

                    erro(troca, "Essa placa já está cadastrada.");

                } else {
                    e.printStackTrace();
                    erro(troca, "Erro ao registrar entrada.");
                }

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            erro(troca, "Erro na conexão com o banco.");
        }
    }

    // ================= OCUPAR VAGA =================

    private static void ocuparVaga(HttpExchange troca)
            throws IOException {

        String[] partes = troca.getRequestURI().getPath().split("/");

        if (partes.length < 4) {
            erro(troca, "Informe o ID da vaga.");
            return;
        }

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE vagas SET status='OCUPADA' WHERE id=?")) {

            stmt.setInt(1, Integer.parseInt(partes[3]));

            int resultado = stmt.executeUpdate();

            enviarResposta(troca,
                    resultado > 0 ? "Vaga ocupada!" : "Vaga não encontrada.",
                    "text/plain");

        } catch (Exception e) {
            erro(troca, "Erro ao ocupar vaga.");
        }
    }

    // ================= LIBERAR VAGA =================

    private static void liberarVaga(HttpExchange troca)
            throws IOException {

        String[] partes = troca.getRequestURI().getPath().split("/");

        if (partes.length < 4) {
            erro(troca, "Informe o ID da vaga.");
            return;
        }

        try (Connection conn = Conexao.conectar()) {

            conn.setAutoCommit(false);

            try {

                int vagaId = Integer.parseInt(partes[3]);

                // Registra saída da movimentação ativa
                try (PreparedStatement stmt = conn.prepareStatement("""
                        UPDATE movimentacoes
                        SET saida = NOW()
                        WHERE vaga_id = ? AND saida IS NULL
                        """)) {

                    stmt.setInt(1, vagaId);
                    stmt.executeUpdate();
                }

                // Libera vaga
                try (PreparedStatement stmt = conn.prepareStatement("""
                        UPDATE vagas SET status='LIVRE'
                        WHERE id=?
                        """)) {

                    stmt.setInt(1, vagaId);

                    int resultado = stmt.executeUpdate();

                    if (resultado == 0) {
                        conn.rollback();
                        erro(troca, "Vaga não encontrada.");
                        return;
                    }
                }

                conn.commit();

                enviarResposta(troca,
                        "Vaga liberada com sucesso!",
                        "text/plain");

            } catch (Exception e) {

                conn.rollback();
                erro(troca, "Erro ao liberar vaga.");

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            erro(troca, "Erro na conexão com o banco.");
        }
    }

    // ================= PÁGINAS =================

    private static void paginaInicial(HttpExchange troca)
            throws IOException {

        if (troca.getRequestURI().getPath().equals("/")) {
            enviarArquivo(troca, "index.html", "text/html");
        } else {
            erro(troca, "Página não encontrada.");
        }
    }

    private static void cadastro(HttpExchange troca)
            throws IOException {

        enviarArquivo(troca, "cadastro.html", "text/html");
    }

    private static void estilo(HttpExchange troca)
            throws IOException {

        enviarArquivo(troca, "style.css", "text/css");
    }

    private static void javascript(HttpExchange troca)
            throws IOException {

        enviarArquivo(troca, "script.js", "application/javascript");
    }

    private static void enviarArquivo(HttpExchange troca,
                                     String nome,
                                     String tipo) throws IOException {

        Path arquivo = Path.of("web", nome);

        if (!Files.exists(arquivo)) {
            erro(troca, "Arquivo não encontrado: " + nome);
            return;
        }

        String conteudo = Files.readString(
                arquivo, StandardCharsets.UTF_8);

        enviarResposta(troca, conteudo, tipo);
    }

    // ================= UTILITÁRIOS =================

    private static Map<String, String> parametros(String texto) {

        Map<String, String> mapa = new HashMap<>();

        for (String item : texto.split("&")) {

            String[] partes = item.split("=", 2);

            if (partes.length == 2) {

                mapa.put(
                        URLDecoder.decode(partes[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(partes[1], StandardCharsets.UTF_8)
                );
            }
        }

        return mapa;
    }

    private static String escapar(String texto) {

        if (texto == null) return "";

        return texto.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static void erro(HttpExchange troca, String mensagem)
            throws IOException {

        enviarResposta(troca,
                "{\"sucesso\":false,\"mensagem\":\""
                        + escapar(mensagem) + "\"}",
                "application/json");
    }

    private static void enviarResposta(HttpExchange troca,
                                      String resposta,
                                      String tipo) throws IOException {

        byte[] dados = resposta.getBytes(StandardCharsets.UTF_8);

        troca.getResponseHeaders().set(
                "Content-Type", tipo + "; charset=UTF-8");

        troca.sendResponseHeaders(200, dados.length);

        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(dados);
        }
    }
}
