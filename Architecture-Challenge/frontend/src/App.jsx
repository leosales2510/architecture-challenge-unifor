import { useCallback, useEffect, useRef, useState } from 'react'
import { api } from './api.js'
import Montador from './components/Montador.jsx'
import Carrinho from './components/Carrinho.jsx'
import Pagamento from './components/Pagamento.jsx'
import Resultado from './components/Resultado.jsx'
import PainelEstoque from './components/PainelEstoque.jsx'

const CHAVE_PEDIDO = 'totem.pedidoId'

function lerPedidoSalvo() {
  try { return localStorage.getItem(CHAVE_PEDIDO) } catch { return null }
}
function salvarPedido(id) {
  try { localStorage.setItem(CHAVE_PEDIDO, String(id)) } catch { /* modo privado: ignora */ }
}

export default function App() {
  const [catalogo, setCatalogo] = useState(null)
  const [cupons, setCupons] = useState([])
  const [pedido, setPedido] = useState(null)
  const [estoque, setEstoque] = useState([])
  const [ocupado, setOcupado] = useState(false)
  const [aviso, setAviso] = useState(null)          // { tipo: 'erro' | 'ok', texto }
  const [pagando, setPagando] = useState(false)
  const [resultado, setResultado] = useState(null)  // retorno do checkout (sucesso ou erro)
  const [erroInicial, setErroInicial] = useState(null)
  const iniciado = useRef(false) // o StrictMode roda efeitos 2x em dev: evita abrir dois pedidos

  const atualizarEstoque = useCallback(() => api.estoque().then(setEstoque).catch(() => setEstoque([])), [])

  const novoPedido = useCallback(async () => {
    const p = await api.criarPedido()
    salvarPedido(p.id)
    setPedido(p)
    setResultado(null)
    return p
  }, [])

  useEffect(() => {
    if (iniciado.current) return
    iniciado.current = true
    async function iniciar() {
      try {
        const [cat, cup] = await Promise.all([api.catalogo(), api.cupons()])
        setCatalogo(cat)
        setCupons(cup)
        const salvo = lerPedidoSalvo()
        const existente = salvo ? await api.pedido(salvo).catch(() => null) : null
        if (existente && existente.editavel) setPedido(existente)
        else await novoPedido()
        atualizarEstoque()
      } catch (e) {
        setErroInicial(e.message)
      }
    }
    iniciar()
  }, [novoPedido, atualizarEstoque])

  useEffect(() => {
    if (!aviso) return undefined
    const t = setTimeout(() => setAviso(null), 5000)
    return () => clearTimeout(t)
  }, [aviso])

  /** Executa uma ação no pedido-service, atualiza a tela e trata erros RFC 7807. */
  async function acao(fn, mensagemOk) {
    setOcupado(true)
    try {
      const atualizado = await fn()
      setPedido(atualizado)
      if (mensagemOk) setAviso({ tipo: 'ok', texto: mensagemOk })
    } catch (e) {
      const faltas = e.problema?.faltas?.length
        ? ' — ' + e.problema.faltas.map((f) => `${f.sku}: pedido ${f.solicitado}, disponível ${f.disponivel}`).join('; ')
        : ''
      setAviso({ tipo: 'erro', texto: e.message + faltas })
    } finally {
      setOcupado(false)
      atualizarEstoque()
    }
  }

  async function finalizar(metodo, numeroCartao) {
    setOcupado(true)
    try {
      const r = await api.finalizar(pedido.id, metodo, numeroCartao)
      setPedido(r.pedido)
      setResultado({ ok: true, ...r })
    } catch (e) {
      const p = e.problema
      if (p?.pedido) setPedido(p.pedido)
      setResultado({ ok: false, resultado: p?.codigo, mensagem: e.message, trilha: p?.trilha || [], faltas: p?.faltas || [] })
    } finally {
      setPagando(false)
      setOcupado(false)
      atualizarEstoque()
    }
  }

  if (erroInicial) {
    return (
      <div className="tela-erro">
        <h1>Não foi possível abrir o totem</h1>
        <p>{erroInicial}</p>
        <p>Suba os serviços com <code>./scripts/start-all.sh</code> e recarregue a página.</p>
      </div>
    )
  }
  if (!catalogo || !pedido) return <div className="carregando">Carregando o cardápio…</div>

  const id = pedido.id
  return (
    <div className="app">
      <header className="topo">
        <div className="marca">
          <img src="/sorvete.svg" alt="" width="36" height="36" />
          <div>
            <h1>Sorveteria</h1>
            <span>Totem de autoatendimento</span>
          </div>
        </div>
        <div className="topo-pedido">
          <span>Pedido #{id}</span>
          <span className={`status status-${pedido.status}`}>{pedido.status.replaceAll('_', ' ')}</span>
          <button className="botao-secundario" onClick={() => acao(novoPedido)} disabled={ocupado}>Novo pedido</button>
        </div>
      </header>

      {aviso && <div className={`aviso aviso-${aviso.tipo}`} role="status">{aviso.texto}</div>}
      {pedido.status === 'PAGAMENTO_EM_VERIFICACAO' && (
        <div className="aviso aviso-alerta">
          {pedido.mensagem} Não pague de novo: chame um atendente ou verifique o pagamento.{' '}
          <button className="botao-secundario" disabled={ocupado}
                  onClick={() => acao(() => api.reconciliar(id), 'Pagamento confirmado')}>Verificar pagamento</button>
        </div>
      )}
      {pedido.mensagem && pedido.editavel && (
        <div className="aviso aviso-alerta">Checkout não concluído: {pedido.mensagem}</div>
      )}

      <main className="grade">
        <Montador
          catalogo={catalogo}
          desabilitado={ocupado || !pedido.editavel}
          onAdicionar={(item) => acao(() => api.adicionarItem(id, item), 'Item adicionado ao pedido')}
        />
        <Carrinho
          pedido={pedido}
          cupons={cupons}
          ocupado={ocupado}
          onRemover={(codigo) => acao(() => api.removerItem(id, codigo))}
          onCupom={(codigo) => acao(() => api.aplicarCupom(id, codigo), `Cupom ${codigo.toUpperCase()} aplicado`)}
          onDesfazer={() => acao(() => api.desfazer(id), 'Última ação desfeita')}
          onFinalizar={() => setPagando(true)}
          onCancelar={() => acao(() => api.cancelar(id), 'Pedido cancelado')}
        />
      </main>

      <PainelEstoque itens={estoque} onRepor={(sku, qtd) => api.ajustarEstoque(sku, qtd).then(atualizarEstoque)} />

      {pagando && <Pagamento total={pedido.total} ocupado={ocupado} onPagar={finalizar} onFechar={() => setPagando(false)} />}
      {resultado && (
        <Resultado
          resultado={resultado}
          onNovoPedido={() => acao(novoPedido)}
          onFechar={() => setResultado(null)}
        />
      )}
    </div>
  )
}
