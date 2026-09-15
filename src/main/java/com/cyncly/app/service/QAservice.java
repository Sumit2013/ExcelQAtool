package com.cyncly.app.service;

import java.io.IOException;

import com.cyncly.app.excel.ExcelReader;
import com.cyncly.app.model.QAProduct;

public class QAservice {

    public QAProduct loadProduct(String filePath) throws IOException {

        ExcelReader reader = new ExcelReader();

        QAProduct product = reader.readExcel(filePath);

        return product;
    }
}