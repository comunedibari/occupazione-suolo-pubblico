package it.fincons.osp.exceptions;

import java.util.Objects;

import javax.validation.ValidationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	public static final String TRACE = "trace";

	@Value("${reflectoring.trace:false}")
	private boolean printStackTrace;

	@ExceptionHandler(BadCredentialsException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<Object> handleBadCredentialException(BadCredentialsException badCredentialsException,
			WebRequest request) {
		log.error("Crdenziali non valide", badCredentialsException);
		return buildErrorResponse(badCredentialsException, "Username e/o password errati", ErrorCode.E25,
				HttpStatus.UNAUTHORIZED, request);
	}

	@ExceptionHandler(DisabledException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<Object> handleDisabledException(DisabledException disabledException, WebRequest request) {
		log.error("L'utente è disabilitato", disabledException);
		return buildErrorResponse(disabledException, "L'utente è disabilitato", ErrorCode.E26, HttpStatus.UNAUTHORIZED,
				request);
	}

	@ExceptionHandler(AccountExpiredException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<Object> handleAccountExpiredException(AccountExpiredException accountExpiredException,
			WebRequest request) {
		log.error("L'account è stato eliminato", accountExpiredException);
		return buildErrorResponse(accountExpiredException, "L'utente non è attivo o è stato cancellato", ErrorCode.E27,
				HttpStatus.UNAUTHORIZED, request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException accessDeniedException,
			WebRequest request) {
		log.error("Accesso negato", accessDeniedException);
		return buildErrorResponse(accessDeniedException, "Accesso negato", ErrorCode.E28, HttpStatus.UNAUTHORIZED,
				request);
	}

	@ExceptionHandler(ValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Object> handleValidationException(ValidationException validationException,
			WebRequest request) {
		log.error("Errore di validazione", validationException);
		return buildErrorResponse(validationException, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		log.error("Errore di validazione dati in input", ex);
		return buildErrorResponse(ex, "Dati non validi nella richiesta", HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Object> handleBusinessException(BusinessException businessException, WebRequest request) {
		log.error("Errore di business", businessException);

		if (businessException.getCode() == ErrorCode.E10) {
			return buildBusinessErrorResponse(businessException, businessException.getMessage(), HttpStatus.CONFLICT,
					request);
		}

		return buildBusinessErrorResponse(businessException, businessException.getMessage(), HttpStatus.BAD_REQUEST,
				request);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<Object> handleAllUncaughtException(Exception exception, WebRequest request) {
		log.error("Unknown error occurred", exception);
		return buildErrorResponse(exception, "Errore generico", HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

	private ResponseEntity<Object> buildErrorResponse(Exception exception, HttpStatus httpStatus, WebRequest request) {
		return buildErrorResponse(exception, exception.getMessage(), httpStatus, request);
	}

	private ResponseEntity<Object> buildErrorResponse(Exception exception, String message, HttpStatus httpStatus,
			WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(null, message);
		if (printStackTrace && isTraceOn(request)) {
			errorResponse.setStackTrace(ExceptionUtils.getStackTrace(exception));
		}
		return ResponseEntity.status(httpStatus).body(errorResponse);
	}

	private ResponseEntity<Object> buildErrorResponse(Exception exception, String message, ErrorCode code,
			HttpStatus httpStatus, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(code, message);
		if (printStackTrace && isTraceOn(request)) {
			errorResponse.setStackTrace(ExceptionUtils.getStackTrace(exception));
		}
		return ResponseEntity.status(httpStatus).body(errorResponse);
	}

	private ResponseEntity<Object> buildBusinessErrorResponse(BusinessException exception, String message,
			HttpStatus httpStatus, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse(exception.getCode(), message);
		if (printStackTrace && isTraceOn(request)) {
			errorResponse.setStackTrace(ExceptionUtils.getStackTrace(exception));
		}
		return ResponseEntity.status(httpStatus).body(errorResponse);
	}

	private boolean isTraceOn(WebRequest request) {
		String[] value = request.getParameterValues(TRACE);
		return Objects.nonNull(value) && value.length > 0 && value[0].contentEquals("true");
	}

	@Override
	public ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		return buildErrorResponse(ex, status, request);
	}
}
