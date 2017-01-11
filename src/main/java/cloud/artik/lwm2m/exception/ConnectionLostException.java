package cloud.artik.lwm2m.exception;

/**
 * Created by p.ayyavu on 1/11/17.
 */
public class ConnectionLostException extends Exception{
    public ConnectionLostException(String message) {
        super(message);
    }
}