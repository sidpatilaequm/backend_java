package com.example.multimedia.file_upload_api.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ExcelUtil {

    public static String getString(Row row, int cellIndex) {
        if (row == null) return null;
        Cell cell = row.getCell(cellIndex);
        return getString(cell);
    }

    public static String getString(Cell cell) {
        if (cell == null) return null;
        Object val = getCellValue(cell);
        return val == null ? null : val.toString().trim();
    }

    public static Double getNumeric(Row row, int cellIndex) {
        if (row == null) return null;
        Cell cell = row.getCell(cellIndex);
        return getNumeric(cell);
    }

    public static Double getNumeric(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (cell.getCellType() == CellType.FORMULA) {
            try {
                return cell.getNumericCellValue();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static LocalDate getDate(Row row, int cellIndex) {
        if (row == null) return null;
        Cell cell = row.getCell(cellIndex);
        return getDate(cell);
    }

    public static LocalDate getDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            if (date != null) {
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        }
        return null;
    }

    public static Boolean getBoolean(Row row, int cellIndex) {
        if (row == null) return null;
        Cell cell = row.getCell(cellIndex);
        return getBoolean(cell);
    }

    public static Boolean getBoolean(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            return Boolean.parseBoolean(val) || "1".equals(val) || "Y".equalsIgnoreCase(val) || "YES".equalsIgnoreCase(val);
        }
        return null;
    }

    public static Object getCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue();
                }
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    yield String.valueOf((long) numericValue);
                } else {
                    yield numericValue;
                }
            }
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> {
                try {
                    yield cell.getNumericCellValue();
                } catch (Exception e) {
                    try {
                        yield cell.getStringCellValue();
                    } catch (Exception ex) {
                        yield null;
                    }
                }
            }
            case BLANK -> null;
            default -> null;
        };
    }
}
