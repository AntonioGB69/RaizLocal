const form = document.getElementById('produto-form');
const statusEl = document.getElementById('status');
const listaProdutos = document.getElementById('lista-produtos');
const filtroCategoria = document.getElementById('filtro-categoria');
const STORAGE_KEY = 'raizlocal_produtos_v1';

const produtosIniciais = [
  {
    id: crypto.randomUUID(),
    nome: 'Tomate Orgânico',
    preco: 'R$ 8,00/kg',
    categoria: 'Hortifruti',
    feirante: 'Feira da Dona Cida',
    descricao: 'Colheita da semana, sem agrotóxico.',
    contato: '(11) 99999-1000',
    criadoEm: new Date().toISOString()
  },
  {
    id: crypto.randomUUID(),
    nome: 'Queijo Minas Artesanal',
    preco: 'R$ 38,00',
    categoria: 'Laticínios',
    feirante: 'Sítio Bom Leite',
    descricao: 'Queijo curado por 20 dias.',
    contato: '(11) 98888-2020',
    criadoEm: new Date(Date.now() - 600000).toISOString()
  },
  {
    id: crypto.randomUUID(),
    nome: 'Mel Silvestre',
    preco: 'R$ 25,00',
    categoria: 'Naturais',
    feirante: 'Apiário Vale Verde',
    descricao: 'Pote de 500g, produção local.',
    contato: '(11) 97777-3030',
    criadoEm: new Date(Date.now() - 1200000).toISOString()
  }
];

function carregarDoStorage() {
  const bruto = localStorage.getItem(STORAGE_KEY);
  if (!bruto) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(produtosIniciais));
    return [...produtosIniciais];
  }

  try {
    const produtos = JSON.parse(bruto);
    return Array.isArray(produtos) ? produtos : [];
  } catch {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(produtosIniciais));
    return [...produtosIniciais];
  }
}

function salvarNoStorage(produtos) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(produtos));
}

function carregarProdutos(categoria = '') {
  const todos = carregarDoStorage();
  const filtro = categoria.trim().toLowerCase();

  const produtos = todos
    .filter((produto) => !filtro || produto.categoria.toLowerCase().includes(filtro))
    .sort((a, b) => new Date(b.criadoEm) - new Date(a.criadoEm));

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

form.addEventListener('submit', (event) => {
  event.preventDefault();
  statusEl.textContent = 'Publicando produto...';
  statusEl.style.color = '#5f6f5f';

  const payload = Object.fromEntries(new FormData(form).entries());
  const faltando = Object.values(payload).some((valor) => !valor.trim());
  if (faltando) {
    statusEl.textContent = 'Preencha todos os campos.';
    statusEl.style.color = '#b01d1d';
    return;
  }

  const produtos = carregarDoStorage();
  const novoProduto = {
    id: crypto.randomUUID(),
    ...payload,
    criadoEm: new Date().toISOString()
  };

  produtos.push(novoProduto);
  salvarNoStorage(produtos);

  statusEl.textContent = `Produto "${novoProduto.nome}" publicado com sucesso!`;
  statusEl.style.color = '#236937';
  form.reset();
  carregarProdutos(filtroCategoria.value.trim());
});

let debounce;
filtroCategoria.addEventListener('input', () => {
  clearTimeout(debounce);
  debounce = setTimeout(() => {
    carregarProdutos(filtroCategoria.value.trim());
  }, 250);
});

carregarProdutos();
