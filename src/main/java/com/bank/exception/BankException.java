package com.bank.exception;

public class BankException extends Exception {

	public BankException(String message) {
		super(message);
	}

	public static class InsufficientBalanceException extends BankException {
		public InsufficientBalanceException(String message) {
			super(message);
		}
	}

	public static class AccountNotFoundException extends BankException {
		public AccountNotFoundException(String message) {
			super(message);
		}
	}

	public static class AuthenticationException extends BankException {
		public AuthenticationException(String message) {
			super(message);
		}
	}
}