package com.assignment.lostandfound.reader;

import java.io.InputStream;

// This interface is used to read the file and process it.
// It is used to read and process data from different file types, such as PDF, CSV, etc.
public interface FileReader {
    int processFile(InputStream inputStream);
}
