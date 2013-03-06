/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edc.utils;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author KWilcox
 */
public class CsvFileUtils {
  
  public static ArrayList<String> getTimesteps(File csvFile, String timeHeader) throws IOException {
    CsvReader reader = new CsvReader(new FileReader(csvFile));
    reader.readHeaders();
    reader.skipLine(); // skip headers
    ArrayList<String> timesteps = new ArrayList<String>();
    while (reader.readRecord()) {
      timesteps.add(reader.get(timeHeader));
    }
    reader.close();
    return timesteps;
  }
  
  public static ArrayList<String> getVariables(File csvFile, List<String> knownHeaders) throws IOException {
    CsvReader reader = new CsvReader(new FileReader(csvFile));
    reader.readHeaders();
    reader.skipLine(); // skip headers
    ArrayList<String> variables = new ArrayList<String>();
    for (String s : reader.getHeaders()) {
      variables.add(s.trim());
    }
    variables.removeAll(knownHeaders);
    reader.close();
    return variables;
  }
  
  public static void convertToGeneric(File csvFile) throws IOException {
    File outFile = new File(csvFile.getParentFile() + File.separator + "temp.csv");
    CsvWriter writer = new CsvWriter(outFile.getAbsolutePath());
    writer.setEscapeMode(CsvWriter.ESCAPE_MODE_BACKSLASH);
    CsvReader reader = new CsvReader(new FileReader(csvFile));
    reader.readHeaders();
    writer.writeRecord(reader.getHeaders());
    String[] values;
    while (reader.readRecord()) {
      values = reader.getValues();
      for (int j = 0 ; j < values.length ; j++) {
        if (convertToGenericRecord(values[j])) {
          values[j] = '"' + values[j] + '"';
          writer.setUseTextQualifier(false);
        }
        writer.write(values[j]);
        writer.setUseTextQualifier(true);
      }
      writer.endRecord();
    }
    writer.flush();
    writer.close();
    reader.close();
    csvFile.delete();
    outFile.renameTo(csvFile.getAbsoluteFile());
  }      
  
  public static void convertToEsri(File csvFile, String timeHeader) throws IOException {
    File outFile = new File(csvFile.getParentFile() + File.separator + "temp.csv");
    CsvWriter writer = new CsvWriter(outFile.getAbsolutePath());
    writer.setEscapeMode(0);
    CsvReader reader = new CsvReader(new FileReader(csvFile));
    reader.readHeaders();
    reader.skipLine(); // skip headers
    writer.writeRecord(reader.getHeaders());
    String[] values;
    while (reader.readRecord()) {
      values = reader.getValues();
      values[reader.getIndex(timeHeader)] = convertToEsriTime(reader.get(timeHeader));
      for (int j = 0 ; j < values.length ; j++) {
        values[j] = convertToEsriRecord(values[j]);
      }
      writer.writeRecord(values);
    }
    writer.flush();
    writer.close();
    reader.close();
    csvFile.delete();
    outFile.renameTo(csvFile.getAbsoluteFile());
  }
  
  private static String convertToEsriTime(String zulu) {
    return zulu.replace("T", " ").replace("Z", "");
  }
  
  private static String convertToEsriRecord(String record) {
    if (record.contains(";")) {
      record = "Data could not be imported into ArcMap";
    }
    return record;
  }
  
  private static boolean convertToGenericRecord(String record) {
    if (record.contains(";")) {
      return true;
    }
    return false;
  }
  
}
