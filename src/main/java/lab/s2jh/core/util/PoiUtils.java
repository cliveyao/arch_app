package lab.s2jh.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PoiUtils {

    private final static Logger logger = LoggerFactory.getLogger(PoiUtils.class);

    /**
     * <pre>
     *    Get a list of all specified data
     * </pre>
     * 
     * @return
     */
    public static List<Map<String, String>> readExcelSpecifyColNum(InputStream is, Integer readFromRowNum, Integer specifyColNum) {

        return readExcelSpecifyColNum(is, null, readFromRowNum, specifyColNum);
    }

    public static List<Map<String, String>> readExcelContent(MultipartFile excelFile, Integer sheetIndex, Integer readFromRowNum,
            Integer readFromColNum) {
        return readExcelContent(excelFile, sheetIndex, null, readFromRowNum, readFromColNum);
    }

    public static List<Map<String, String>> readExcelContent(MultipartFile excelFile, String sheetName, Integer readFromRowNum, Integer readFromColNum) {
        return readExcelContent(excelFile, null, sheetName, readFromRowNum, readFromColNum);
    }

    /**
     * <pre>
     * Read Excel data content
     * Agreed format requirements : the first is the header row after row of data
     * Back structure set Map List structure : key = each row heading of the first row , value = cell values ​​, unity is a string , according to their own needs data type conversion
     * </ Pre>
     * @param InputStream
     * @return Map Map object that contains the cell data content
     */
    public static List<Map<String, String>> readExcelContent(MultipartFile excelFile, String sheetName) {
        return readExcelContent(excelFile, sheetName, 0, 0);
    }

    /**
     * <pre>
     *Read Excel data content
     * Agreed format requirements : first readFromRowNum behavior header row after row of data
     * Back structure set Map List structure : key = each row heading of the first row , value = cell values ​​, unity is a string , according to their own needs data type conversion
     * </ Pre>
     * @param InputStream
     * @return Map Map object that contains the cell data content
     */
    public static List<Map<String, String>> readExcelContent(MultipartFile excelFile, String sheetName, Integer readFromRowNum) {
        return readExcelContent(excelFile, sheetName, readFromRowNum, 0);
    }

    /**
     * <pre>
     * Read Excel data content
     * Agreed format requirements : first readFromRowNum behavior header row after row of data
     * Back structure set Map List structure : key = each row heading of the first row , value = cell values ​​, unity is a string , according to their own needs data type conversion
     * </ Pre>
     * @param InputStream
     * @return Map Map object that contains the cell data content
     */
    public static List<Map<String, String>> readExcelContent(MultipartFile excelFile, Integer sheetIndex, Integer readFromRowNum) {
        return readExcelContent(excelFile, sheetIndex, null, readFromRowNum, 0);
    }

    /**
     * <pre>
     * Read Excel data content
     * Agreed format requirements : Article (titleStartRowNum + 1) is the header row after row of data
     * Back structure set Map List structure : key per line = s (titleStartRowNum + 1) of the title line ,
     * Value = cell values ​​, unity is a string , according to their own needs data type conversion
     * </ Pre>
     *
     * @param ExcelName table name
     * @param SheetName read the work item name tag
     * @param ReadFromRowNum readFromRowNum as the title to begin reading
     * @param ReadFromColNum read from the column readFromColNum
     * @return Map Map object that contains the cell data content
     */
    public static List<Map<String, String>> readExcelContent(MultipartFile excelFile, Integer sheetIndex, String sheetName, Integer readFromRowNum,
            Integer readFromColNum) {
        List<Map<String, String>> rows = Lists.newArrayList();
        if (excelFile.isEmpty()) {
            return rows;
        }
        InputStream is = null;
        String excelName = excelFile.getOriginalFilename();
        try {
        	// Read Excel files
            is = excelFile.getInputStream(); //this.getClass().getResourceAsStream(excelName);
            if (excelName.toLowerCase().endsWith(".xls")) {

                Workbook wb = new HSSFWorkbook(is);
                Sheet sheet = null;
                if (StringUtils.isNotBlank(sheetName)) {
                    logger.debug("Excel: {}, Sheet: {}", excelName, sheetName);
                    sheet = wb.getSheet(sheetName);
                } else {

                    if (null == sheetIndex) {
                        sheetIndex = 0;
                    }
                    sheet = wb.getSheetAt(sheetIndex);
                    sheetName = sheet.getSheetName();
                }
                int colNum = readFromColNum;
                Row row0 = sheet.getRow(readFromRowNum);
                //The total number of columns  heading
                List<String> titleList = Lists.newArrayList();
                while (true) {
                    Cell cell = row0.getCell(colNum);
                    if (cell == null) {
                        break;
                    }
                    String title = getCellFormatValue(cell);
                    if (StringUtils.isBlank(title)) {
                        break;
                    }
                    titleList.add(title);
                    colNum++;
                    logger.debug(" - Title : {} = {}", colNum, title);
                }
                logger.debug("Excel: {}, Sheet: {}, Column Num: {}", excelName, sheetName, colNum);
                String[] titles = titleList.toArray(new String[titleList.size()]);


             // Body content should readFromRowNum + 1 from the first row start , the first readFromRowNum behavior header title
                int rowNum = readFromRowNum + 1;
                while (rowNum > readFromRowNum) {
                    Row row = sheet.getRow(rowNum++);
                    if (row == null) {
                        break;
                    }
                    Map<String, String> rowMap = Maps.newHashMap();
                    rowMap.put("sheetName", sheetName);

                    Cell firstCell = row.getCell(readFromColNum);

                 // If the first colNum column is empty and the end line item data processing
                    if (firstCell == null) {
                        logger.info("End as first cell is Null at row: {}", rowNum);
                        break;
                    }

                    int j = readFromColNum;
                    int titleCnt = 0;
                    while (j < colNum) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            String cellValue = getCellFormatValue(cell);
                            if (StringUtils.isNotBlank(cellValue)) {
                                rowMap.put(titles[titleCnt], cellValue);
                            }
                        }
                        titleCnt++;
                        j++;
                    }
                    if (rowNum > readFromRowNum) {
                        rows.add(rowMap);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        logger.debug("Row Map Data: {}", rows);
        return rows;
    }

    /**
     *Read Excel data content
     * Agreed format requirements : the first is the header row after row of data
     * Back structure set Map List structure : key = each row heading of the first row , value = cell values ​​, unity is a string , according to their own needs data type conversion
     *
     * @param InputStream
     * @return Map Map object that contains the cell data content
     */
    public static List<Map<String, String>> readExcelContent(InputStream is, String excelName, String sheetName) {
        List<Map<String, String>> rows = Lists.newArrayList();
        try {
            Workbook wb = new HSSFWorkbook(is);
            logger.debug("Excel: {}, Sheet: {}", excelName, sheetName);
            Sheet sheet = wb.getSheet(sheetName);
            Row row0 = sheet.getRow(0);
            //  The total number of columns  heading
            int colNum = 0;
            List<String> titleList = Lists.newArrayList();
            while (true) {
                Cell cell = row0.getCell(colNum);
                if (cell == null) {
                    break;
                }
                String title = getCellFormatValue(cell);
                if (StringUtils.isBlank(title)) {
                    break;
                }
                titleList.add(title);
                colNum++;
                logger.debug(" - Title : {} = {}", colNum, title);
            }
            logger.debug("Excel: {}, Sheet: {}, Column Num: {}", excelName, sheetName, colNum);
            String[] titles = titleList.toArray(new String[titleList.size()]);


         // Body content should start from the second row , first row header title
            int rowNum = 1;
            while (rowNum > 0) {
                Row row = sheet.getRow(rowNum++);
                if (row == null) {
                    break;
                }
                Map<String, String> rowMap = Maps.newHashMap();
                rowMap.put("sheetName", sheetName);

                Cell firstCell = row.getCell(0);

             // If the first column is empty and the end line item data processing
                if (firstCell == null) {
                    logger.info("End as firt cell is Null at row: {}", rowNum);
                    break;
                }

                int j = 0;
                while (j < colNum) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        String cellValue = getCellFormatValue(cell);
                        rowMap.put(titles[j], cellValue);
                    }
                    j++;
                }
                if (rowNum > 0) {
                    rows.add(rowMap);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        logger.debug("Row Map Data: {}", rows);
        return rows;
    }

    /**
     * Set the data type according to HSSFCell
     * 
     * @param cell
     * @return
     */
    private static String getCellFormatValue(Cell cell) {
        String cellvalue = null;
        if (cell != null) {
            //Analyzing the current Type Cell
            switch (cell.getCellType()) {

         // If the current Type Cell is NUMERIC
            case Cell.CELL_TYPE_NUMERIC:
            case Cell.CELL_TYPE_FORMULA: {
            	// Determine whether the current cell to Date
                if (HSSFDateUtil.isCellDateFormatted(cell)) {

                	// If the Date type is converted to a Data Format

                    // Method 1: The data format is like this with every minute of the time : 2011-10-12 0:00:00
                    // Cellvalue = cell.getDateCellValue () toLocaleString ().;

                    // Method 2 : data format is like this when every minute without tape : 2011-10-12
                    Date date = cell.getDateCellValue();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    cellvalue = sdf.format(date);

                }

             // If it is a pure digital
                else {
                	// Get the current value of the Cell
                    DecimalFormat df = new DecimalFormat("#.####");
                    cellvalue = df.format(cell.getNumericCellValue());
                }
                break;
            }

         // If the current Type Cell is STRING
            case Cell.CELL_TYPE_STRING:

            	// Get the current Cell strings
                cellvalue = cell.getRichStringCellValue().getString();
                break;
            }
        }
        if (cellvalue == null) {
            logger.warn("NULL cell value [{}, {}]", cell.getRowIndex(), cell.getColumnIndex());
        } else {
            cellvalue = cellvalue.trim();
        }
        return cellvalue;
    }

    /**
     * <Pre>
     * Read the contents of the specified column Excel
     * </ Pre>
     *
     * @param ExcelName table name
     * @param SheetName read the work item name tag
     * @param ReadFromRowNum readFromRowNum as the title to begin reading
     * @param ReadFromColNum read from the column readFromColNum
     * @return Map Map object that contains the cell data content
     */
    public static List<Map<String, String>> readExcelSpecifyColNum(InputStream is, Integer sheetIndex, Integer readFromRowNum, Integer specifyColNum) {
        List<Map<String, String>> rows = Lists.newArrayList();
        String sheetName = "defaultSheet";
        try {
        	// Read Excel files
            Workbook wb = new HSSFWorkbook(is);
            Sheet sheet = null;
            if (null == sheetIndex) {
                sheetIndex = 0;
            }

            if (null == readFromRowNum) {
                readFromRowNum = 0;
            }
            sheet = wb.getSheetAt(sheetIndex);
            int colNum = 0;
            Row row0 = sheet.getRow(readFromRowNum);

           //The total number of columns  heading
            List<String> titleList = Lists.newArrayList();
            while (colNum <= specifyColNum) {

                Cell cell = row0.getCell(colNum);
                if (cell == null) {
                    break;
                }
                String title = getCellFormatValue(cell);
                if (StringUtils.isBlank(title)) {
                    break;
                }
                titleList.add(title);
                logger.debug(" - Title : {} = {}", colNum, title);
                colNum++;
            }
            logger.debug("Sheet: {}, Column Num: {}", sheetIndex, colNum);
            String[] titles = titleList.toArray(new String[titleList.size()]);


         // Body content should readFromRowNum + 1 from the first row start , the first readFromRowNum behavior header title
            int rowNum = readFromRowNum + 1;
            while (rowNum > readFromRowNum) {
                Row row = sheet.getRow(rowNum++);
                if (row == null) {
                    break;
                }
                Map<String, String> rowMap = Maps.newHashMap();
                rowMap.put("sheetName", sheetName);

                Cell firstCell = row.getCell(specifyColNum);

             // If the first colNum column is empty and the end line item data processing
                if (firstCell == null) {
                    logger.info("End as first cell is Null at row: {}", rowNum);
                    break;
                }

                int j = 0;
                int titleCnt = 0;
                while (j < colNum) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        String cellValue = getCellFormatValue(cell);
                        if (StringUtils.isNotBlank(cellValue)) {
                            rowMap.put(titles[titleCnt], cellValue);
                        }
                    }
                    titleCnt++;
                    j++;
                }
                if (rowNum > readFromRowNum) {
                    rows.add(rowMap);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        logger.debug("Row Map Data: {}", rows);
        return rows;
    }

    public static void main(String[] args) {

    }

}
