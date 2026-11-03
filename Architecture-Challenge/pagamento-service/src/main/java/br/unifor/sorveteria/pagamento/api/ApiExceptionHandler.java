package br.unifor.sorveteria.pagamento.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.unifor.sorveteria.pagamento.domain.exception.DadosPagamentoInvalidosException;
import br.unifor.sorveteria.pagamento.domain.exception.GatewayIndisponivelException;
import br.unifor.sorveteria.pagamento.domain.exception.RecursoNaoEncontradoException;

/** Respostas de erro RFC 7807 com campo "codigo" (mesmo formato nos 4 serviços). */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DadosPagamentoInvalidosException.class)
    ProblemDetail dadosInvalidos(DadosPagamentoInvalidosException e) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "DADOS_PAGAMENTO_INVALIDOS", e.getMessage());
    }

    @ExceptionHandler(GatewayIndisponivelException.class)
    ProblemDetail gatewayIndisponivel(GatewayIndisponivelException e) {
        return problema(HttpStatus.SERVICE_UNAVAILABLE, "GATEWAY_INDISPONIVEL", e.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacao(MethodArgumentNotValidException e) {
        Map<String, String> erros = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> erros.put(f.getField(), f.getDefaultMessage()));
        ProblemDetail p = problema(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", "Requisição com campos inválidos");
        p.setProperty("erros", erros);
        return p;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    ProblemDetail corpoInvalido(Exception e) {
        return problema(HttpStatus.BAD_REQUEST, "CORPO_INVALIDO", "Requisição malformada ou com valor desconhecido");
    }

    private static ProblemDetail problema(HttpStatus status, String codigo, String detalhe) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detalhe);
        p.setTitle(status.getReasonPhrase());
        p.setProperty("codigo", codigo);
        return p;
    }
}
