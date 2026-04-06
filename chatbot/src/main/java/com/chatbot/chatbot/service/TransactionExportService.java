package com.chatbot.chatbot.service;

import com.chatbot.chatbot.model.Customer;
import com.chatbot.chatbot.model.Transaction;
import com.chatbot.chatbot.repository.CustomerRepository;
import com.chatbot.chatbot.repository.TransactionRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionExportService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public byte[] exportToPdf(Long customerId,
                              LocalDateTime startDate,
                              LocalDateTime endDate) throws Exception {

        // Step 1 — Validate customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        // Step 2 — Fetch ALL transactions (no pagination for export)
        List<Transaction> transactions = transactionRepository
                .findByCustomerId(customerId, startDate, endDate, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        // Step 3 — Build PDF
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(document, out);
        document.open();

        // ── Fonts ─────────────────────────────────────────────────────
        Font titleFont   = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
        Font headerFont  = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        Font normalFont  = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, BaseColor.DARK_GRAY);
        Font subFont     = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
        Font amountFont  = new Font(Font.FontFamily.HELVETICA, 9,  Font.BOLD, BaseColor.DARK_GRAY);

        // ── Title ─────────────────────────────────────────────────────
        Paragraph title = new Paragraph("Transaction History", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4);
        document.add(title);

        // ── Subtitle ──────────────────────────────────────────────────
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        Paragraph subtitle = new Paragraph("Customer: " + customerName, subFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(4);
        document.add(subtitle);

        // ── Date range info ───────────────────────────────────────────
        String period = (startDate != null && endDate != null)
                ? startDate.format(FORMATTER) + " → " + endDate.format(FORMATTER)
                : "All time";
        Paragraph periodPara = new Paragraph("Period: " + period, subFont);
        periodPara.setAlignment(Element.ALIGN_CENTER);
        periodPara.setSpacingAfter(4);
        document.add(periodPara);

        // ── Generated at ──────────────────────────────────────────────
        Paragraph generated = new Paragraph(
                "Generated: " + LocalDateTime.now().format(FORMATTER), subFont);
        generated.setAlignment(Element.ALIGN_CENTER);
        generated.setSpacingAfter(16);
        document.add(generated);

        // ── Divider ───────────────────────────────────────────────────
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);

        // ── Table ─────────────────────────────────────────────────────
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.5f, 1.8f, 1.5f, 1.5f, 2f, 2f});
        table.setSpacingBefore(10);

        // Table headers
        String[] headers = {"Reference", "Type", "Status", "Amount", "From", "To"};
        BaseColor headerBg = new BaseColor(52, 73, 94);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerBg);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);
        }

        // Table rows
        boolean alternate = false;
        BaseColor altColor = new BaseColor(245, 245, 245);

        for (Transaction t : transactions) {
            BaseColor rowColor = alternate ? altColor : BaseColor.WHITE;
            alternate = !alternate;

            // Reference
            addCell(table, t.getReferenceNumber(), normalFont, rowColor, Element.ALIGN_LEFT);

            // Type — color coded
            Font typeFont = getTypeFont(t);
            addCell(table, t.getTransactionType().name(), typeFont, rowColor, Element.ALIGN_CENTER);

            // Status
            Font statusFont = getStatusFont(t);
            addCell(table, t.getStatus().name(), statusFont, rowColor, Element.ALIGN_CENTER);

            // Amount
            addCell(table,
                    String.format("%.2f", t.getAmount()),
                    amountFont, rowColor, Element.ALIGN_RIGHT);

            // From Account
            addCell(table,
                    t.getFromAccount() != null ? t.getFromAccount().getAccountNumber() : "-",
                    normalFont, rowColor, Element.ALIGN_CENTER);

            // To Account
            addCell(table,
                    t.getToAccount() != null ? t.getToAccount().getAccountNumber() : "-",
                    normalFont, rowColor, Element.ALIGN_CENTER);
        }

        document.add(table);

        // ── Summary footer ────────────────────────────────────────────
        document.add(Chunk.NEWLINE);
        LineSeparator line2 = new LineSeparator();
        line2.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(line2));
        document.add(Chunk.NEWLINE);

        Font summaryFont = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY);
        Paragraph footer = new Paragraph(
                "Total transactions: " + transactions.size() + "  •  " +
                        "This document was generated automatically by the Banking System.", summaryFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void addCell(PdfPTable table, String text,
                         Font font, BaseColor bg, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(6);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }

    private Font getTypeFont(Transaction t) {
        BaseColor color = switch (t.getTransactionType()) {
            case DEPOSIT    -> new BaseColor(39, 174, 96);   // green
            case WITHDRAWAL -> new BaseColor(192, 57, 43);   // red
            case TRANSFER   -> new BaseColor(41, 128, 185);  // blue
        };
        return new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, color);
    }

    private Font getStatusFont(Transaction t) {
        BaseColor color = switch (t.getStatus()) {
            case SUCCESS -> new BaseColor(39, 174, 96);   // green
            case FAILED  -> new BaseColor(192, 57, 43);   // red
            case PENDING -> new BaseColor(243, 156, 18);  // orange
        };
        return new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, color);
    }
}