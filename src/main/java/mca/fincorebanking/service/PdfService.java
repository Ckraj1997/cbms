package mca.fincorebanking.service;

import java.io.ByteArrayInputStream;
import java.util.List;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.Loan;
import mca.fincorebanking.entity.Transaction;

public interface PdfService {

    ByteArrayInputStream generateLoanSanctionLetter(Loan loan);

    ByteArrayInputStream generateTransactionReceipt(Transaction transaction);

    ByteArrayInputStream generateAccountStatement(Account account, List<Transaction> transactions);
}