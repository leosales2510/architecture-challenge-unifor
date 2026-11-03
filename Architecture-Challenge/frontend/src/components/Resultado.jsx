const TITULOS = {
  APROVADO: 'Pedido confirmado!',
  PAGAMENTO_RECUSADO: 'Pagamento recusado',
  ESTOQUE_INSUFICIENTE: 'Faltou estoque',
  DADOS_PAGAMENTO_INVALIDOS: 'Dados de pagamento inválidos',
  FALHA_INTEGRACAO: 'Serviço indisponível',
  PAGAMENTO_EM_VERIFICACAO: 'Pagamento em verificação',
}

/** Mostra o resultado do checkout e a trilha de eventos coordenada pelo Mediator no back-end. */
export default function Resultado({ resultado, onNovoPedido, onFechar }) {
  const { ok, pedido, mensagem, trilha = [], faltas = [] } = resultado
  return (
    <div className="fundo-modal" role="dialog" aria-modal="true" aria-labelledby="titulo-resultado">
      <div className={`modal resultado ${ok ? 'sucesso' : 'falha'}`}>
        <div className="icone-resultado" aria-hidden="true">{ok ? '✓' : '!'}</div>
        <h2 id="titulo-resultado">{TITULOS[resultado.resultado] || 'Não foi possível concluir'}</h2>
        {ok ? (
          <p>Pedido #{pedido.id} pago. Autorização <strong>{pedido.codigoAutorizacao}</strong>. Retire seu sorvete no balcão.</p>
        ) : (
          <p>{mensagem}</p>
        )}
        {faltas.length > 0 && (
          <ul className="faltas">{faltas.map((f) => <li key={f.sku}>{f.sku}: pedido {f.solicitado}, disponível {f.disponivel}</li>)}</ul>
        )}
        {trilha.length > 0 && (
          <div className="trilha">
            <h3>Trilha do checkout (Mediator)</h3>
            <ol>{trilha.map((passo, i) => <li key={i}>{passo.replaceAll('_', ' ').toLowerCase()}</li>)}</ol>
          </div>
        )}
        <div className="acoes-finais">
          {ok
            ? <button className="botao-principal" onClick={onNovoPedido}>Fazer novo pedido</button>
            : <button className="botao-principal" onClick={onFechar}>Voltar ao pedido</button>}
        </div>
      </div>
    </div>
  )
}
