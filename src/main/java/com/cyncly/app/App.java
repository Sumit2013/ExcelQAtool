package com.cyncly.app;

import com.cyncly.*;
import com.cyncly.app.model.QAProduct;
import com.cyncly.app.service.QAservice;
import com.cyncly.app.ui.QAWindow;

public class App {
    public static void main(String[] args) throws Exception {
        QAservice service = new QAservice();

        QAProduct product = service.loadProduct(
                "D:\\ExcelQAtool\\my-app\\src\\QA Template 1.xlsx");

        QAWindow window = new QAWindow(product);
        window.show();
    }
}
