# RaizLocal

Plataforma simples em **HTML + CSS + JavaScript** com backend em **Java** para ajudar feirantes da região a anunciarem seus produtos.

## Funcionalidades

- Cadastro de produtos com nome, preço, categoria, descrição e contato.
- Vitrine pública de produtos com atualização instantânea.
- Filtro por categoria.
- Layout responsivo e preparado para futuras evoluções.

## Tecnologias

- Java 17
- Servidor HTTP nativo do Java (`com.sun.net.httpserver.HttpServer`)
- Front-end estático (HTML/CSS/JS)

## Como executar (sem dependências externas)

```bash
javac -d out src/main/java/com/raizlocal/Main.java
java -cp out com.raizlocal.Main
```

Depois, acesse: `http://localhost:8080`

## Próximas atualizações sugeridas

- Login para feirantes e painel de gerenciamento.
- Geolocalização das feiras por bairro/cidade.
- Integração com pagamentos e entregas.
- Avaliações de clientes e métricas de vendas.
