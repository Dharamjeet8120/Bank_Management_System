package com.bank;

import com.bank.config.DatabaseConfig;
import com.bank.service.BankingService;

import java.math.BigDecimal;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		BankingService bankingService = new BankingService();
		Scanner scanner = new Scanner(System.in);

		System.out.println("Initializing Enterprise Bank Management System...");

		while (true) {
			System.out.println("\n===================================================================");
			System.out.println("                  ENTERPRISE BANK MANAGEMENT SYSTEM                ");
			System.out.println("===================================================================");
			System.out.println("[ 1] ── Create New Account");
			System.out.println("[ 2] ── Deposit Funds");
			System.out.println("[ 3] ── Withdraw Funds (PIN Required)");
			System.out.println("[ 4] ── Transfer Funds (ACID Transaction)");
			System.out.println("[ 5] ── View Transaction History (Passbook)");
			System.out.println("[ 6] ── Update Account Status (ACTIVE / FROZEN / CLOSED)");
			System.out.println("[ 7] ── Exit Application");
			System.out.println("===================================================================");
			System.out.print("Select an option (1-7): ");

			try {
				String input = scanner.nextLine().trim();
				int choice = Integer.parseInt(input);

				switch (choice) {
				case 1 -> {
					System.out.print("Enter Account Number: ");
					String accNum = scanner.nextLine().trim();
					System.out.print("Enter Account Holder Name: ");
					String name = scanner.nextLine().trim();
					System.out.print("Set 4-Digit Security PIN: ");
					String pin = scanner.nextLine().trim();
					System.out.print("Select Account Type (SAVINGS/CURRENT): ");
					String type = scanner.nextLine().trim();
					System.out.print("Enter Initial Deposit Amount: ");
					BigDecimal deposit = new BigDecimal(scanner.nextLine().trim());
					bankingService.createAccount(accNum, name, pin, type, deposit);
				}
				case 2 -> {
					System.out.print("Enter Account Number: ");
					String accNum = scanner.nextLine().trim();
					System.out.print("Enter Amount to Deposit: ");
					BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
					bankingService.deposit(accNum, amount);
				}
				case 3 -> {
					System.out.print("Enter Account Number: ");
					String accNum = scanner.nextLine().trim();
					System.out.print("Enter Security PIN: ");
					String pin = scanner.nextLine().trim();
					System.out.print("Enter Amount to Withdraw: ");
					BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
					bankingService.withdraw(accNum, pin, amount);
				}
				case 4 -> {
					System.out.print("Enter Sender Account Number: ");
					String sender = scanner.nextLine().trim();
					System.out.print("Enter Security PIN: ");
					String pin = scanner.nextLine().trim();
					System.out.print("Enter Receiver Account Number: ");
					String receiver = scanner.nextLine().trim();
					System.out.print("Enter Transfer Amount: ");
					BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
					bankingService.transferFunds(sender, pin, receiver, amount);
				}
				case 5 -> {
					System.out.print("Enter Account Number: ");
					String accNum = scanner.nextLine().trim();
					bankingService.viewTransactionHistory(accNum);
				}
				case 6 -> {
					System.out.print("Enter Account Number: ");
					String accNum = scanner.nextLine().trim();
					System.out.print("Enter New Status (ACTIVE / FROZEN / CLOSED): ");
					String status = scanner.nextLine().trim();
					bankingService.updateAccountStatus(accNum, status);
				}
				case 7 -> {
					System.out.println("\nClosing Database Connection Pool...");
					DatabaseConfig.closePool();
					System.out.println("Thank you for using Enterprise Bank Management System. Goodbye!");
					scanner.close();
					System.exit(0);
				}
				default -> System.out.println("❌ Invalid option. Please select a number between 1 and 7.");
				}
			} catch (NumberFormatException e) {
				System.err.println("❌ Error: Please enter a valid numeric choice or amount.");
			} catch (Exception e) {
				System.err.println("❌ Operation Error: " + e.getMessage());
			}
		}
	}
}