const form = document.getElementById('produto-form');
const statusEl = document.getElementById('status');
const listaProdutos = document.getElementById('lista-produtos');
const filtroCategoria = document.getElementById('filtro-categoria');

async function carregarProdutos(categoria = '') {
  const url = categoria
    ? `/api/produtos?categoria=${encodeURIComponent(categoria)}`
    : '/api/produtos';

  const resposta = await fetch(url);
  const produtos = await resposta.json();

  listaProdutos.innerHTML = '';

  if (!produtos.length) {
    listaProdutos.innerHTML = '<p>Nenhum produto encontrado para esse filtro.</p>';
    return;
  }

  produtos.forEach((produto) => {
    const card = document.createElement('article');
    card.className = 'card';
    card.innerHTML = `
      <h3>${produto.nome}</h3>
      <p>${produto.descricao}</p>
      <div class="tags">
        <span class="tag">${produto.preco}</span>
        <span class="tag">${produto.categoria}</span>
      </div>
      <div class="meta"><strong>${produto.feirante}</strong> • Contato: ${produto.contato}</div>
    `;
    listaProdutos.appendChild(card);
  });
}

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  statusEl.textContent = 'Publicando produto...';
  statusEl.style.color = '#5f6f5f';

  const payload = Object.fromEntries(new FormData(form).entries());

  const resposta = await fetch('/api/produtos', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });

  const resultado = await resposta.json();

  if (!resposta.ok) {
    statusEl.textContent = resultado.erro || 'Falha ao cadastrar.';
    statusEl.style.color = '#b01d1d';
    return;
  }

  statusEl.textContent = `Produto "${resultado.nome}" publicado com sucesso!`;
  statusEl.style.color = '#236937';
  form.reset();
  await carregarProdutos(filtroCategoria.value.trim());
});

let debounce;
filtroCategoria.addEventListener('input', () => {
  clearTimeout(debounce);
  debounce = setTimeout(() => {
    carregarProdutos(filtroCategoria.value.trim());
  }, 250);
});

carregarProdutos();
