package freerails.nove;

public class Status {

    public static final Status OK = new Status(true, "");
    private final boolean success;
    private final String message;

    public Status(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
