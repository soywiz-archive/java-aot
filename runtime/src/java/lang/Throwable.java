package java.lang;

public class Throwable {
    String message;

    public Throwable() {
    }

    public Throwable(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void printStackTrace() {
        //System.out.println(message);
        System.out.println("StackTrace:" + message);
        //printStackTrace(System.err);
    }
}
