/**
  * Project        : Prométhée / Cast3m 2020
  * Web site       : http://promethee.irsn.org
  * Copyright      : IRSN, Paris, FRANCE, 2020
  *                  https://www.irsn.fr
  *                  All copyright and trademarks reserved.
  * Email          : https://www.irsn.fr/FR/Contact/Pages/Question.aspx
  * License        : cf. LICENSE.txt
  * Developed By   : Artenum SARL
  * Authors        : Laurent Mallet
  * Contract       : AL17_A02 / 22003083
  * Creation Date  : 2020-09-01
  */

package org.funz.Cast3m;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.*;
import au.com.bytecode.opencsv.CSVReader;

import org.funz.util.ParserUtils;
import org.math.plot.utils.Array;

public class DGibiHelper {
    
    public  static String[] loadDgibi (String path) {
        File fdgibi = new File(path);
        String[] lines = ParserUtils.getASCIIFileLines(fdgibi);
        return lines;
    }

    /**
     * filter lines to extract variable saved in tables
     * - look line with OPTI SORT '${filename}_res.csv';
     * - and then look if it's an excel output and which variable is ouputed
     * @param lines
     * @return
     */
    public static String[] filterOptiSort (String[] lines) {
        Pattern pattern = Pattern.compile("OPTI[ ]+SORT[ ]+'([\\w]+)_res.csv'[ ]*;");
        Pattern pattern2 = Pattern.compile("SORT[ ]+'EXCE'[ ]+([\\w_]*);");

        ArrayList<String> res = new ArrayList<String>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // remove comment
            if (line.startsWith("*")) continue;
            // look for
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                // this file is interesting! look the next line
                if (i+1 < lines.length) {
                    String nextLine = lines[i+1];
                    Matcher matcher2 = pattern2.matcher(nextLine);
                    if (matcher2.find()) {
                        res.add(matcher2.group(1));
                    }
                }
            }
        }
        return res.toArray(new String[0]);
    }

    /**
     * find variable saved in table to extract columns variables
     *  - look for var = TABLE
     */ 
    public static String[] filterTable (String[] lines, String variable) {
        Pattern pattern = Pattern.compile(variable + "[ ]*=[ ]*TABLE[ ]*;");
        Pattern pattern2 = Pattern.compile(variable + "[ ]+\\.[ ]+'([\\w_]*)[ ]*'[ ]*=");

        ArrayList<String> res = new ArrayList<String>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                // search in the following lines
                for (int j = i+1; j < lines.length; j++) {
                    String fline = lines[j];
                    Matcher matcher2 = pattern2.matcher(fline);
                    if (matcher2.find()) {
                        res.add(matcher2.group(1));
                    }
                }
            }
        }
        return res.toArray(new String[0]);
    }

    public static String[] filterMess (String[] lines) {
        Pattern pattern = Pattern.compile("MESS[ ]+'([\\w]+)[ ]*=[ ]*'");
        ArrayList<String> res = new ArrayList<String>();
        for (String line: lines) {
            // remove comment
            if (line.startsWith("*")) continue;
            // look for
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                res.add(matcher.group(1));
            }
        }
        return res.toArray(new String[0]);
    }

    public static File[] filterFiles (String suffix, File...inputfiles) {
        ArrayList<File> res = new ArrayList<File>();
        for (File f: inputfiles) {
            String name = f.getName();
            if (name.endsWith(suffix)) {
                res.add(f);
            }
        }
        return res.toArray(new File[0]);
    }

    public static Map<String, Double[]> readCSV (File f) {
        HashMap<String, Double[]> columns = new HashMap<String, Double[]>();

        try {
            CSVReader csvReader = new CSVReader(new FileReader(f.getPath()), ';');
            String[] headers = csvReader.readNext();
            ArrayList<ArrayList<Double>> values = new ArrayList<ArrayList<Double>>(headers.length);
            for (int i=0; i<headers.length; i++) {
                values.add(new ArrayList<Double>());
            }
        
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                for (int i=0; i<line.length; i++) {
                    if ((line[i].length() > 0) && (i < headers.length)) {
                        values.get(i).add(Double.parseDouble(line[i]));
                    } else if (i < headers.length) {
                        values.get(i).add(Double.valueOf(0));
                    }
                }
            }

            // System.out.println(Arrays.toString(headers));
            // List<String[]> lines = csvReader.readAll();
            // System.out.print(headers.length);
            for (int i=0; i<headers.length; i++) {
                if (headers[i].length() >0) {
                    String key = headers[i].trim();
                    columns.put(key, values.get(i).toArray(new Double[0]));
                    // System.out.println(key);
                    // System.out.println(values.get(i).toArray(new Double[0]));
                }
            }
            return columns;
        } catch (Exception e) {
            System.out.println(e);
        }
        return columns;
    }
}
