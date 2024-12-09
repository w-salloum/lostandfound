package com.assignment.lostandfound.reader;

import com.assignment.lostandfound.dto.LostItemDto;
import com.assignment.lostandfound.exception.InitializingPDFException;
import com.assignment.lostandfound.exception.InvalidPDFContentException;
import com.assignment.lostandfound.service.LostItemService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;

public class PDFFileReader implements FileReader {

    private static final String ITEM_NAME_PREFIX = "ItemName:";
    private static final String QUANTITY_PREFIX = "Quantity:";
    private static final String PLACE_PREFIX = "Place:";

    private final LostItemService lostItemService;
    private final PDFTextStripper pdfStripper;

    public PDFFileReader(LostItemService lostItemService) {
        this.lostItemService = lostItemService;
        try {
            this.pdfStripper = new PDFTextStripper(); // Initialize once to reuse across pages
        } catch (IOException e) {
            throw new InitializingPDFException("Error initializing PDF reader: " + e.getMessage());
        }

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
        int totalPages = document.getNumberOfPages();

        try {
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
                if (line.startsWith(ITEM_NAME_PREFIX)) {
                    LostItemDto lostItem = parseItem(lineReader, line);
                    lostItemService.saveLostItem(lostItem);
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
            String name = extractField(itemLine, ITEM_NAME_PREFIX);
            String quantityLine = lineReader.readLine();
            String placeLine = lineReader.readLine();

            if (quantityLine == null || placeLine == null) {
                throw new InvalidPDFContentException("Incomplete item details in PDF.");
            }

            int quantity = Integer.parseInt(extractField(quantityLine, QUANTITY_PREFIX));
            String place = extractField(placeLine, PLACE_PREFIX);

            return LostItemDto.builder()
                    .name(name)
                    .quantity(quantity)
                    .place(place)
                    .build();
        } catch (IOException | NumberFormatException e) {
            throw new InvalidPDFContentException("Invalid item format in PDF: " + e.getMessage());
        }
    }

    private String extractField(String line, String prefix) {
        if (line.startsWith(prefix)) {
            return line.substring(prefix.length()).trim();
        }
        throw new InvalidPDFContentException("Invalid format, missing prefix: " + prefix);
    }
}
