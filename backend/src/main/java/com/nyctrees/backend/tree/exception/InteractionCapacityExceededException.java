package com.nyctrees.backend.tree.exception;

/**
 * Thrown when the in-memory interaction store has reached its configured
 * capacity limit, used to protect low-tier demo deployments from unbounded
 * memory growth.
 */
public class InteractionCapacityExceededException extends RuntimeException {
    public InteractionCapacityExceededException(String message) {
        super(message);
    }
}
