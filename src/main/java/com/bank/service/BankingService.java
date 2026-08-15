package com.bank.service;

import com.bank.config.DatabaseConfig;
import com.bank.dao.AccountDao;
import com.bank.domain.Account;
import com.bank.exception.BankException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BankingService {

	private final AccountDao accountDao;

	public BankingService() {
		this.accountDao = new AccountDao();
	}

	public BankingService(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	public void createAccount(String accountNumber, String holderName, String pin, String accountType,
			BigDecimal initialDeposit) throws BankException, SQLException {
		if (initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
			throw new BankException("Initial deposit cannot be negative.");
		}

		try (Connection conn = DatabaseConfig.getConnection()) {
			conn.setAutoCommit(false);
			try {
				if (accountDao.findByAccountNumber(conn, accountNumber).isPresent()) {
					throw new BankException("Account number " + accountNumber + " already exists.");
				}

				Account account = new Account(accountNumber, holderName, pin, accountType.toUpperCase(), initialDeposit,
						"ACTIVE");
				accountDao.save(conn, account);
				accountDao.saveTransactionLog(conn, accountNumber, "DEPOSIT", initialDeposit, initialDeposit,
						"Initial Account Deposit");

				conn.commit();
				System.out.println(" SUCCESS: Account " + accountNumber + " created successfully.");
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		}
	}

	public void deposit(String accountNumber, BigDecimal amount) throws BankException {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BankException("Deposit amount must be greater than zero.");
		}

		try (Connection conn = DatabaseConfig.getConnection()) {
			conn.setAutoCommit(false);
			try {
				Account account = (Account) accountDao.findByAccountNumberForUpdate(conn, accountNumber).orElseThrow(
						() -> new BankException.AccountNotFoundException("Account " + accountNumber + " not found."));

				if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
					throw new BankException("Account status is " + account.getStatus() + ". Deposit denied.");
				}

				BigDecimal newBalance = account.getBalance().add(amount);
				accountDao.updateBalance(conn, accountNumber, newBalance);
				accountDao.saveTransactionLog(conn, accountNumber, "DEPOSIT", amount, newBalance, "Cash Deposit");

				conn.commit();
				System.out.println(" SUCCESS: Deposited ₹" + amount + " | New Balance: ₹" + newBalance);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} catch (Throwable e) {

		}
	}

	public void withdraw(String accountNumber, String pin, BigDecimal amount) throws BankException {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BankException("Withdrawal amount must be greater than zero.");
		}

		try (Connection conn = DatabaseConfig.getConnection()) {
			conn.setAutoCommit(false);
			try {
				Account account = (Account) accountDao.findByAccountNumberForUpdate(conn, accountNumber).orElseThrow(
						() -> new BankException.AccountNotFoundException("Account " + accountNumber + " not found."));

				validateAccountAndPin(account, pin);

				if (account.getBalance().compareTo(amount) < 0) {
					throw new BankException.InsufficientBalanceException(
							"Insufficient funds. Available: ₹" + account.getBalance());
				}

				BigDecimal newBalance = account.getBalance().subtract(amount);
				accountDao.updateBalance(conn, accountNumber, newBalance);
				accountDao.saveTransactionLog(conn, accountNumber, "WITHDRAWAL", amount, newBalance, "Cash Withdrawal");

				conn.commit();
				System.out.println(" SUCCESS: Withdrew ₹" + amount + " | Remaining Balance: ₹" + newBalance);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} catch (Throwable e) {

		}
	}

	public void transferFunds(String senderAccNum, String pin, String receiverAccNum, BigDecimal amount)
			throws BankException {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BankException("Transfer amount must be greater than zero.");
		}
		if (senderAccNum.equals(receiverAccNum)) {
			throw new BankException("Cannot transfer funds to the same account.");
		}

		try (Connection conn = DatabaseConfig.getConnection()) {
			conn.setAutoCommit(false);
			try {
				// Lock account IDs in alphabetical order to avoid deadlocks
				String firstLock = senderAccNum.compareTo(receiverAccNum) < 0 ? senderAccNum : receiverAccNum;
				String secondLock = senderAccNum.compareTo(receiverAccNum) < 0 ? receiverAccNum : senderAccNum;

				accountDao.findByAccountNumberForUpdate(conn, firstLock);
				accountDao.findByAccountNumberForUpdate(conn, secondLock);

				Account sender = (Account) accountDao.findByAccountNumber(conn, senderAccNum)
						.orElseThrow(() -> new BankException.AccountNotFoundException("Sender account not found."));

				Account receiver = (Account) accountDao.findByAccountNumber(conn, receiverAccNum)
						.orElseThrow(() -> new BankException.AccountNotFoundException("Receiver account not found."));

				validateAccountAndPin(sender, pin);

				if (!"ACTIVE".equalsIgnoreCase(receiver.getStatus())) {
					throw new BankException("Receiver account is " + receiver.getStatus() + ". Transfer aborted.");
				}

				if (sender.getBalance().compareTo(amount) < 0) {
					throw new BankException.InsufficientBalanceException("Insufficient funds in sender account.");
				}

				BigDecimal senderNewBal = sender.getBalance().subtract(amount);
				BigDecimal receiverNewBal = receiver.getBalance().add(amount);

				accountDao.updateBalance(conn, senderAccNum, senderNewBal);
				accountDao.updateBalance(conn, receiverAccNum, receiverNewBal);

				accountDao.saveTransactionLog(conn, senderAccNum, "TRANSFER_SENT", amount, senderNewBal,
						"Sent to " + receiverAccNum);
				accountDao.saveTransactionLog(conn, receiverAccNum, "TRANSFER_RECEIVED", amount, receiverNewBal,
						"Received from " + senderAccNum);

				conn.commit();
				System.out.println(" SUCCESS: Transferred ₹" + amount + " to Account: " + receiverAccNum);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} catch (Throwable e) {

		}
	}

	public void viewTransactionHistory(String accountNumber) {
		try (Connection conn = DatabaseConfig.getConnection()) {

			Account account = (Account) accountDao.findByAccountNumber(conn, accountNumber).orElseThrow(
					() -> new BankException.AccountNotFoundException("Account " + accountNumber + " not found."));

			List transactionLogs = accountDao.getTransactionLogs(conn, accountNumber);

			System.out.println(
					"\n-------------------------------------------------------------------------------------------------");
			System.out.println("PASSBOOK TRANSACTIONS FOR ACCOUNT: " + account.getAccountNumber() + " ("
					+ account.getAccountHolder() + ")");
			System.out.println(
					"-------------------------------------------------------------------------------------------------");
			System.out.printf("%-8s | %-16s | %-11s | %-13s | %-20s | %s%n", "TXN ID", "TYPE", "AMOUNT",
					"BALANCE AFTER", "REMARK", "DATE & TIME");
			System.out.println(
					"-------------------------------------------------------------------------------------------------");

			if (transactionLogs.isEmpty()) {
				System.out.println("No transactions found.");
			} else {
				transactionLogs.forEach(System.out::println);
			}

			System.out.println(
					"-------------------------------------------------------------------------------------------------");

		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {

		}
	}

	public void updateAccountStatus(String accountNumber, String newStatus) {
		try (Connection conn = DatabaseConfig.getConnection()) {
			Account account = (Account) accountDao.findByAccountNumber(conn, accountNumber).orElseThrow(
					() -> new BankException.AccountNotFoundException("Account " + accountNumber + " not found."));

			accountDao.updateStatus(conn, accountNumber, newStatus.toUpperCase());
			System.out.println(" SUCCESS: Account " + accountNumber + " status updated from " + account.getStatus()
					+ " to " + newStatus.toUpperCase());
		} catch (Throwable e) {

		}
	}

	private void validateAccountAndPin(Account account, String pin) throws BankException {
		if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
			throw new BankException("Account is " + account.getStatus() + ". Operation denied.");
		}
		if (!account.getPinHash().equals(pin)) {
			throw new BankException.AuthenticationException("Invalid Security PIN.");
		}
	}
}