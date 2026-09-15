package com.cyncly.app.excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import com.cyncly.app.model.Checkpoint;
import com.cyncly.app.model.QAProduct;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {

    public QAProduct readExcel(String filePath) throws IOException {

        FileInputStream file = new FileInputStream(filePath);

        XSSFWorkbook workbook = new XSSFWorkbook(file);

        XSSFSheet sheet = workbook.getSheet("Validation");

        System.out.println("Excel opened successfully!");

       if(sheet == null){
        System.out.println("Sheet not found");
        return null;
       }
       System.out.println("Validation sheet found");

       XSSFRow headerRow = sheet.getRow(0);
       System.out.println("Last row: " + sheet.getLastRowNum());

       for (int i = 160; i <= sheet.getLastRowNum(); i++) {

    XSSFRow row = sheet.getRow(i);

    if (row != null) {
        System.out.println(
            "Java row " + i +
            " | Excel row " + (i + 1) +
            " | SKU: " +
            (row.getCell(5) == null ? "NULL" : row.getCell(5).toString())
        );
    }
}

        if (headerRow == null) {
            System.out.println("Header row not found!");
            return null;
        }

        // DataFormatter formatter = new DataFormatter();

        // for(int i =0;i<=headerRow.getLastCellNum();i++){
        //     System.out.println(i +"->" + headerRow.getCell(i));
            
        // }
       XSSFRow productRow  = sheet.getRow(2);
        
        String type = productRow.getCell(1).toString();
        String subtype = productRow.getCell(2).toString();
        String description = productRow.getCell(3).toString();
        String sku = productRow.getCell(5).toString();

        QAProduct product = new QAProduct(sku, type, subtype, description);
        System.out.println("Product created:");
        System.out.println("SKU: " + sku);
        System.out.println("Type: " + type);
        System.out.println("Subtype: " + subtype);
        System.out.println("Description: " + description);

        List<String> values = Arrays.asList(
        "Ok",
        "Need to confirm",
        "NA"
);

    List<Checkpoint> qACheck =new ArrayList<>();
        for(int i = 12;i<=40;i+=2){
            String checkPointName = headerRow.getCell(i).toString();
            String selectedValue = productRow.getCell(i).toString();
            String comment = productRow.getCell(i + 1).toString();
            Checkpoint checkpoints =new Checkpoint(
                checkPointName,
                values
            );
            
        System.out.println("Checkpoint created: " + checkPointName);
        checkpoints.setSelectedValue(selectedValue);
        checkpoints.setComment(comment);
        System.out.println("Selected value: " + selectedValue);
        System.out.println("Comment: " + comment);
        qACheck.add(checkpoints);

        }
        product.setCheckpoints(qACheck);

        System.out.println("Total checkpoints"+product.getCheckpoints().size());
        workbook.close();
        file.close();
        return product;
    }
}