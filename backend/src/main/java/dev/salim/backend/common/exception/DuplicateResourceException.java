package dev.salim.backend.common.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resource, String identifier) {
        super(resource + " with identifier " + identifier + " already exists");
    }
}
