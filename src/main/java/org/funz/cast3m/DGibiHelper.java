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
  *                  Arnaud Trouche
  * Contract       : AL17_A02 / 22003083
  * Creation Date  : 2020-09-01
  */

package org.funz.cast3m;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.funz.util.ParserUtils;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Helper class for Dgibiane files reading.
 *
 * @author Laurent Mallet - ARTENUM SARL
 */
class DGibiHelper {

    /**
     * Placeholder used to indicate that an output should be found inside the "out.txt" file
     */
    static final String CASE_OUTPUT_KEY = "?1";

    /**
     * Placeholder used to indicate that an output should be found inside a CSV file.<br>
     * After the "?", the name of the CSV file is given.
     */
    static final String FILE_OUTPUT_PREFIX = "?";

    private static final Pattern EXCEL1_SIMPLE_PATTERN = Pattern.compile("\\s*@EXCEL1\\s+(\\w+)\\s+'(\\w+)\\.csv'\\s*");

    private static final Pattern EXCEL1_CHAI_PATTERN = Pattern
            .compile("\\s*@EXCEL1\\s+(\\w+)\\s+\\(\\s*CHAI(?:NE)?\\s+(.+)\\)");

    private static final Pattern OPTI_SORT_PATTERN = Pattern.compile("\\s*OPTI\\s+SORT\\s+'([\\w\\.]+)'\\s*;");

    private static final Pattern SORT_EXCE_PATTERN = Pattern.compile("SORT\\s+'EXCE'\\s+([\\w_]*);");

    private static final Pattern SORT_CHAI_PATTERN = Pattern.compile("SORT\\s+'CHAI'\\s+([\\w_]*);");

    /**
     * @param path
     *            path to the dgibi file.
     * @return the list of read lines
     */
    public static String[] loadDgibi(final String path) {
        final File fdgibi = new File(path);
        return ParserUtils.getASCIIFileLines(fdgibi);
    }

    /**
     * Filter the lines with the "EXCEL1" procedure<br>
     * Currently we support the following forms:
     * <ul>
     * <li>{@code @EXCEL1 VAR 'file.csv'}</li>
     * <li>{@code @EXCEL1 VAR (CHAI[NE] ...)}</li>
     * </ul>
     *
     * @param lines
     *            the lines of the input file.
     * @return a map associating the VAR to the 'filename.csv'
     *
     *
     * @see "http://www-cast3m.cea.fr/index.php?page=notices&notice=%40EXCEL1"
     */
    public static Map<String, String> filterExcel1(final String[] lines) {

        final Map<String, String> result = new HashMap<>();
        for (final String line : lines) {
            // remove comment
            if (DGibiHelper.ignoreLine(line)) {
                continue;
            }

            // Search for the first form
            final Matcher simpleMatcher = DGibiHelper.EXCEL1_SIMPLE_PATTERN.matcher(line);
            if (simpleMatcher.find()) {
                result.put(simpleMatcher.group(1), simpleMatcher.group(2) + ".csv");
            }

            // Try the second form
            final Matcher matcher = DGibiHelper.EXCEL1_CHAI_PATTERN.matcher(line);
            if (matcher.find()) {
                final String variableName = matcher.group(1);
                final String[] complexFileName = matcher.group(2).split(" ");
                final StringBuilder filenameBuilder = new StringBuilder();
                for (final String item : complexFileName) {
                    if (item.startsWith("'")) {
                        filenameBuilder.append(item.replace("'", ""));
                    } else {
                        // Variable
                        filenameBuilder.append(DGibiHelper.getVariableValue(lines, item));
                    }
                }

                result.put(variableName, filenameBuilder.toString());
            }
        }
        return result;
    }

    /**
     * Filter lines to extract variable saved in tables - look line with OPTI SORT '${filename}'; - and then look if
     * it's an excel output and which variable is outputed
     *
     * @param lines
     * @return the name of the 'EXCEL' variable associated to the name of the output CSV file
     */
    static Map<String, String> filterSortExcel(final String[] lines) {
        return DGibiHelper.filterOptiSort(lines, DGibiHelper.SORT_EXCE_PATTERN);
    }

    /**
     * Filter lines to extract variable saved in txt file
     *
     * @param lines
     * @return the name of the 'CHAI' variable associated to the name of the output file
     */
    static Map<String, String> filterSortChai(final String[] lines) {
        return DGibiHelper.filterOptiSort(lines, DGibiHelper.SORT_CHAI_PATTERN);
    }

    private static Map<String, String> filterOptiSort(final String[] lines, final Pattern sortPattern) {

        final Map<String, String> results = new HashMap<>();
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            // remove comment
            if (line.isEmpty() || line.startsWith("*")) {
                continue;
            }
            // look for
            final Matcher optiMatcher = DGibiHelper.OPTI_SORT_PATTERN.matcher(line);
            // this file is interesting! look the next line
            if (optiMatcher.find() && ((i + 1) < lines.length)) {
                final String filename = optiMatcher.group(1);
                final String nextLine = lines[i + 1];
                final Matcher sortMatcher = sortPattern.matcher(nextLine);
                if (sortMatcher.find()) {
                    final String excelVariable = sortMatcher.group(1);
                    results.put(excelVariable, filename);
                }
            }
        }

