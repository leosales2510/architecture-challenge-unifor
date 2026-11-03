import { useEffect, useState } from 'react'
import { api, reais } from '../api.js'

/**
 * Monta um sorvete. A pré-visualização (descrição + preço) é calculada pelo
 * catalogo-service a cada escolha — inclusive as regras do Builder (máximo de
 * coberturas, sabor da mesma linha), que aparecem aqui como mensagem de erro.
 */
export default function Montador({ catalogo, desabilitado, onAdicionar }) {
  const [linha, setLinha] = useState(catalogo.linhas[0].codigo)
  const [sabor, setSabor] = useState(null)
  const [coberturas, setCoberturas] = useState([])
  const [quantidade, setQuantidade] = useState(1)
  const [previa, setPrevia] = useState(null)
  const [erroPrevia, setErroPrevia] = useState(null)

  const linhaAtual = catalogo.linhas.find((l) => l.codigo === linha)

  useEffect(() => {
    if (!sabor) { setPrevia(null); setErroPrevia(null); return undefined }
    let ativo = true
    api.montar(linha, sabor, coberturas)
      .then((m) => { if (ativo) { setPrevia(m); setErroPrevia(null) } })
      .catch((e) => { if (ativo) { setPrevia(null); setErroPrevia(e.message) } })
    return () => { ativo = false }
  }, [linha, sabor, coberturas])

  function escolherLinha(codigo) {
    setLinha(codigo)
    setSabor(null)
  }

  function alternarCobertura(sku) {
    setCoberturas((atual) => (atual.includes(sku) ? atual.filter((c) => c !== sku) : [...atual, sku]))
  }

  function adicionar() {
    onAdicionar({ linha, saborSku: sabor, coberturas, quantidade })
    setCoberturas([])
    setQuantidade(1)
  }

  return (
    <section className="cartao montador">
      <h2>Monte seu sorvete</h2>

      <h3><span className="passo">1</span> Linha</h3>
      <div className="linhas">
        {catalogo.linhas.map((l) => (
          <button key={l.codigo} className={`opcao-linha ${l.codigo === linha ? 'ativa' : ''}`}
                  onClick={() => escolherLinha(l.codigo)} disabled={desabilitado}>
            <strong>{l.nome}</strong>
            <small>a partir de {reais(Number(l.precoBase) + Number(l.casquinha.preco))}</small>
            <small>{l.casquinha.nome}</small>
          </button>
        ))}
      </div>

      <h3><span className="passo">2</span> Sabor</h3>
      <div className="chips">
        {linhaAtual.sabores.map((s) => (
          <button key={s.sku} className={`chip ${s.sku === sabor ? 'ativo' : ''}`}
                  onClick={() => setSabor(s.sku)} disabled={desabilitado}>{s.nome}</button>
        ))}
      </div>

      <h3><span className="passo">3</span> Coberturas <small>(até {catalogo.maxCoberturas})</small></h3>
      <div className="chips">
        {catalogo.coberturas.map((c) => (
          <button key={c.sku} className={`chip ${coberturas.includes(c.sku) ? 'ativo' : ''}`}
                  onClick={() => alternarCobertura(c.sku)} disabled={desabilitado}>
            {c.nome} <span className="preco-chip">+{reais(c.preco)}</span>
          </button>
        ))}
      </div>

      <h3><span className="passo">4</span> Quantidade</h3>
      <div className="quantidade">
        <button onClick={() => setQuantidade((q) => Math.max(1, q - 1))} disabled={desabilitado} aria-label="Diminuir">−</button>
        <span>{quantidade}</span>
        <button onClick={() => setQuantidade((q) => Math.min(10, q + 1))} disabled={desabilitado} aria-label="Aumentar">+</button>
      </div>

      <div className={`previa ${erroPrevia ? 'previa-erro' : ''}`}>
        {!sabor && <p>Escolha um sabor para ver o preço.</p>}
        {erroPrevia && <p>⚠ {erroPrevia}</p>}
        {previa && (
          <>
            <p>{previa.descricao}</p>
            <strong>{reais(previa.precoUnitario)} cada</strong>
          </>
        )}
      </div>

      <button className="botao-principal" onClick={adicionar} disabled={desabilitado || !previa}>
        Adicionar ao pedido
      </button>
    </section>
  )
}
