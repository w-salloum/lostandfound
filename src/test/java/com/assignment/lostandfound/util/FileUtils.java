package com.assignment.lostandfound.util;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.*;

public class FileUtils {

    public static InputStream getPdfInputStream(String filePath) throws IOException {
        PDDocument document = PDDocument.load(new File(FileUtils.class.getClassLoader().getResource(filePath).getFile()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
