package exceptions;

public class GitAPIException extends GitException {
    public GitAPIException(String message) {
        super(message);
    }
    public GitAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}