        return results;
    }

    /**
     * find variable saved in table to extract columns variables - look for var = TABLE
     */
    public static String[] filterTable(final String[] lines, final String variable) {
        final Pattern pattern = Pattern.compile(variable + "[ ]*=[ ]*TABLE[ ]*;");
        final Pattern pattern2 = Pattern.compile(variable + "[ ]+\\.[ ]+'([\\w_]*)[ ]*'[ ]*=");

        final ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            final Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                // search in the following lines
                for (int j = i + 1; j < lines.length; j++) {
                    final String fline = lines[j];
                    final Matcher matcher2 = pattern2.matcher(fline);
                    if (matcher2.find()) {
                        res.add(matcher2.group(1));
                    }
                }
            }
        }
        return res.toArray(new String[0]);
    }

    public static String[] filterMess(final String[] lines) {
        final Pattern pattern = Pattern.compile("MESS[ ]+'([\\w_]+)[ ]*=[ ]*'");
        final ArrayList<String> res = new ArrayList<>();
        for (final String line : lines) {
            // remove comment
            if (line.startsWith("*")) {
                continue;
            }
            // look for
            final Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                res.add(matcher.group(1));
            }
        }
        return res.toArray(new String[0]);
    }

    public static File[] filterFiles(final String suffix, final File... inputfiles) {
        final ArrayList<File> res = new ArrayList<>();
        for (final File f : inputfiles) {
            final String name = f.getName();
            if (name.endsWith(suffix)) {
                res.add(f);
            }
        }
        return res.toArray(new File[0]);
    }

    /**
     * @param outputs
     *            the raw output map
     * @return the names of the "simple" variables stored in the outputs map
     */
    static List<String> extractSimpleVariables(final Map<String, Object> outputs) {
        final List<String> variables = new ArrayList<>();
        for (final Entry<String, Object> entry : outputs.entrySet()) {
            if (DGibiHelper.CASE_OUTPUT_KEY.equals(entry.getValue())) {
                variables.add(entry.getKey());
            }
        }
        return variables;
    }

    /**
     * @param outputs
     *            the raw output map
     * @return the name and the filename of the CSV output variables
     */
    static Map<String, String> extractCsvVariables(final Map<String, Object> outputs) {
        return DGibiHelper.extractVariables(outputs, DGibiHelper.FILE_OUTPUT_PREFIX, v -> v.endsWith(".csv"));
    }

    /**
     * @param outputs
     *            the raw output map
     * @return the name and the filename of the non CSV output variables (usually txt files)
     */
    static Map<String, String> extractNonCsvVariables(final Map<String, Object> outputs) {
        return DGibiHelper.extractVariables(outputs, DGibiHelper.FILE_OUTPUT_PREFIX, v -> !v.endsWith(".csv"));
    }

    private static Map<String, String> extractVariables(final Map<String, Object> outputs, final String prefix,
            final Predicate<String> additionalTest) {
        final Map<String, String> results = new HashMap<>();
        for (final Entry<String, Object> entry : outputs.entrySet()) {
            final Object obj = entry.getValue();
            if (obj instanceof String) {
                final String value = (String) obj;
                if (value.startsWith(prefix) && additionalTest.test(value)) {
                    final String filename = value.replace(prefix, "");
                    results.put(entry.getKey(), filename);
                }
            }
        }
        return results;
    }

    public static List<String[]> readCSV(final File f) {
        List<String[]> result;

        try (final CSVReader csvReader = new CSVReader(new FileReader(f.getPath()), ';')) {

            final List<String[]> lines = csvReader.readAll();

            if (!lines.isEmpty()) {
                final String[] firstLine = lines.get(0);
                final int size = lines.size();
                result = Arrays.stream(firstLine).map(s -> new String[size]).collect(Collectors.toList());

                // Rest of lines
                for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                    final String[] line = lines.get(lineIndex);
                    for (int column = 0; column < line.length; column++) {
                        result.get(column)[lineIndex] = line[column].trim();
                    }
                }
            } else {
                return Collections.emptyList();
            }

        } catch (final Exception e) {
            System.out.println(e);
            return Collections.emptyList();
        }

        // Remove empty columns
        final Iterator<String[]> iterator = result.iterator();
        while (iterator.hasNext()) {
            final String[] column = iterator.next();
            if (Arrays.stream(column).allMatch(String::isEmpty)) {
                iterator.remove();
            }
        }

        return result;
    }

    public static Double lookForScalar(final String[] lines, final String var) {
        final Pattern pattern = Pattern.compile("" + var + "=\\s*([\\d\\.\\+\\-E]+)[ ]*");

        for (final String line : lines) {
            // remove $
            if (line.startsWith("$")) {
                continue;
            }
            final String[] parts = line.split(";");
            final String code = parts[0];

            // look for
            final Matcher matcher = pattern.matcher(code);
            if (matcher.find()) {
                try {
                    final String value = matcher.group(1);
                    return Double.parseDouble(value);
                } catch (final Exception e) {
                }
            }
        }
        return null;
    }

    /**
     * Look for a variable value
     */
    private static String getVariableValue(final String[] lines, final String item) {
        for (final String line : lines) {
            // remove $
            if (line.startsWith("$")) {
                continue;
            }

            final String cleanLine = line.trim();
            if (cleanLine.startsWith(item)) {
                return cleanLine.split("=")[1].trim().replace(";", "").trim();
            }
        }
        return null;
    }

    private static boolean ignoreLine(final String line) {
        return line.isEmpty() || line.startsWith("*");
    }

    private DGibiHelper() {
        // Suppress default constructor for noninstantiability.
        throw new AssertionError();
    }

}
