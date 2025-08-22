package lion.mode.tradebot_backend.exception;

public class NotEnoughDataException extends RuntimeException{
    public NotEnoughDataException(String message) {
        super(message);
    }
}

// exception handling'de custom exception yazmak, hata loglarını okurken sysout çıktısından daha anlamlı sonuçlar verir