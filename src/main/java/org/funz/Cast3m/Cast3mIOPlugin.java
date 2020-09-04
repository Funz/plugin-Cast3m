package org.funz.Cast3m;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.funz.ioplugin.*;
import org.funz.Constants;
import org.funz.parameter.OutputFunctionExpression;
import org.funz.script.ParseExpression;
import org.funz.parameter.SyntaxRules;
import org.funz.util.Parser;
import org.funz.util.ParserUtils;
import org.funz.log.Log;
import org.funz.log.LogCollector.SeverityLevel;

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
     *   ./Funz.sh CompileInput -m Cast3m -if samples/poutre_console.par.dgibi -> output
     *   ./Funz.sh ParseInput -m Cast3m -if samples/poutre_console.par.dgibi -> input
     */
    @Override
    public void setInputFiles(File... inputfiles) {
        _inputfiles = inputfiles;
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(ParseExpression.FILES, _inputfiles);

        List<String> exprs = new ArrayList<String>();
        exprs.add("grep(\"(.*)dgibi\",\"MESS '\")>>after(\"MESS '\")>>before(\"=\")>>trim()");
        
        for (String o : exprs) {
            Object eval = ParseExpression.eval(o, params);
            if (eval != null) {
                if (eval instanceof String) {
                    _output.put(eval.toString(), "?");
                } else if (eval instanceof String[]) {
                    String[] evals = (String[]) eval;
                    for (String e : evals) {
                        _output.put(e, "?");
                    }
                } else if (eval instanceof List) {
                    List evals = (List) eval;
                    for (Object e : evals) {
                        _output.put(e.toString(), "?");
                    }
                } else {
                    Log.logMessage("Cast3mIOPlugin " + getID(), SeverityLevel.INFO, false, "  output name expression " + o + " not evaluable:" + eval);
                }
            }
        }
    }

    @Override
    public HashMap<String, Object> readOutput(File outdir) {
        HashMap<String, Object> lout = new HashMap<String, Object>();

        // special case for file "out.txt" (the output)
        File outfile = new File(outdir, "out.txt");
        if (outfile.exists()) {
            String fullcontent = ParserUtils.getASCIIFileContent(outfile);
            // System.out.println("parsing output:" + fullcontent);
            for (String o : _output.keySet()) {
                // o contains the variable name
                String lines[] = fullcontent.split("\\r?\\n");
                for (String line : lines) {
                    int begin = line.indexOf(o + "=");
                    if (begin == 0) {
                        int end = line.indexOf("=") + 1;
                        String word = line.substring(end);
                        System.out.println("Look for " + o + " " + line + " " + word);
                        try {
                            double d = Double.parseDouble(word);
                            lout.put(o, d);
                        } catch (NumberFormatException nfe) {
                            lout.put(o, Double.NaN);
                        }
                    }
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