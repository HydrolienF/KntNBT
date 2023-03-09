package com.jkantrell;

@FunctionalInterface
public interface ExceptionSupplier<T, E extends Exception> {

	T run() throws E;
}
