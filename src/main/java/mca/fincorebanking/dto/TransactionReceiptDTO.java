package mca.fincorebanking.dto;

import mca.fincorebanking.entity.Transaction;

public record TransactionReceiptDTO(Transaction debitTransaction, Transaction creditTransaction) {

}
