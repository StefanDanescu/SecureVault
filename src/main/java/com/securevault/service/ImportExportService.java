package com.securevault.service;

import com.securevault.model.PasswordEntry;
import com.securevault.model.Vault;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for importing and exporting vault data.
 * 
 * Supports CSV format for compatibility with other password managers.
 */
public class ImportExportService {

    private static final String CSV_HEADER = "title,username,password,url,notes";

    /**
     * Exports vault entries to a CSV file.
     * 
     * WARNING: The exported file contains plaintext passwords!
     * 
     * @param vault The vault to export
     * @param outputPath The path to save the CSV file
     * @throws IOException if export fails
     */
    public void exportToCsv(Vault vault, Path outputPath) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append(CSV_HEADER).append("\n");

        for (PasswordEntry entry : vault.getEntries()) {
            csv.append(escapeCsv(entry.getTitle())).append(",");
            csv.append(escapeCsv(entry.getUsername())).append(",");
            csv.append(escapeCsv(entry.getPassword())).append(",");
            csv.append(escapeCsv(entry.getUrl())).append(",");
            csv.append(escapeCsv(entry.getNotes())).append("\n");
        }

        Files.writeString(outputPath, csv.toString(), StandardCharsets.UTF_8);
    }

    /**
     * Imports entries from a CSV file.
     * 
     * @param csvPath The path to the CSV file
     * @return List of imported password entries
     * @throws IOException if import fails
     */
    public List<PasswordEntry> importFromCsv(Path csvPath) throws IOException {
        List<PasswordEntry> entries = new ArrayList<>();
        List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);

        // Skip header if present
        int startIndex = 0;
        if (!lines.isEmpty() && lines.get(0).toLowerCase().contains("title")) {
            startIndex = 1;
        }

        for (int i = startIndex; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] fields = parseCsvLine(line);
            if (fields.length >= 3) {
                PasswordEntry entry = new PasswordEntry();
                entry.setTitle(fields.length > 0 ? fields[0] : "");
                entry.setUsername(fields.length > 1 ? fields[1] : "");
                entry.setPassword(fields.length > 2 ? fields[2] : "");
                entry.setUrl(fields.length > 3 ? fields[3] : "");
                entry.setNotes(fields.length > 4 ? fields[4] : "");
                entries.add(entry);
            }
        }

        return entries;
    }

    /**
     * Escapes a string for CSV format.
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Parses a CSV line respecting quoted fields.
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());

        return fields.toArray(new String[0]);
    }
}
