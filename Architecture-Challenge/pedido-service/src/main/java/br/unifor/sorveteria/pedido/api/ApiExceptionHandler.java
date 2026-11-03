package br.unifor.sorveteria.pedido.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.unifor.sorveteria.pedido.domain.exception.CheckoutNaoConcluidoException;
import br.unifor.sorveteria.pedido.domain.exception.CupomInvalidoException;
import br.unifor.sorveteria.pedido.domain.exception.EstoqueInsuficienteException;
import br.unifor.sorveteria.pedido.domain.exception.ItemInvalidoException;
import br.unifor.sorveteria.pedido.domain.exception.ItemNaoEncontradoException;
import br.unifor.sorveteria.pedido.domain.exception.NadaParaDesfazerException;
import br.unifor.sorveteria.pedido.domain.exception.PagamentoNaoConfirmadoException;
import br.unifor.sorveteria.pedido.domain.exception.PedidoNaoEncontradoException;
import br.unifor.sorveteria.pedido.domain.exception.RegraPedidoException;
import br.unifor.sorveteria.pedido.domain.exception.ServicoIndisponivelException;
import br.unifor.sorveteria.pedido.dto.CheckoutResponse;

/** Respostas de erro RFC 7807 com campo "codigo" (mesmo formato nos 4 serviços). */
@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({PedidoNaoEncontradoException.class, ItemNaoEncontradoException.class})
    ProblemDetail naoEncontrado(RuntimeException e) {
        return problema(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(RegraPedidoException.class)
    ProblemDetail regra(RegraPedidoException e) {
        return problema(HttpStatus.CONFLICT, "PEDIDO_NAO_EDITAVEL", e.getMessage());
    }

    @ExceptionHandler(NadaParaDesfazerException.class)
    ProblemDetail nadaParaDesfazer(NadaParaDesfazerException e) {
        return problema(HttpStatus.CONFLICT, "NADA_PARA_DESFAZER", e.getMessage());
    }

    @ExceptionHandler(CupomInvalidoException.class)
    ProblemDetail cupom(CupomInvalidoException e) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "CUPOM_INVALIDO", e.getMessage());
    }

    @ExceptionHandler(ItemInvalidoException.class)
    ProblemDetail item(ItemInvalidoException e) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "ITEM_INVALIDO", e.getMessage());
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    ProblemDetail estoque(EstoqueInsuficienteException e) {
        ProblemDetail p = problema(HttpStatus.CONFLICT, "ESTOQUE_INSUFICIENTE", e.getMessage());
        p.setProperty("faltas", e.getFaltas());
        return p;
    }

    @ExceptionHandler(PagamentoNaoConfirmadoException.class)
    ProblemDetail naoConfirmado(PagamentoNaoConfirmadoException e) {
        return problema(HttpStatus.CONFLICT, "PAGAMENTO_NAO_CONFIRMADO", e.getMessage());
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    ProblemDetail indisponivel(ServicoIndisponivelException e) {
        ProblemDetail p = problema(HttpStatus.SERVICE_UNAVAILABLE, "SERVICO_INDISPONIVEL", e.getMessage());
        p.setProperty("servico", e.getServico());
        return p;
    }

    /** Recusado -> 402, falta de estoque -> 409, dados -> 422, resultado incerto -> 502, integração -> 503. */
    @ExceptionHandler(CheckoutNaoConcluidoException.class)
    ProblemDetail checkout(CheckoutNaoConcluidoException e) {
        CheckoutResponse c = e.getCheckout();
        HttpStatus status = switch (c.resultado()) {
            case PAGAMENTO_RECUSADO -> HttpStatus.PAYMENT_REQUIRED;
            case ESTOQUE_INSUFICIENTE -> HttpStatus.CONFLICT;
            case DADOS_PAGAMENTO_INVALIDOS -> HttpStatus.UNPROCESSABLE_ENTITY;
            case PAGAMENTO_EM_VERIFICACAO -> HttpStatus.BAD_GATEWAY;
            case FALHA_INTEGRACAO, APROVADO -> HttpStatus.SERVICE_UNAVAILABLE;
        };
        ProblemDetail p = problema(status, c.resultado().name(), c.mensagem());
        p.setProperty("trilha", c.trilha());
        p.setProperty("faltas", c.faltas());
        p.setProperty("pedido", c.pedido());
        return p;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail concorrencia(OptimisticLockingFailureException e) {
        return problema(HttpStatus.CONFLICT, "CONFLITO_CONCORRENCIA", "O pedido foi alterado por outra operação; recarregue");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacao(MethodArgumentNotValidException e) {
        Map<String, String> erros = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> erros.put(f.getField(), f.getDefaultMessage()));
        ProblemDetail p = problema(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", "Requisição com campos inválidos");
        p.setProperty("erros", erros);
        return p;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail corpoInvalido(HttpMessageNotReadableException e) {
        return problema(HttpStatus.BAD_REQUEST, "CORPO_INVALIDO", "JSON malformado ou com valor desconhecido");
    }

    @ExceptionHandler(TypeMismatchException.class)
    ProblemDetail tipoInvalido(TypeMismatchException e) {
        return problema(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", "Parâmetro com formato inválido: " + e.getValue());
    }

    /** Último recurso: erros do próprio Spring (404 de rota, 405...) mantêm seu status; o resto vira 500. */
    @ExceptionHandler(Exception.class)
    ProblemDetail inesperado(Exception e) {
        if (e instanceof ErrorResponse resposta) {
            ProblemDetail p = resposta.getBody();
            p.setProperty("codigo", "REQUISICAO_INVALIDA");
            return p;
        }
        log.error("Erro inesperado", e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Erro inesperado no servidor; tente novamente");
    }

    private static ProblemDetail problema(HttpStatus status, String codigo, String detalhe) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detalhe);
        p.setTitle(status.getReasonPhrase());
        p.setProperty("codigo", codigo);
        return p;
    }
}
