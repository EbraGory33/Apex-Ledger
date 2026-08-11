package com.apexledger.shared.web;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.apexledger.account.application.AccountNotFoundException;
import com.apexledger.ledger.application.JournalEntryNotFoundException;
import com.apexledger.ledger.application.LedgerAccountNotFoundException;
import com.apexledger.ledger.domain.journal.JournalEntryNotBalancedException;
import com.apexledger.wallet.application.DuplicateWalletLabelException;
import com.apexledger.wallet.application.InactiveAccountException;
import com.apexledger.wallet.application.WalletNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @SuppressWarnings("unused")
    ErrorResponse validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Request validation failed");
        return new ErrorResponse("VALIDATION_ERROR", message, traceId(request));
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @SuppressWarnings("unused")
    ErrorResponse malformedRequest(Exception exception, HttpServletRequest request) {
        return new ErrorResponse("VALIDATION_ERROR", "Request validation failed", traceId(request));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @SuppressWarnings("unused")
    ErrorResponse invalidIdentifier(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        if ("walletId".equals(exception.getName())) {
            return new ErrorResponse("INVALID_WALLET_ID", "walletId must be a valid UUID", traceId(request));
        }
        if ("journalEntryId".equals(exception.getName())) {
            return new ErrorResponse("INVALID_JOURNAL_ENTRY_ID", "journalEntryId must be a valid UUID", traceId(request));
        }
        return new ErrorResponse("INVALID_ACCOUNT_ID", "accountId must be a valid UUID", traceId(request));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @SuppressWarnings("unused")
    ErrorResponse accountNotFound(AccountNotFoundException exception, HttpServletRequest request) {
        return new ErrorResponse("ACCOUNT_NOT_FOUND", "Account does not exist", traceId(request));
    }

    @ExceptionHandler(WalletNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @SuppressWarnings("unused")
    ErrorResponse walletNotFound(WalletNotFoundException exception, HttpServletRequest request) {
        return new ErrorResponse("WALLET_NOT_FOUND", "Wallet does not exist", traceId(request));
    }

    @ExceptionHandler(InactiveAccountException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @SuppressWarnings("unused")
    ErrorResponse inactiveAccount(InactiveAccountException exception, HttpServletRequest request) {
        return new ErrorResponse("ACCOUNT_NOT_ACTIVE", "Account must be active to create a wallet", traceId(request));
    }

    @ExceptionHandler(DuplicateWalletLabelException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @SuppressWarnings("unused")
    ErrorResponse duplicateWalletLabel(DuplicateWalletLabelException exception, HttpServletRequest request) {
        return new ErrorResponse("WALLET_LABEL_CONFLICT", "Wallet label already exists for this account", traceId(request));
    }

    @ExceptionHandler(LedgerAccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @SuppressWarnings("unused")
    ErrorResponse ledgerAccountNotFound(LedgerAccountNotFoundException exception, HttpServletRequest request) {
        return new ErrorResponse("LEDGER_ACCOUNT_NOT_FOUND", "Ledger account does not exist", traceId(request));
    }

    @ExceptionHandler(JournalEntryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @SuppressWarnings("unused")
    ErrorResponse journalNotFound(JournalEntryNotFoundException exception, HttpServletRequest request) {
        return new ErrorResponse("JOURNAL_ENTRY_NOT_FOUND", "Journal entry does not exist", traceId(request));
    }

    @ExceptionHandler(JournalEntryNotBalancedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    @SuppressWarnings("unused")
    ErrorResponse journalNotBalanced(JournalEntryNotBalancedException exception, HttpServletRequest request) {
        return new ErrorResponse("JOURNAL_ENTRY_NOT_BALANCED", exception.getMessage(), traceId(request));
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @SuppressWarnings("unused")
    ErrorResponse invalidJournalTransition(IllegalStateException exception, HttpServletRequest request) {
        return new ErrorResponse("JOURNAL_ENTRY_NOT_REVERSIBLE", "Journal entry cannot perform this transition", traceId(request));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @SuppressWarnings("unused")
    ErrorResponse internal(Exception exception, HttpServletRequest request) {
        return new ErrorResponse("INTERNAL_ERROR", "Unexpected failure", traceId(request));
    }

    private String traceId(HttpServletRequest request) {
        return Objects.toString(request.getHeader("X-Trace-Id"), null);
    }
}
