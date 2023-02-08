package exception;


public class OrderDoesNotExistException extends IllegalArgumentException {

    public OrderDoesNotExistException(String message) {
        super(message);
    }
}
