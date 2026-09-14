package service;

/** Thrown when business-rule or input validation fails before touching the database. */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
