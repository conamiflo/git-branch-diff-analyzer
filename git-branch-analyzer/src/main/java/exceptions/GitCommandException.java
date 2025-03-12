package exceptions;

public class GitCommandException extends GitException {
    public GitCommandException(String message) {
        super(message);
    }
    public GitCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}