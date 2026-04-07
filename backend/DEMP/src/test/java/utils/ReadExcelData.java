
package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
 
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
public class ReadExcelData {
 
	public static String[][] readExcelData() {
 
		String fileName = "C:\\Users\\PR20586952\\Documents\\GitHub\\Digital-Event-Management-Platform-\\backend\\DEMP\\src\\test\\resources\\testdata\\testdata.xlsx";
 
		String[][] inputData = new String[2][6];
 
		try {
			FileInputStream fis = new FileInputStream(fileName);
 
			XSSFWorkbook workBook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workBook.getSheet("Sheet1");
 
			XSSFCell cell;
 
			workBook.close();
			int rowCount = sheet.getLastRowNum();
			DataFormatter df = new DataFormatter();
			for (int rowNo = 1; rowNo <= rowCount; rowNo++) {
				int cellCount = sheet.getRow(rowNo).getLastCellNum();
				for (int cellNo = 0; cellNo < cellCount; cellNo++) {
					cell = sheet.getRow(rowNo).getCell(cellNo);
					inputData[rowNo - 1][cellNo] = df.formatCellValue(cell);
				}
			}
 
		} catch (FileNotFoundException e) {
			System.out.println("File name or path is not correct");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
		return inputData;
	}
 
}
 
