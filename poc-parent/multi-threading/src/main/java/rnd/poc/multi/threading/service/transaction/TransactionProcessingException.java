package rnd.poc.multi.threading.service.transaction;

public class TransactionProcessingException extends RuntimeException {

    public TransactionProcessingException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
