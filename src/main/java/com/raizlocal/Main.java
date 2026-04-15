package com.raizlocal;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final Map<Long, Produto> PRODUTOS = new ConcurrentHashMap<>();
    private static final AtomicLong COUNTER = new AtomicLong(0);

    public static void main(String[] args) throws IOException {
        carregarProdutosIniciais();

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/produtos", Main::handleProdutosApi);
        server.createContext("/", Main::handleStaticFiles);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("RaizLocal no ar: http://localhost:" + port);
    }

    private static void handleProdutosApi(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET" -> listarProdutos(exchange);
                case "POST" -> cadastrarProduto(exchange);
                default -> sendJson(exchange, 405, "{\"erro\":\"Método não permitido\"}");
            }
        } catch (Exception ex) {
            sendJson(exchange, 500, "{\"erro\":\"Falha interna\",\"detalhe\":\"" + escape(ex.getMessage()) + "\"}");
        } finally {
            exchange.close();
        }
    }

    private static void listarProdutos(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        String filtro = null;
        if (uri.getQuery() != null && uri.getQuery().startsWith("categoria=")) {
            filtro = uri.getQuery().substring("categoria=".length()).toLowerCase(Locale.ROOT);
        }

        String finalFiltro = filtro;
        List<Produto> produtos = PRODUTOS.values().stream()
                .filter(p -> finalFiltro == null || p.categoria().toLowerCase(Locale.ROOT).contains(finalFiltro))
                .sorted(Comparator.comparing(Produto::criadoEm).reversed())
                .toList();

        sendJson(exchange, 200, toJsonArray(produtos));
    }

    private static void cadastrarProduto(HttpExchange exchange) throws IOException {
        String body = readBody(exchange.getRequestBody());
        ProdutoRequest request = parseProdutoRequest(body);

        if (request == null || isBlank(request.nome) || isBlank(request.preco)
                || isBlank(request.categoria) || isBlank(request.feirante)
                || isBlank(request.descricao) || isBlank(request.contato)) {
            sendJson(exchange, 400, "{\"erro\":\"Preencha todos os campos\"}");
            return;
        }

        long id = COUNTER.incrementAndGet();
        Produto produto = new Produto(
                id,
                request.nome.trim(),
                request.preco.trim(),
                request.categoria.trim(),
                request.feirante.trim(),
                request.descricao.trim(),
                request.contato.trim(),
                Instant.now().toString()
        );

        PRODUTOS.put(id, produto);
        sendJson(exchange, 201, toJson(produto));
    }

    private static void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }

        Path webRoot = Path.of("web");
        Path filePath = webRoot.resolve(path.substring(1)).normalize();

        if (!filePath.startsWith(webRoot) || Files.notExists(filePath) || Files.isDirectory(filePath)) {
            sendText(exchange, 404, "Página não encontrada", "text/plain");
            exchange.close();
            return;
        }

        byte[] bytes = Files.readAllBytes(filePath);
        String contentType = getContentType(filePath.getFileName().toString());
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
    }

    private static void sendText(HttpExchange exchange, int status, String text, String contentType) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
    }

    private static String readBody(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static ProdutoRequest parseProdutoRequest(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        ProdutoRequest req = new ProdutoRequest();
        req.nome = extractJsonValue(json, "nome");
        req.preco = extractJsonValue(json, "preco");
        req.categoria = extractJsonValue(json, "categoria");
        req.feirante = extractJsonValue(json, "feirante");
        req.descricao = extractJsonValue(json, "descricao");
        req.contato = extractJsonValue(json, "contato");
        return req;
    }

    private static String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"(.*?)\\\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1)
                .replace("\\\\", "\\")
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\t", "\t");
    }

    private static String toJsonArray(List<Produto> produtos) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < produtos.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(toJson(produtos.get(i)));
        }
        sb.append(']');
        return sb.toString();
    }

    private static String toJson(Produto p) {
        return "{" +
                "\"id\":" + p.id() + "," +
                "\"nome\":\"" + escape(p.nome()) + "\"," +
                "\"preco\":\"" + escape(p.preco()) + "\"," +
                "\"categoria\":\"" + escape(p.categoria()) + "\"," +
                "\"feirante\":\"" + escape(p.feirante()) + "\"," +
                "\"descricao\":\"" + escape(p.descricao()) + "\"," +
                "\"contato\":\"" + escape(p.contato()) + "\"," +
                "\"criadoEm\":\"" + escape(p.criadoEm()) + "\"" +
                "}";
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String getContentType(String filename) {
        if (filename.endsWith(".html")) return "text/html";
        if (filename.endsWith(".css")) return "text/css";
        if (filename.endsWith(".js")) return "application/javascript";
        if (filename.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void carregarProdutosIniciais() {
        List<ProdutoRequest> seed = new ArrayList<>();
        seed.add(new ProdutoRequest("Tomate Orgânico", "R$ 8,00/kg", "Hortifruti", "Feira da Dona Cida", "Colheita da semana, sem agrotóxico.", "(11) 99999-1000"));
        seed.add(new ProdutoRequest("Queijo Minas Artesanal", "R$ 38,00", "Laticínios", "Sítio Bom Leite", "Queijo curado por 20 dias.", "(11) 98888-2020"));
        seed.add(new ProdutoRequest("Mel Silvestre", "R$ 25,00", "Naturais", "Apiário Vale Verde", "Pote de 500g, produção local.", "(11) 97777-3030"));

        for (ProdutoRequest item : seed) {
            long id = COUNTER.incrementAndGet();
            PRODUTOS.put(id, new Produto(
                    id,
                    item.nome,
                    item.preco,
                    item.categoria,
                    item.feirante,
                    item.descricao,
                    item.contato,
                    Instant.now().minusSeconds(id * 600).toString()
            ));
        }
    }

    record Produto(long id, String nome, String preco, String categoria, String feirante,
                   String descricao, String contato, String criadoEm) {
    }

    static class ProdutoRequest {
        String nome;
        String preco;
        String categoria;
        String feirante;
        String descricao;
        String contato;

        ProdutoRequest() {
        }

        ProdutoRequest(String nome, String preco, String categoria, String feirante, String descricao, String contato) {
            this.nome = nome;
            this.preco = preco;
            this.categoria = categoria;
            this.feirante = feirante;
            this.descricao = descricao;
            this.contato = contato;
        }
    }
}
