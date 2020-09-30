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
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import org.funz.ioplugin.*;
import org.funz.parameter.OutputFunctionExpression;
import org.funz.parameter.SyntaxRules;
import org.funz.util.ParserUtils;
import org.funz.log.Log;

public class Cast3mIOPlugin extends ExtendedIOPlugin {

    static String[] DOC_LINKS = {"http://www-cast3m.cea.fr/"};
    static String INFORMATION = "Cast3m plugin made by Artenum\nCopyright IRSN";

    public Cast3mIOPlugin() {
        variableStartSymbol = VariableStartSymbol;
        variableLimit = VariableLimit;
        formulaStartSymbol = FormulaStartSymbol;
        formulaLimit = FormulaLimit;
        commentLine = "#";
        setID("Cast3m");
        source = "http://irsn.fr/Cast3m";
        // System.out.println("Cast3mIOPlugin");
    }

    public static final int VariableStartSymbol = SyntaxRules.START_SYMBOL_DOLLAR;
    public static final int VariableLimit = SyntaxRules.LIMIT_SYMBOL_PARENTHESIS;
    public static final int FormulaStartSymbol = SyntaxRules.START_SYMBOL_PERCENT;
    public static final int FormulaLimit = SyntaxRules.LIMIT_SYMBOL_BRACKETS;

    /**
     * Role:
     * Method used by Funz to know if the plugin can be used to handle this dataset
     * Not used if -m Model is defined
     */
    @Override
    public boolean acceptsDataSet(File f) {
        Log.out("acceptsDataSet " + f.getName(), 1);
        return f.isFile() && f.getName().endsWith(".dgibi");
    }

    @Override
    public String getPluginInformation() {
        return INFORMATION;
    }

    /**
     * Roles:
     * - Method used by Funz.sh ParseInput to find input variables & formulas
     *   We must define _inputfiles with files which can contain variables & formulas
     * - Method used by Funz.sh CompileInput to find output variables
     *   We must define the hashmap _output:
     *    * scalar variable _output.put("var", "?")
     * 
     *  Parse: MESS 'dep_P2=' dep_P2;
     *  
     *  cmd:
     *   ./Funz.sh ParseInput -m Cast3m -if samples/poutre_console.par.dgibi -> input
     *   ./Funz.sh CompileInput -m Cast3m -if samples/poutre_console.par.dgibi -> output
     */
    @Override
    public void setInputFiles(File... inputfiles) {
        // input files
        _inputfiles = inputfiles;

        // scan input files
        for (File fdgibi: DGibiHelper.filterFiles("dgibi", inputfiles)) {
            String[] lines = ParserUtils.getASCIIFileLines(fdgibi);
            // MESS 'var='
            String[] vars = DGibiHelper.filterMess(lines);
            for (String var: vars) {
                _output.put(var, "?1");
            }
            // OPTI SORT 'var_res.csv'
            String[] csvvars = DGibiHelper.filterOptiSort(lines);
            for (String var: csvvars) {
                String[] vars2 = DGibiHelper.filterTable(lines, var);
                for (String var2: vars2) {
                    _output.put(var2, "?2");
                }
            }
        }
    }

    @Override
    public HashMap<String, Object> readOutput(File outdir) {
        HashMap<String, Object> lout = new HashMap<String, Object>();

        // split _ouput into two groups: scalars and vect
        List<String> g1 = new ArrayList<String>();
        List<String> g2 = new ArrayList<String>();
        DGibiHelper.splitOuputs(_output, g1, g2);

        // special case for file "out.txt" (the output)
        File outfile = new File(outdir, "out.txt");
        if (outfile.exists()) {
            String fullcontent = ParserUtils.getASCIIFileContent(outfile);
            // System.out.println("parsing output:" + fullcontent);
            for (String o : g1) {
                // o contains the variable name
                String lines[] = fullcontent.split("\\r?\\n");
                Double val = DGibiHelper.lookForScalar(lines, o);
                if (val != null) {
                    lout.put(o, val);
                } else {
                    lout.put(o, Double.NaN);
                }
            }
        }

        // special case for file "_res.csv"
        File[] files = outdir.listFiles();
        File[] csvfiles = DGibiHelper.filterFiles("_res.csv", files);
        for (File f: csvfiles) {
            Map<String, Double[]> columns = DGibiHelper.readCSV(f);
            // System.out.println(columns);
            for (String o : g2) {
                if (columns.containsKey(o)) {
                    lout.put(o, columns.get(o));
                }
            }
        }
        return lout;
    }

    @Override
    public LinkedList<OutputFunctionExpression> suggestOutputFunctions() {
        LinkedList<OutputFunctionExpression> s = new LinkedList<OutputFunctionExpression>();
        for (String k : _output.keySet()) {
            if (_output.get(k) instanceof Double) {
                s.addFirst(new OutputFunctionExpression.Numeric(k));
            }
        }
        return s;
    }
}