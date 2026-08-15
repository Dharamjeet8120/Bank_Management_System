package com.bank.dao;

import com.bank.domain.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDao {

	public void save(Connection conn, Account account) throws SQLException {
		String sql = "INSERT INTO accounts (account_number, account_holder, pin_hash, account_type, balance, status) VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, account.getAccountNumber());
			pstmt.setString(2, account.getAccountHolder());
			pstmt.setString(3, account.getPinHash());
			pstmt.setString(4, account.getAccountType());
			pstmt.setBigDecimal(5, account.getBalance());
			pstmt.setString(6, account.getStatus());
			pstmt.executeUpdate();
		}
	}

	public Optional findByAccountNumber(Connection conn, String accountNumber) throws SQLException {
		String sql = "SELECT * FROM accounts WHERE account_number = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, accountNumber);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapResultSetToAccount(rs));
				}
			}
		}
		return Optional.empty();
	}

	public Optional findByAccountNumberForUpdate(Connection conn, String accountNumber) throws SQLException {
		String sql = "SELECT * FROM accounts WHERE account_number = ? FOR UPDATE";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, accountNumber);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapResultSetToAccount(rs));
				}
			}
		}
		return Optional.empty();
	}

	public boolean updateBalance(Connection conn, String accountNumber, BigDecimal newBalance) throws SQLException {
		String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setBigDecimal(1, newBalance);
			pstmt.setString(2, accountNumber);
			return pstmt.executeUpdate() > 0;
		}
	}

	public boolean updateStatus(Connection conn, String accountNumber, String status) throws SQLException {
		String sql = "UPDATE accounts SET status = ? WHERE account_number = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setString(2, accountNumber);
			return pstmt.executeUpdate() > 0;
		}
	}

	public void saveTransactionLog(Connection conn, String accountNumber, String type, BigDecimal amount,
			BigDecimal balanceAfter, String remark) throws SQLException {
		String sql = "INSERT INTO transaction_logs (account_number, type, amount, balance_after, remark) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, accountNumber);
			pstmt.setString(2, type);
			pstmt.setBigDecimal(3, amount);
			pstmt.setBigDecimal(4, balanceAfter);
			pstmt.setString(5, remark);
			pstmt.executeUpdate();
		}
	}

	public List getTransactionLogs(Connection conn, String accountNumber) throws SQLException {
		String sql = "SELECT transaction_id, type, amount, balance_after, remark, created_at FROM transaction_logs WHERE account_number = ? ORDER BY created_at DESC LIMIT 10";
		List logs = new ArrayList<>();
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, accountNumber);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					String log = String.format("%-8d | %-16s | ₹%-10.2f | ₹%-12.2f | %-20s | %s",
							rs.getLong("transaction_id"), rs.getString("type"), rs.getBigDecimal("amount"),
							rs.getBigDecimal("balance_after"), rs.getString("remark"), rs.getTimestamp("created_at"));
					logs.add(log);
				}
			}
		}
		return logs;
	}

	private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
		Account acc = new Account();
		acc.setAccountNumber(rs.getString("account_number"));
		acc.setAccountHolder(rs.getString("account_holder"));
		acc.setPinHash(rs.getString("pin_hash"));
		acc.setAccountType(rs.getString("account_type"));
		acc.setBalance(rs.getBigDecimal("balance"));
		acc.setStatus(rs.getString("status"));
		acc.setCreatedAt(rs.getTimestamp("created_at"));
		return acc;
	}
}