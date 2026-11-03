package br.unifor.sorveteria.estoque.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.unifor.sorveteria.estoque.domain.exception.EstoqueInsuficienteException;
import br.unifor.sorveteria.estoque.domain.exception.OperacaoInvalidaException;
import br.unifor.sorveteria.estoque.domain.exception.RecursoNaoEncontradoException;

/** Respostas de erro RFC 7807 com campo "codigo" (mesmo formato nos 4 serviços). */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EstoqueInsuficienteException.class)
    ProblemDetail insuficiente(EstoqueInsuficienteException e) {
        ProblemDetail p = problema(HttpStatus.CONFLICT, "ESTOQUE_INSUFICIENTE", e.getMessage());
        p.setProperty("faltas", e.getFaltas());
        return p;
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(OperacaoInvalidaException.class)
    ProblemDetail invalida(OperacaoInvalidaException e) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "OPERACAO_INVALIDA", e.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail concorrencia(OptimisticLockingFailureException e) {
        return problema(HttpStatus.CONFLICT, "CONFLITO_CONCORRENCIA", "O saldo foi alterado por outra operação; tente novamente");
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

    private static ProblemDetail problema(HttpStatus status, String codigo, String detalhe) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detalhe);
        p.setTitle(status.getReasonPhrase());
        p.setProperty("codigo", codigo);
        return p;
    }
}
