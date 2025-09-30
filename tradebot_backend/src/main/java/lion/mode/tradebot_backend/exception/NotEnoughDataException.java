package lion.mode.tradebot_backend.exception;

public class NotEnoughDataException extends RuntimeException{
    public NotEnoughDataException(String message) {
        super(message);
    }
}
