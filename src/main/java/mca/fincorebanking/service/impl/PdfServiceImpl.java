package mca.fincorebanking.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.Loan;
import mca.fincorebanking.entity.Transaction;
import mca.fincorebanking.service.PdfService;

@Service
public class PdfServiceImpl implements PdfService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

    @Override
    public ByteArrayInputStream generateLoanSanctionLetter(Loan loan) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, "LOAN SANCTION LETTER");

            document.add(new Paragraph("Date: " + java.time.LocalDate.now(), NORMAL_FONT));
            document.add(new Paragraph("\nTo,", NORMAL_FONT));
            document.add(new Paragraph("Mr./Ms. " + loan.getUser().getUsername(), NORMAL_FONT));
            document.add(new Paragraph("Email: " + loan.getUser().getEmail(), NORMAL_FONT));

            document.add(
                    new Paragraph("\nSubject: Sanction of Personal Loan Application #" + loan.getId(), SUBTITLE_FONT));
            document.add(new Paragraph("\nDear Customer,", NORMAL_FONT));
            document.add(new Paragraph(
                    "We are pleased to inform you that your loan application has been approved based on the following terms:",
                    NORMAL_FONT));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addTableRow(table, "Loan ID", String.valueOf(loan.getId()));
            addTableRow(table, "Loan Type", loan.getLoanType());
            addTableRow(table, "Sanctioned Amount", "Rs. " + loan.getAmount());
            addTableRow(table, "Interest Rate", loan.getInterestRate() + "% p.a.");
            addTableRow(table, "Tenure", loan.getTenureMonths() + " Months");

            document.add(table);

            document.add(new Paragraph(
                    "\n\nThis is a computer-generated document and does not require a physical signature.",
                    FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY)));

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream generateTransactionReceipt(Transaction transaction) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, "TRANSACTION RECEIPT");

            document.add(new Paragraph("Transaction ID: " + transaction.getId(), SUBTITLE_FONT));
            document.add(new Paragraph(
                    "Date: " + transaction.getTransactionTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    NORMAL_FONT));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addTableRow(table, "Account Number", transaction.getAccount().getAccountNumber());
            addTableRow(table, "Transaction Type", transaction.getType());
            addTableRow(table, "Amount", "Rs. " + transaction.getAmount());
            addTableRow(table, "Status", "SUCCESS");

            document.add(table);
            document.add(new Paragraph("\n\nThank you for banking with FinCore.", NORMAL_FONT));

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating Receipt", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream generateAccountStatement(Account account, List<Transaction> transactions) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, "ACCOUNT STATEMENT");

            document.add(new Paragraph("Account Number: " + account.getAccountNumber(), SUBTITLE_FONT));
            document.add(new Paragraph("Account Holder: " + account.getUser().getUsername(), NORMAL_FONT));
            document.add(new Paragraph("Current Balance: Rs. " + account.getBalance(), NORMAL_FONT));
            document.add(new Paragraph("\nTransaction History:", SUBTITLE_FONT));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new int[] { 3, 3, 3 });

            Stream.of("Date", "Type", "Amount").forEach(columnTitle -> {
                PdfPCell header = new PdfPCell();
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setBorderWidth(1);
                header.setPhrase(new Phrase(columnTitle, HEADER_FONT));
                table.addCell(header);
            });

            for (Transaction t : transactions) {
                table.addCell(t.getTransactionTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                table.addCell(t.getType());

                Font amtFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL,
                        (t.getType().equals("DEPOSIT") || t.getType().equals("CREDIT")) ? BaseColor.GREEN
                                : BaseColor.RED);

                table.addCell(new Phrase("Rs. " + t.getAmount(), amtFont));
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating Statement", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addHeader(Document document, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, TITLE_FONT);
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);
        document.add(new Paragraph("\n"));
        document.add(new com.itextpdf.text.pdf.draw.LineSeparator());
        document.add(new Paragraph("\n"));
    }

    private void addTableRow(PdfPTable table, String key, String value) {
        PdfPCell cellKey = new PdfPCell(new Phrase(key, HEADER_FONT));
        cellKey.setPadding(5);
        table.addCell(cellKey);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, NORMAL_FONT));
        cellValue.setPadding(5);
        table.addCell(cellValue);
    }
}