/** Visão do operador da loja: saldos do estoque-service, para acompanhar reservas e liberações ao vivo. */
export default function PainelEstoque({ itens, onRepor }) {
  return (
    <details className="cartao painel-estoque">
      <summary>Painel do operador · Estoque ({itens.length} SKUs)</summary>
      {itens.length === 0 && <p className="vazio">Estoque indisponível.</p>}
      <table>
        <thead><tr><th>SKU</th><th>Item</th><th>Saldo</th><th /></tr></thead>
        <tbody>
          {itens.map((i) => (
            <tr key={i.sku} className={i.quantidade <= 3 ? 'baixo' : ''}>
              <td><code>{i.sku}</code></td>
              <td>{i.descricao}</td>
              <td>{i.quantidade}</td>
              <td><button className="botao-link" onClick={() => onRepor(i.sku, i.quantidade + 5)}>+5</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </details>
  )
}
