package com.example.deustobank.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import com.example.deustobank.model.Account;
import com.example.deustobank.model.Transaction;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    public byte[] generateStatement(Account account, List<Transaction> transactions) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            fontTitle.setSize(18);
            Paragraph title = new Paragraph("Extracto de Cuenta - DeustoBank", fontTitle);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            
            document.add(new Paragraph(" "));

            Font fontContent = FontFactory.getFont(FontFactory.HELVETICA);
            fontContent.setSize(12);
            document.add(new Paragraph("Titular: " + account.getOwnerName(), fontContent));
            document.add(new Paragraph("Cuenta ID: " + account.getId(), fontContent));
            document.add(new Paragraph("Saldo Actual: " + String.format("%.2f", account.getBalance()) + " €", fontContent));
            
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 2.0f, 2.0f, 2.0f, 2.0f });

            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            fontHeader.setColor(Color.WHITE);

            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(new Color(26, 60, 94));
            cell.setPadding(5);

            cell.setPhrase(new Phrase("Fecha", fontHeader));
            table.addCell(cell);
            
            cell.setPhrase(new Phrase("Tipo", fontHeader));
            table.addCell(cell);
            
            cell.setPhrase(new Phrase("Importe", fontHeader));
            table.addCell(cell);

            cell.setPhrase(new Phrase("Saldo Final", fontHeader));
            table.addCell(cell);

            for (Transaction t : transactions) {
                table.addCell(String.valueOf(t.getDate()));
                table.addCell(t.getType());
                table.addCell(String.format("%.2f €", t.getAmount()));
                table.addCell(String.format("%.2f €", t.getBalanceAfter()));
            }

            document.add(table);

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}
