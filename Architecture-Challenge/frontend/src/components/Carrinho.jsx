import { useState } from 'react'
import { reais } from '../api.js'

export default function Carrinho({ pedido, cupons, ocupado, onRemover, onCupom, onDesfazer, onFinalizar, onCancelar }) {
  const [codigo, setCodigo] = useState('')
  const editavel = pedido.editavel && !ocupado
  const proximoDesfazer = pedido.historicoDesfazer[0]

  function aplicar(e) {
    e.preventDefault()
    if (codigo.trim()) onCupom(codigo.trim())
    setCodigo('')
  }

  return (
    <section className="cartao carrinho">
      <h2>Seu pedido</h2>

      {pedido.itens.length === 0 && <p className="vazio">Nenhum item ainda.</p>}
      <ul className="itens">
        {pedido.itens.map((item) => (
          <li key={item.codigo}>
            <div>
              <span className={`selo selo-${item.linha}`}>{item.linha === 'ZERO_ACUCAR' ? 'Zero' : 'Tradicional'}</span>
              <p>{item.descricao}</p>
              <small>{item.quantidade} × {reais(item.precoUnitario)}</small>
            </div>
            <div className="item-acoes">
              <strong>{reais(item.subtotal)}</strong>
              <button className="botao-link" onClick={() => onRemover(item.codigo)} disabled={!editavel}>Remover</button>
            </div>
          </li>
        ))}
      </ul>

      <form className="cupom" onSubmit={aplicar}>
        <input value={codigo} onChange={(e) => setCodigo(e.target.value)} placeholder="Cupom de desconto"
               disabled={!editavel} aria-label="Código do cupom" />
        <button className="botao-secundario" disabled={!editavel || !codigo.trim()}>Aplicar</button>
      </form>
      <p className="dica">Cupons: {cupons.map((c) => c.codigo).join(' · ')}</p>

      <dl className="totais">
        <div><dt>Subtotal</dt><dd>{reais(pedido.subtotal)}</dd></div>
        <div>
          <dt>Desconto {pedido.cupom && <em>({pedido.cupom.codigo}{pedido.cupom.ativo ? '' : ' — mínimo não atingido'})</em>}</dt>
          <dd>− {reais(pedido.desconto)}</dd>
        </div>
        <div className="total"><dt>Total</dt><dd>{reais(pedido.total)}</dd></div>
      </dl>

      <button className="botao-desfazer" onClick={onDesfazer} disabled={!editavel || !proximoDesfazer}>
        ↶ Desfazer{proximoDesfazer ? `: ${proximoDesfazer}` : ''}
      </button>
      {pedido.historicoDesfazer.length > 1 && (
        <details className="historico">
          <summary>Histórico de ações ({pedido.historicoDesfazer.length})</summary>
          <ol>{pedido.historicoDesfazer.map((h, i) => <li key={i}>{h}</li>)}</ol>
        </details>
      )}

      <div className="acoes-finais">
        <button className="botao-link" onClick={onCancelar} disabled={!editavel}>Cancelar pedido</button>
        <button className="botao-principal" onClick={onFinalizar} disabled={!editavel || pedido.itens.length === 0}>
          Finalizar · {reais(pedido.total)}
        </button>
      </div>
    </section>
  )
}
