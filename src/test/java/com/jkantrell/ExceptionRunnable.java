package com.jkantrell;

@FunctionalInterface
public interface ExceptionRunnable<E extends Exception> {

	void run() throws E;
}
