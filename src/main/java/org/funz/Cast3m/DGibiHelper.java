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
package org.funz.Cast3m;

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
     * Placeholder used to indicate that an output should be found inside a file.<br>
     * After the "#", the name of the file is given.
     */
    static final String FILE_OUTPUT_PREFIX = "#";

    private static final Pattern EXCEL1_SIMPLE_PATTERN = DGibiHelper
            .compile("\\s*@excel1\\s+(\\w+)\\s+'(\\w+)\\.csv'\\s*");

    private static final Pattern EXCEL1_CHAI_PATTERN = DGibiHelper
            .compile("\\s*@excel1\\s+(\\w+)\\s+\\(\\s*chai(?:ne)?\\s+(.+)\\)");

    private static final Pattern OPTI_SORT_PATTERN = DGibiHelper.compile("\\s*opti\\s+sort\\s+'([\\w\\.]+)'\\s*;");

    private static final Pattern SORT_EXCE_PATTERN = DGibiHelper.compile("sort\\s+'exce(?:l)?'\\s+([\\w_]*);");

    private static final Pattern SORT_CHAI_PATTERN = DGibiHelper.compile("sort\\s+'chai(?:ne)?'\\s+([\\w_]*);");

    /**
     * @param path
     *            path to the dgibi file.
     * @return the list of read lines
     */
    public static List<String> loadDgibi(final String path) {
        return DGibiHelper.loadDgibi(new File(path));
    }

    /**
     * @param fdgibi
     *            the dgibi file.
     * @return the list of read lines
     */
    public static List<String> loadDgibi(final File fdgibi) {
        final String[] rawLines = ParserUtils.getASCIIFileLines(fdgibi);

        final List<String> cleanLines = new ArrayList<>(rawLines.length);

        int index = 0;
        while (index < rawLines.length) {
            final String line = rawLines[index].trim();
            if (!"".equals(line) && !line.startsWith("*")) {
                // Ignore comments lines
                if (line.endsWith(";")) {
                    // We have a complete line, we can store it
                    cleanLines.add(line);
                } else if (index < (rawLines.length - 1)) {
                    // We need to look for the end of the line
                    final StringBuilder newLine = new StringBuilder(line);

                    index++;
                    String nextLine = rawLines[index].trim();
                    while (!nextLine.trim().endsWith(";") && (index < (rawLines.length - 1))) {
                        newLine.append(' ').append(nextLine);
                        index++;
                        nextLine = rawLines[index].trim();
                    }
                    newLine.append(' ').append(nextLine);
                    cleanLines.add(newLine.toString());
                }
            }
            index++;
        }
        return cleanLines;
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
    public static Map<String, String> filterExcel1(final List<String> lines) {
        // Filter lines starting with @EXCEL
        final List<String> filtered = lines.stream().filter(l -> l.toLowerCase().startsWith("@excel1"))
                .collect(Collectors.toList());

        final Map<String, String> result = new HashMap<>();
        for (final String line : filtered) {
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
    static Map<String, String> filterSortExcel(final List<String> lines) {
        return DGibiHelper.filterOptiSort(lines, DGibiHelper.SORT_EXCE_PATTERN);
    }

    /**
     * Filter lines to extract variable saved in txt file
     *
     * @param lines
     * @return the name of the 'CHAI' variable associated to the name of the output file
     */
    static Map<String, String> filterSortChai(final List<String> lines) {
        return DGibiHelper.filterOptiSort(lines, DGibiHelper.SORT_CHAI_PATTERN);
    }

    private static Map<String, String> filterOptiSort(final List<String> lines, final Pattern sortPattern) {

        // Filter lines starting with OPTI
        final List<String> filtered = lines.stream().filter(l -> l.toLowerCase().startsWith("opti"))
                .collect(Collectors.toList());

        final Map<String, String> results = new HashMap<>();
        for (int i = 0; i < filtered.size(); i++) {
            final String line = filtered.get(i);
            final int nextIndex = lines.indexOf(line) + 1;

            // look for
            final Matcher optiMatcher = DGibiHelper.OPTI_SORT_PATTERN.matcher(line);
            // this file is interesting! look the next line
            if (optiMatcher.find() && (nextIndex < lines.size())) {
                final String filename = optiMatcher.group(1);
                final String nextLine = lines.get(nextIndex);
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
     * Find variable saved in table to extract columns variables - look for var = TABLE
     *
     * @param lines
     *            lines of the file
     * @param variable
     *            the variable to filter
     * @return the columns variables
     */
    public static List<String> filterTable(final List<String> lines, final String variable) {
        final List<String> filtered = lines.stream().filter(l -> l.toLowerCase().startsWith(variable.toLowerCase()))
                .collect(Collectors.toList());

        final Pattern pattern = DGibiHelper.compile(variable + "\\s*=\\s*table\\s*;");
        final Pattern pattern2 = DGibiHelper.compile(variable + "\\s+\\.\\s+'([\\w_]*)\\s*'\\s*=");

        final ArrayList<String> res = new ArrayList<>();
        for (final String line : filtered) {
            final Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                // search in the following lines
                for (final String fline : filtered) {
                    final Matcher matcher2 = pattern2.matcher(fline);
                    if (matcher2.find()) {
                        res.add(matcher2.group(1));
                    }
                }
            }
        }
        return res;
    }

    /**
     * Find variable saved in EVOL MANU to extract columns variables - look for var = EVOL MANU
     *
     * @param lines
     *            lines of the file
     * @param variable
     *            the variable to filter
     * @return the columns variables
     */
    public static List<String> filterEvolManu(final List<String> lines, final String variable) {
        final List<String> filtered = lines.stream().filter(l -> l.toLowerCase().startsWith(variable.toLowerCase()))
                .collect(Collectors.toList());
        final Pattern pattern = DGibiHelper
                .compile(variable + "\\s*=\\s*evol\\s+manu\\s+'(.*)'\\s+[^']+\\s+'(.*)'\\s+[^']+\\s*;");
        return DGibiHelper.filterOneLiner(filtered, pattern);
    }

    /**
     * Find variable saved in EXTR to extract columns variables - look for var = EXTR
     *
     * @param lines
     *            lines of the file
     * @param variable
     *            the variable to filter
     * @return the columns variables
     */
    public static List<String> filterExtr(final List<String> lines, final String variable) {
        final List<String> filtered = lines.stream().filter(l -> l.toLowerCase().startsWith(variable.toLowerCase()))
                .collect(Collectors.toList());
        final Pattern pattern = DGibiHelper.compile(variable + "\\s*=\\s*extr\\s+[^']+\\s+'(.*)'\\s*;");
        final List<String> extractedVars = DGibiHelper.filterOneLiner(filtered, pattern);
        if (extractedVars.size() == 1) {
            // We have found the unique column. An extraction does not necessarily have a name, so we put the name of
            // the variable
            extractedVars.clear();
            extractedVars.add(variable);
        }
        return extractedVars;
    }

    private static List<String> filterOneLiner(final List<String> lines, final Pattern pattern) {
        final List<String> res = new ArrayList<>();
        for (int i = 0; (i < lines.size()) && res.isEmpty(); i++) {
            final String line = lines.get(i);
            // PB performance // PB text on several lines
            final Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                for (int groupIndex = 1; groupIndex <= matcher.groupCount(); groupIndex++) {
                    res.add(matcher.group(groupIndex));
                }
            }
        }
        return res;
    }

    public static List<String> filterMess(final List<String> lines) {
        final List<String> filtered = lines.stream().filter(l -> l.toLowerCase().startsWith("mess"))
                .collect(Collectors.toList());

        final Pattern pattern = DGibiHelper.compile("mess[ ]+'([\\w_]+)[ ]*=[ ]*'");
        final List<String> res = new ArrayList<>();
        for (final String line : filtered) {
            // look for
            final Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                res.add(matcher.group(1));
            }
        }
        return res;
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
            e.printStackTrace();
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
        final Pattern pattern = DGibiHelper.compile("" + var + "=\\s*([\\d\\.\\+\\-E]+)[ ]*");

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
    private static String getVariableValue(final List<String> lines, final String item) {
        for (final String line : lines) {
            final String cleanLine = line;
            if (cleanLine.startsWith(item)) {
                return cleanLine.split("=")[1].trim().replace(";", "").trim();
            }
        }
        return null;
    }

    private static Pattern compile(final String regexp) {
        return Pattern.compile("(?iu)" + regexp);
    }

    private DGibiHelper() {
        // Suppress default constructor for noninstantiability.
        throw new AssertionError();
    }

}
