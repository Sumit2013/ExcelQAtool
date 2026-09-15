package com.cyncly.app.excel;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.FileOutputStream;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CreateExcel1 {
    public static void createExcel(){
        //Create workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        //Create a sheet
        XSSFSheet sheet = workbook.createSheet("Student Details");

        //Create Row
        XSSFRow row = sheet.createRow(0);

        row.createCell(0).setCellValue("Name");
        row.createCell(1).setCellValue("Age");
        row.createCell(2).setCellValue("City");
        
        XSSFRow row2 = sheet.createRow(1);

        row2.createCell(0).setCellValue("John");
        row2.createCell(1).setCellValue(25);
        row2.createCell(2).setCellValue("New York");
        
       try{
        FileOutputStream fis = new FileOutputStream("D:\\ExcelQAtool\\my-app\\src\\StudentData.xlsx");
        workbook.write(fis);
        fis.close();
        workbook.close();
        System.out.println("Excel created successfully");

       }catch(Exception e){
        e.printStackTrace();
       }
    }
}
