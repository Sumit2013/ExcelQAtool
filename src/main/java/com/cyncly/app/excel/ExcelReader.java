package com.cyncly.app.excel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyncly.app.model.Checkpoint;
import com.cyncly.app.model.QAProduct;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFRow headerRow;
    private final DataFormatter formatter = new DataFormatter();
    private String currentFilePath;

    private final Map<String, List<String>> checkpointOptionsByName = new HashMap<>();
    private final List<List<String>> checkpointOptionsByColIndex = new ArrayList<>();

    public void loadWorkbook(String filePath) throws IOException {
        this.currentFilePath = filePath;
        if (this.workbook != null) {
            try {
                this.workbook.close();
            } catch (Exception ignored) {}
        }

        try (FileInputStream file = new FileInputStream(filePath)) {
            this.workbook = new XSSFWorkbook(file);
        }

        this.sheet = workbook.getSheet("Validation");
        if (sheet == null) {
            throw new IllegalArgumentException("Sheet 'Validation' not found in " + filePath);
        }

        this.headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Header row (row 0) not found in 'Validation' sheet");
        }

        // Load pre-defined dropdown options from sheet "List" (starting at column H = index 7)
        XSSFSheet listSheet = workbook.getSheet("List");
        if (listSheet != null) {
            loadDropdownOptions(listSheet);
        } else {
            System.out.println("Warning: 'List' sheet not found. Defaulting to standard Ok/Need to confirm/NA options.");
        }
    }

    private void loadDropdownOptions(XSSFSheet listSheet) {
        checkpointOptionsByName.clear();
        checkpointOptionsByColIndex.clear();

        XSSFRow listHeader = listSheet.getRow(0);
        if (listHeader == null) {
            return;
        }

        int startCol = 7; // Column H is 0-indexed column 7
        int lastCol = listHeader.getLastCellNum();
        int maxRows = listSheet.getLastRowNum();

        for (int col = startCol; col < lastCol; col++) {
            String headerTitle = getCellValue(listHeader, col);
            if (headerTitle.isEmpty()) {
                continue;
            }

            List<String> options = new ArrayList<>();
            for (int r = 1; r <= maxRows; r++) {
                XSSFRow row = listSheet.getRow(r);
                if (row != null) {
                    String val = getCellValue(row, col);
                    if (!val.isEmpty()) {
                        options.add(val);
                    }
                }
            }

            if (options.isEmpty()) {
                options = Arrays.asList("Ok", "Need to confirm", "NA");
            }

            checkpointOptionsByColIndex.add(options);
            checkpointOptionsByName.put(normalizeKey(headerTitle), options);
        }

        System.out.println("Successfully loaded " + checkpointOptionsByColIndex.size() + " checkpoint dropdown categories from 'List' sheet.");
    }

    public QAProduct readProductAtRow(int rowIndex) {
        if (sheet == null) return null;
        XSSFRow productRow = sheet.getRow(rowIndex);
        if (productRow == null) return null;

        String type = getCellValue(productRow, 1);
        String subtype = getCellValue(productRow, 2);
        String description = getCellValue(productRow, 3);
        String sku = getCellValue(productRow, 5);

        if (sku.isEmpty() && description.isEmpty() && type.isEmpty()) {
            return null;
        }

        QAProduct product = new QAProduct(sku, type, subtype, description);
        product.setRowIndex(rowIndex);

        List<Checkpoint> qACheck = new ArrayList<>();
        int checkpointIndex = 0;
        for (int i = 12; i <= 40; i += 2) {
            String checkPointName = getCellValue(headerRow, i);
            if (checkPointName.isEmpty()) {
                continue;
            }
            String selectedValue = getCellValue(productRow, i);
            String comment = getCellValue(productRow, i + 1);

            List<String> values = getOptionsForCheckpoint(checkPointName, checkpointIndex);

            Checkpoint checkpoint = new Checkpoint(checkPointName, values);
            checkpoint.setSelectedValue(selectedValue);
            checkpoint.setComment(comment);
            qACheck.add(checkpoint);

            checkpointIndex++;
        }
        product.setCheckpoints(qACheck);

        // Read Bug ID & Description from Column AS (Index 44)
        String bugIdAndDesc = getCellValue(productRow, 44);
        product.setBugIdAndDescription(bugIdAndDesc);

        return product;
    }

    private List<String> getOptionsForCheckpoint(String checkPointName, int index) {
        String key = normalizeKey(checkPointName);
        if (checkpointOptionsByName.containsKey(key)) {
            return new ArrayList<>(checkpointOptionsByName.get(key));
        }
        if (index >= 0 && index < checkpointOptionsByColIndex.size()) {
            return new ArrayList<>(checkpointOptionsByColIndex.get(index));
        }
        return Arrays.asList("Ok", "Need to confirm", "NA");
    }

    private String normalizeKey(String key) {
        if (key == null) return "";
        return key.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    public void saveProductAtRow(int rowIndex, QAProduct product, String filePath) throws IOException {
        if (sheet == null) {
            throw new IllegalStateException("No Excel sheet loaded");
        }

        XSSFRow productRow = sheet.getRow(rowIndex);
        if (productRow == null) {
            productRow = sheet.createRow(rowIndex);
        }

        List<Checkpoint> checkpoints = product.getCheckpoints();
        if (checkpoints != null) {
            int colIndex = 12;
            for (Checkpoint cp : checkpoints) {
                if (colIndex > 40) {
                    break; // Never write past column 40 (AO) to protect AQ, AR, AS, AT
                }
                setCellValue(productRow, colIndex, cp.getSelectedValue());
                setCellValue(productRow, colIndex + 1, cp.getComment());
                colIndex += 2;
            }
        }

        // Save Bug ID & Description to Column AS (Index 44)
        setCellValue(productRow, 44, product.getBugIdAndDescription());

        // Save workbook to file
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            workbook.write(out);
        }
    }

    public int findNextProductRow(int startRowIndex) {
        if (sheet == null) return -1;
        int lastRow = sheet.getLastRowNum();
        for (int r = startRowIndex; r <= lastRow; r++) {
            XSSFRow row = sheet.getRow(r);
            if (row != null) {
                String sku = getCellValue(row, 5);
                if (!sku.isEmpty()) {
                    return r;
                }
            }
        }
        return -1;
    }

    public int findPreviousProductRow(int startRowIndex) {
        if (sheet == null) return -1;
        for (int r = startRowIndex; r >= 2; r--) {
            XSSFRow row = sheet.getRow(r);
            if (row != null) {
                String sku = getCellValue(row, 5);
                if (!sku.isEmpty()) {
                    return r;
                }
            }
        }
        return -1;
    }

    public QAProduct readExcel(String filePath) throws IOException {
        loadWorkbook(filePath);
        int firstRow = findNextProductRow(2);
        if (firstRow == -1) firstRow = 2;
        return readProductAtRow(firstRow);
    }

    public void close() {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getCellValue(XSSFRow row, int colIndex) {
        if (row == null) return "";
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private void setCellValue(XSSFRow row, int colIndex, String value) {
        if (row == null) return;
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        cell.setCellValue(value != null ? value : "");
    }

    public String getCurrentFilePath() {
        return currentFilePath;
    }
}