package com.fairchild.dbcp;

import java.io.*;

public class JDBCPoolException extends Exception {
	protected Throwable cause;

	public JDBCPoolException() {
		super("--------(Error occurred)--------");
	}

	public JDBCPoolException(String message) {
		super(message);
	}

	public JDBCPoolException(String message, Throwable cause) {
		super(message);
		this.cause = cause;
	}

	// Created to match the JDK 1.4 Throwable method.
	public Throwable initCause(Throwable cause) {
		this.cause = cause;
		return cause;
	}

	public String getMessage() {
		String msg = super.getMessage();
		Throwable parent = this;
		Throwable child;
		while ((child = getNestedException(parent)) != null) {
			String msg2 = child.getMessage();
			if (msg2 != null) {
				if (msg != null) {
					msg += " : " + msg2;
				} else {
					msg = msg2;
				}
			}
			// Any nested ApplicationException will append its own
			// children, so we need to break out of here.
			if (child instanceof JDBCPoolException) {
				break;
			}
			parent = child;
		}
		return msg;
	}

	public void printStackTrace() {
		super.printStackTrace();
		Throwable parent = this;
		Throwable child;
		while ((child = getNestedException(parent)) != null) {
			if (child != null) {
				System.err.print("--------(Caused by)--------");
				child.printStackTrace();
				if (child instanceof JDBCPoolException) {
					break;
				}
				parent = child;
			}
		}
	}

	public void printStackTrace(PrintStream s) {
		super.printStackTrace(s);
		Throwable parent = this;
		Throwable child;
		while ((child = getNestedException(parent)) != null) {
			if (child != null) {
				s.print("--------(Caused by)--------");
				child.printStackTrace(s);
				if (child instanceof JDBCPoolException) {
					break;
				}
				parent = child;
			}
		}
	}

	public void printStackTrace(PrintWriter w) {
		super.printStackTrace(w);
		Throwable parent = this;
		Throwable child;
		while ((child = getNestedException(parent)) != null) {
			if (child != null) {
				w.print("--------(Caused by)--------");
				child.printStackTrace(w);
				if (child instanceof JDBCPoolException) {
					break;
				}
				parent = child;
			}
		}
	}

	public Throwable getCause() {
		return cause;
	}

	public Throwable getNestedException(Throwable throwable) {
		return throwable.getCause();
	}
}
