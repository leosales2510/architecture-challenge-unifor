import { useState } from 'react'
import { reais } from '../api.js'

// Cartões de teste aceitos pelo simulador do pagamento-service (LegacyCardProcessor).
const CENARIOS = [
  { id: 'aprovado', titulo: 'Cartão aprovado', detalhe: '4111 1111 1111 1111', metodo: 'CARTAO', cartao: '4111111111111111' },
  { id: 'recusado', titulo: 'Cartão recusado', detalhe: '4000 0000 0000 0002 · saldo insuficiente', metodo: 'CARTAO', cartao: '4000000000000002' },
  { id: 'fora', titulo: 'Operadora fora do ar', detalhe: '4000 0000 0000 9999 · simula timeout', metodo: 'CARTAO', cartao: '4000000000009999' },
  { id: 'pix', titulo: 'Pix', detalhe: 'aprovado até R$ 1.000,00', metodo: 'PIX', cartao: null },
]

export default function Pagamento({ total, ocupado, onPagar, onFechar }) {
  const [escolhido, setEscolhido] = useState('aprovado')
  const [outroCartao, setOutroCartao] = useState('')

  function pagar() {
    if (escolhido === 'outro') return onPagar('CARTAO', outroCartao)
    const c = CENARIOS.find((x) => x.id === escolhido)
    return onPagar(c.metodo, c.cartao)
  }

  return (
    <div className="fundo-modal" role="dialog" aria-modal="true" aria-labelledby="titulo-pagamento">
      <div className="modal">
        <h2 id="titulo-pagamento">Pagamento · {reais(total)}</h2>
        <p className="dica">Pagamento simulado: escolha um cenário para demonstrar aprovação, recusa ou falha.</p>
        <div className="cenarios">
          {CENARIOS.map((c) => (
            <label key={c.id} className={`cenario ${escolhido === c.id ? 'ativo' : ''}`}>
              <input type="radio" name="cenario" checked={escolhido === c.id} onChange={() => setEscolhido(c.id)} />
              <span><strong>{c.titulo}</strong><small>{c.detalhe}</small></span>
            </label>
          ))}
          <label className={`cenario ${escolhido === 'outro' ? 'ativo' : ''}`}>
            <input type="radio" name="cenario" checked={escolhido === 'outro'} onChange={() => setEscolhido('outro')} />
            <span>
              <strong>Outro cartão</strong>
              <input value={outroCartao} onChange={(e) => { setOutroCartao(e.target.value); setEscolhido('outro') }}
                     placeholder="Número do cartão" inputMode="numeric" aria-label="Número do cartão" />
            </span>
          </label>
        </div>
        <div className="acoes-finais">
          <button className="botao-link" onClick={onFechar} disabled={ocupado}>Voltar</button>
          <button className="botao-principal" onClick={pagar} disabled={ocupado}>{ocupado ? 'Processando…' : 'Pagar'}</button>
        </div>
      </div>
    </div>
  )
}
