package com.bank.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Account {
	private String accountNumber;
	private String accountHolder;
	private String pinHash;
	private String accountType;
	private BigDecimal balance;
	private String status;
	private Timestamp createdAt;

	public Account() {
	}

	public Account(String accountNumber, String accountHolder, String pinHash, String accountType, BigDecimal balance,
			String status) {
		this.accountNumber = accountNumber;
		this.accountHolder = accountHolder;
		this.pinHash = pinHash;
		this.accountType = accountType;
		this.balance = balance;
		this.status = status;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getAccountHolder() {
		return accountHolder;
	}

	public void setAccountHolder(String accountHolder) {
		this.accountHolder = accountHolder;
	}

	public String getPinHash() {
		return pinHash;
	}

	public void setPinHash(String pinHash) {
		this.pinHash = pinHash;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}
}