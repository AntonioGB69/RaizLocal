# RaizLocal (site estático)

Versão inicial da plataforma em **HTML + CSS + JavaScript**, sem backend, para ajudar feirantes da região a anunciarem seus produtos.

## Funcionalidades

- Cadastro de produtos com nome, preço, categoria, descrição e contato.
- Vitrine de produtos com atualização imediata.
- Filtro por categoria.
- Dados salvos no `localStorage` do navegador.
- Layout responsivo, pronto para futuras evoluções.

## Como rodar localmente

Opção 1 (mais simples): abra `web/index.html` no navegador.

Opção 2 (recomendada para desenvolvimento):

```bash
python -m http.server 8080 -d web
```

Depois, acesse `http://localhost:8080`.

## Publicar no GitHub Pages

1. Suba este repositório no GitHub.
2. Vá em **Settings > Pages**.
3. Em **Build and deployment**, selecione:
   - **Source:** Deploy from a branch
   - **Branch:** `main`
   - **Folder:** `/web`
4. Salve e aguarde a URL pública ser gerada.

## Próximas atualizações sugeridas

- Login para feirantes e painel de gerenciamento.
- Geolocalização das feiras por bairro/cidade.
- Backend com API real (Java/Spring ou Node) para dados compartilhados entre usuários.
- Integração com pagamentos e entregas.
