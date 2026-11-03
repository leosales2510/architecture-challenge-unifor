// Única porta de saída do front-end para o back-end. Nenhum preço, total ou regra
// é calculado aqui: tudo vem das APIs REST dos microsserviços.

export class ErroApi extends Error {
  constructor(status, problema) {
    super(problema?.detail || mensagemPadrao(status))
    this.status = status
    this.problema = problema // corpo RFC 7807: { codigo, detail, faltas, trilha, pedido, ... }
  }
}

function mensagemPadrao(status) {
  if (status >= 500) return 'Serviço indisponível no momento. Verifique se os microsserviços estão rodando.'
  return `Erro HTTP ${status}`
}

async function chamar(metodo, url, corpo) {
  let resposta
  try {
    resposta = await fetch(url, {
      method: metodo,
      headers: corpo ? { 'Content-Type': 'application/json' } : undefined,
      body: corpo ? JSON.stringify(corpo) : undefined,
    })
  } catch {
    throw new ErroApi(503, null)
  }
  const texto = await resposta.text()
  let dados = null
  try { dados = texto ? JSON.parse(texto) : null } catch { dados = null }
  if (!resposta.ok) throw new ErroApi(resposta.status, dados)
  return dados
}

export const api = {
  // catalogo-service (8081)
  catalogo: () => chamar('GET', '/api/catalogo'),
  montar: (linha, saborSku, coberturas) => chamar('POST', '/api/catalogo/montagens', { linha, saborSku, coberturas }),

  // pedido-service (8084)
  criarPedido: () => chamar('POST', '/api/pedidos'),
  pedido: (id) => chamar('GET', `/api/pedidos/${id}`),
  adicionarItem: (id, item) => chamar('POST', `/api/pedidos/${id}/itens`, item),
  removerItem: (id, codigo) => chamar('DELETE', `/api/pedidos/${id}/itens/${codigo}`),
  aplicarCupom: (id, codigo) => chamar('PUT', `/api/pedidos/${id}/cupom`, { codigo }),
  desfazer: (id) => chamar('POST', `/api/pedidos/${id}/desfazer`),
  finalizar: (id, metodo, numeroCartao) => chamar('POST', `/api/pedidos/${id}/finalizacao`, { metodo, numeroCartao }),
  cancelar: (id) => chamar('POST', `/api/pedidos/${id}/cancelamento`),
  reconciliar: (id) => chamar('POST', `/api/pedidos/${id}/reconciliacao`),
  cupons: () => chamar('GET', '/api/cupons'),

  // estoque-service (8082) — painel do operador
  estoque: () => chamar('GET', '/api/estoque'),
  ajustarEstoque: (sku, quantidade) => chamar('PUT', `/api/estoque/${sku}`, { quantidade }),
}

export const reais = (valor) =>
  Number(valor ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
