package com.cyncly.app.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.cyncly.app.excel.ExcelReader;
import com.cyncly.app.model.QAProduct;

public class QAservice {

    private static final String PROGRESS_FILE = ".qa_progress";

    private final ExcelReader reader = new ExcelReader();
    private String filePath;
    private int currentRowIndex = -1;
    private QAProduct currentProduct;

    public static class ProgressRecord {
        public final String filePath;
        public final int lastRowIndex;
        public final String sku;

        public ProgressRecord(String filePath, int lastRowIndex, String sku) {
            this.filePath = filePath;
            this.lastRowIndex = lastRowIndex;
            this.sku = sku;
        }
    }

    public QAProduct startSession(String filePath, int initialRow) throws IOException {
        this.filePath = filePath;
        this.reader.loadWorkbook(filePath);

        int row = reader.findNextProductRow(initialRow);
        if (row == -1) {
            row = initialRow;
        }

        this.currentRowIndex = row;
        this.currentProduct = reader.readProductAtRow(row);
        if (this.currentProduct != null) {
            saveProgressRecord(row, this.currentProduct.getSku());
        }
        return this.currentProduct;
    }

    public QAProduct loadProduct(String filePath) throws IOException {
        return startSession(filePath, 2);
    }

    public void saveCurrentProduct(QAProduct product) throws IOException {
        if (filePath == null) {
            throw new IllegalStateException("No active Excel file loaded");
        }
        int rowToSave = (product != null && product.getRowIndex() >= 0) ? product.getRowIndex() : currentRowIndex;
        reader.saveProductAtRow(rowToSave, product, filePath);
        this.currentProduct = product;
        if (product != null) {
            saveProgressRecord(rowToSave, product.getSku());
        }
    }

    public QAProduct loadNextProduct() {
        int nextRow = reader.findNextProductRow(currentRowIndex + 1);
        if (nextRow == -1) {
            return null;
        }
        this.currentRowIndex = nextRow;
        this.currentProduct = reader.readProductAtRow(nextRow);
        if (this.currentProduct != null) {
            saveProgressRecord(nextRow, this.currentProduct.getSku());
        }
        return this.currentProduct;
    }

    public QAProduct loadPreviousProduct() {
        int prevRow = reader.findPreviousProductRow(currentRowIndex - 1);
        if (prevRow == -1) {
            return null;
        }
        this.currentRowIndex = prevRow;
        this.currentProduct = reader.readProductAtRow(prevRow);
        if (this.currentProduct != null) {
            saveProgressRecord(prevRow, this.currentProduct.getSku());
        }
        return this.currentProduct;
    }

    public boolean hasPreviousProduct() {
        return reader.findPreviousProductRow(currentRowIndex - 1) != -1;
    }

    public void saveAndClose(QAProduct product) throws IOException {
        if (product != null) {
            saveCurrentProduct(product);
        }
        close();
    }

    public void saveProgressRecord(int rowIndex, String sku) {
        Properties props = new Properties();
        props.setProperty("filePath", filePath != null ? filePath : "");
        props.setProperty("lastRowIndex", String.valueOf(rowIndex));
        props.setProperty("sku", sku != null ? sku : "");
        props.setProperty("timestamp", String.valueOf(System.currentTimeMillis()));

        try (FileOutputStream fos = new FileOutputStream(PROGRESS_FILE)) {
            props.store(fos, "Excel QA Tool Session Progress");
        } catch (IOException e) {
            System.err.println("Could not save progress: " + e.getMessage());
        }
    }

    public ProgressRecord loadProgressRecord() {
        File file = new File(PROGRESS_FILE);
        if (!file.exists()) {
            return null;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            String savedPath = props.getProperty("filePath");
            int row = Integer.parseInt(props.getProperty("lastRowIndex", "-1"));
            String sku = props.getProperty("sku", "");
            if (row >= 0) {
                return new ProgressRecord(savedPath, row, sku);
            }
        } catch (Exception e) {
            System.err.println("Could not read progress: " + e.getMessage());
        }
        return null;
    }

    public void clearProgress() {
        File file = new File(PROGRESS_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    public boolean hasNextProduct() {
        return reader.findNextProductRow(currentRowIndex + 1) != -1;
    }

    public int getCurrentRowIndex() {
        return currentRowIndex;
    }

    public QAProduct getCurrentProduct() {
        return currentProduct;
    }

    public String getFilePath() {
        return filePath;
    }

    public void close() {
        reader.close();
    }
}