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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.funz.ioplugin.ExtendedIOPlugin;
import org.funz.log.Log;
import org.funz.parameter.OutputFunctionExpression;
import org.funz.parameter.SyntaxRules;
import org.funz.util.ParserUtils;

/**
 * IOPlugin class for Cast3m.
 *
 * @author Laurent Mallet - ARTENUM SARL
 */
public class Cast3mIOPlugin extends ExtendedIOPlugin {

    private static final String PLUGIN_INFO = "Cast3m plugin made by Artenum\nCopyright IRSN";

    /**
     * Default constructor.
     */
    public Cast3mIOPlugin() {
        this.variableStartSymbol = SyntaxRules.START_SYMBOL_DOLLAR;
        this.variableLimit = SyntaxRules.LIMIT_SYMBOL_PARENTHESIS;
        this.formulaStartSymbol = SyntaxRules.START_SYMBOL_PERCENT;
        this.formulaLimit = SyntaxRules.LIMIT_SYMBOL_BRACKETS;
        this.commentLine = "#";
        this.setID("Cast3m");
        this.source = "http://irsn.fr/Cast3m";
        this.doc_links = new String[] { "http://www-cast3m.cea.fr/" };
        this.information = Cast3mIOPlugin.PLUGIN_INFO;
    }

    /**
     * Role: Method used by Funz to know if the plugin can be used to handle this dataset Not used if -m Model is
     * defined
     */
    @Override
    public boolean acceptsDataSet(final File f) {
        Log.out("acceptsDataSet " + f.getName(), 1);
        return f.isFile() && f.getName().endsWith(".dgibi");
    }

    @Override
    public String getPluginInformation() {
        return Cast3mIOPlugin.PLUGIN_INFO;
    }

    /**
     * Roles: - Method used by Funz.sh ParseInput to find input variables & formulas We must define _inputfiles with
     * files which can contain variables & formulas - Method used by Funz.sh CompileInput to find output variables We
     * must define the hashmap _output: * scalar variable _output.put("var", "?")
     *
     * Parse: MESS 'dep_P2=' dep_P2;
     *
     * cmd: ./Funz.sh ParseInput -m Cast3m -if samples/poutre_console.par.dgibi -> input ./Funz.sh CompileInput -m
     * Cast3m -if samples/poutre_console.par.dgibi -> output
     */
    @Override
    public void setInputFiles(final File... inputfiles) {
        // input files
        this._inputfiles = inputfiles;

        // scan input files
        for (final File fdgibi : DGibiHelper.filterFiles("dgibi", inputfiles)) {
            final String[] lines = ParserUtils.getASCIIFileLines(fdgibi);

            // MESS 'var='
            final String[] vars = DGibiHelper.filterMess(lines);
            for (final String var : vars) {
                this._output.put(var, DGibiHelper.CASE_OUTPUT_KEY);
            }

            // OPTI SORT 'var_res.csv'
            final Map<String, String> optisortVariables = DGibiHelper.filterOptiSort(lines);
            for (final Entry<String, String> entry : optisortVariables.entrySet()) {
                final String[] vars2 = DGibiHelper.filterTable(lines, entry.getKey());
                for (final String var2 : vars2) {
                    this._output.put(var2, DGibiHelper.CSV_OUTPUT_PREFIX + entry.getValue());
                }
            }

            // Lines of type "@EXCEL1 VAR (CHAI 'var' RUN '.csv')"
            final Map<String, String> excelVars = DGibiHelper.filterExcel1(lines);
            for (final Entry<String, String> entry : excelVars.entrySet()) {
                this._output.put(entry.getKey(), DGibiHelper.CSV_OUTPUT_PREFIX + entry.getValue());
            }

        }
    }

    @Override
    public HashMap<String, Object> readOutput(final File outdir) {
        final HashMap<String, Object> lout = new HashMap<>();

        // Read the "out.txt" (the output)
        this.readOutTxt(outdir, lout);

        // Read CSV files
        this.readCsvFiles(outdir, lout);

        return lout;
    }

    private void readOutTxt(final File outdir, final Map<String, Object> result) {
        final List<String> variables = DGibiHelper.extractSimpleVariables(this._output);

        final File outfile = new File(outdir, "out.txt");
        if (outfile.exists()) {
            final String fullcontent = ParserUtils.getASCIIFileContent(outfile);
            for (final String variable : variables) {
                // o contains the variable name
                final String[] lines = fullcontent.split("\\r?\\n");
                final Double val = DGibiHelper.lookForScalar(lines, variable);
                if (val != null) {
                    result.put(variable, val);
                } else {
                    result.put(variable, Double.NaN);
                }
            }
        }
    }

    private void readCsvFiles(final File outdir, final Map<String, Object> result) {
        final Map<String, String> variablesToFilename = DGibiHelper.extractCsvVariables(this._output);

        for (final Entry<String, String> variableAndFilename : variablesToFilename.entrySet()) {
            final String variable = variableAndFilename.getKey();
            final File csvFile = new File(outdir, variableAndFilename.getValue());

            if (csvFile.exists()) {
                final List<String[]> columns = DGibiHelper.readCSV(csvFile);
                if (this.isDouble(columns.get(0)[0])) {
                    this.readCsvFileNoHeader(variable, columns, result);
                } else {
                    this.readCsvFileWithHeader(variable, columns, result);
                }
            } else {
                result.put(variable, Double.NaN);
            }
        }
    }

    private void readCsvFileWithHeader(final String variable, final List<String[]> columns,
            final Map<String, Object> result) {
        final String[] strings = columns.get(0);

        final List<String[]> doubleColumns = new ArrayList<>(columns);
        doubleColumns.remove(0);

        for (int index = 0; index < strings.length; index++) {
            final String current = strings[index];
            if (current.contains(variable)) {
                final int colIndex = index;
                final double[] doubles = doubleColumns.stream().mapToDouble(a -> Double.parseDouble(a[colIndex]))
                        .toArray();
                result.put(variable, doubles);
            }
        }
    }

    private void readCsvFileNoHeader(final String variable, final List<String[]> columns,
            final Map<String, Object> result) {
        final double[][] array = new double[columns.size()][columns.get(0).length];
        for (int listIndex = 0; listIndex < columns.size(); listIndex++) {
            final String[] column = columns.get(listIndex);
            for (int columnIndex = 0; columnIndex < column.length; columnIndex++) {
                array[listIndex][columnIndex] = Double.parseDouble(column[columnIndex]);
            }
        }
        result.put(variable, array);
    }

    @Override
    public LinkedList<OutputFunctionExpression> suggestOutputFunctions() {
        final LinkedList<OutputFunctionExpression> s = new LinkedList<>();
        for (final Entry<String, Object> entry : this._output.entrySet()) {

            final String k = entry.getKey();
            if (k.equals(DGibiHelper.CASE_OUTPUT_KEY)) {
                s.add(new OutputFunctionExpression.Numeric(k));
            } else if (k.startsWith(DGibiHelper.CSV_OUTPUT_PREFIX)) {
                s.add(new OutputFunctionExpression.Numeric2DArray());
            }
        }
        return s;
    }

    private boolean isDouble(final String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }
}