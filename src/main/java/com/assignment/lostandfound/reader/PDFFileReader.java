package com.assignment.lostandfound.reader;

import com.assignment.lostandfound.dto.LostItemDto;
import com.assignment.lostandfound.exception.InvalidPDFContentException;
import com.assignment.lostandfound.service.LostItemService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;

public class PDFFileReader implements FileReader {
    private final LostItemService lostItemService;

    public PDFFileReader(LostItemService lostItemService) {
        this.lostItemService = lostItemService;
    }
    @Override
    public int processFile(InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            return processPdf(document);
        } catch (IOException e) {
            throw new InvalidPDFContentException("Error reading PDF: " + e.getMessage());
        }
    }

    private int processPdf(PDDocument document) {
        int totalItems = 0;

        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int page = 1; page <= totalPages; page++) {
                pdfStripper.setStartPage(page);
                pdfStripper.setEndPage(page);

                String pageContent = pdfStripper.getText(document);
                totalItems += processPageContent(pageContent);
            }
        } catch (IOException e) {
            throw new InvalidPDFContentException("Error processing PDF document: " + e.getMessage());
        }

        return totalItems;
    }

    private int processPageContent(String content) {
        int totalItems = 0;

        try (LineNumberReader lineReader = new LineNumberReader(new StringReader(content))) {
            String line;
            while ((line = lineReader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("ItemName:")) {
                    LostItemDto lostItem = parseItem(lineReader, line);
                    this.lostItemService.saveLostItem(lostItem);
                    totalItems++;
                }
            }
        } catch (IOException e) {
            throw new InvalidPDFContentException("Error processing PDF content: " + e.getMessage());
        }

        return totalItems;
    }

    private LostItemDto parseItem(LineNumberReader lineReader, String itemLine) {
        try {
            String name = itemLine.substring("ItemName:".length()).trim();
            String quantityLine = lineReader.readLine();
            String placeLine = lineReader.readLine();

            if (quantityLine == null || placeLine == null) {
                throw new InvalidPDFContentException("Incomplete item details in PDF.");
            }

            int quantity = Integer.parseInt(quantityLine.substring("Quantity:".length()).trim());
            String place = placeLine.substring("Place:".length()).trim();

            return LostItemDto.builder()
                    .name(name)
                    .quantity(quantity)
                    .place(place)
                    .build();
        } catch (IOException | NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new InvalidPDFContentException("Invalid item format in PDF: " + e.getMessage());
        }
    }

}
