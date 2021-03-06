package com.stasbranger.hypertext.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class HyperXLSService {

	public void processXLSDelibereCC(String file) throws Exception {

		Date dataEsaminata = new Date();

		// Creating a Workbook from an Excel file (.xls or .xlsx)
		Workbook workbook = WorkbookFactory.create(new File(file));

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet s = null;
		short datecol = 0;
		int iirow = 0;

		// Getting the Sheet at index zero
		Sheet sheet = workbook.getSheetAt(0);
		s = wb.createSheet(sheet.getSheetName());
		// Create a DataFormatter to format and get each cell's value as String
		DataFormatter dataFormatter = new DataFormatter();

		// 1. You can obtain a rowIterator and columnIterator and iterate over them
		Iterator<Row> rowIterator = sheet.rowIterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if(row.getCell(0) != null && (row.getCell(0).getCellType() == CellType.NUMERIC)){
				XSSFRow r = s.createRow((short) iirow);
				iirow++;
				// Now let's iterate over the columns of the current row
				Iterator<Cell> cellIterator = row.cellIterator();

				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();

					String cellValue = null;
					
					if(cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)){
						cellValue = new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
						System.out.print(cellValue + "\t");
						XSSFCell c = r.createCell(cell.getColumnIndex());
						c.setCellValue(new XSSFRichTextString(cellValue));
						
						cellValue = new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());

						dataEsaminata = cell.getDateCellValue();

						System.out.print(cellValue + "\t");
						if(datecol == 0)
							datecol = row.getLastCellNum();
						
						c = r.createCell(datecol);
						c.setCellValue(new XSSFRichTextString(cellValue));
					}else{
						cellValue = dataFormatter.formatCellValue(cell);
						System.out.print(cellValue + "\t");
						XSSFCell c = r.createCell(cell.getColumnIndex());
						c.setCellValue(new XSSFRichTextString(cellValue));
					}
					
				}
				//if(row.getCell(0).getHyperlink() != null){
					// ATTACHMENT
				String folder = "Albo Pretorio del anno " + new SimpleDateFormat("yyyy").format(dataEsaminata);
				Path dir = Paths.get("/media/flavio/FC3F-DA32/" + folder);
				String fileFound = null;

				String search = "*Delib.G.C.n." + iirow + "\\ del" + "*";
					System.out.println(search);
					try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, search)) {
						for (Path found : stream) {
							fileFound = found.getFileName().toString();
						}
					}
					XSSFCell c = r.createCell(row.getLastCellNum() + 1);
					if(fileFound != null) {
						c.setCellValue(new XSSFRichTextString("https://www.comune.trani.bt.it/wp-content/uploads/store/" + fileFound));
						System.out.print("https://www.comune.trani.bt.it/wp-content/uploads/store/" + fileFound);
					}
					// TITOLO
					String titolo = "Delib.G.C.n." + iirow + "\\ del\\ " + new SimpleDateFormat("dd.MM.yyyy").format(dataEsaminata);
					c = r.createCell(row.getLastCellNum() + 2);
					c.setCellValue(new XSSFRichTextString(titolo));
					System.out.print(titolo);
				//}
				System.out.println();
			}
		}
		if(iirow > 0){
			FileOutputStream output_file = new FileOutputStream(new File(file + ".mod.xlsx"));  
			//write changes
			wb.write(output_file);
		}else{
			System.out.println("[WARN] " + file + ": links not found.");
		}
		wb.close();
		// Closing the workbook
		workbook.close();
	}
}
