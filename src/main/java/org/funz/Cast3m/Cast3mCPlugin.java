package org.funz.Cast3m;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.funz.calculator.plugin.CodeLauncher;
import org.funz.calculator.plugin.DataChannel;
import org.funz.calculator.plugin.DefaultCalculatorPlugin;
import org.funz.calculator.plugin.DefaultCodeLauncher;
import org.funz.calculator.plugin.GrepReader;

public class Cast3mCPlugin extends DefaultCalculatorPlugin {

    public CodeLauncher createCodeLauncher(Properties variables, DataChannel channel) {
        super.createCodeLauncher(variables, channel);
        return new Cast3mLauncher(this);
    }

    private class Cast3mLauncher extends DefaultCodeLauncher {

        Cast3mLauncher(Cast3mCPlugin plugin) {
            super(plugin);
            _progressSender = new Cast3mOutReader(this);
        }

        private class Cast3mOutReader extends GrepReader {

            public Cast3mOutReader(Cast3mLauncher l) {
                super(l, "out.txt", " ITERATION ");
                _information = "?";
            }
        }
        public String buildCommand() {
            StringBuilder sb = new StringBuilder();
            sb.append("castem18");
            sb.append(" ");
            sb.append(((File) (_files.get(0))).getName());
            return sb.toString();
        }

        @Override
        protected int runCommand() throws Exception {
            int ret = super.runCommand();
            if (ret != 0) {
                return ret;
            }
            return ret;
            /*
            try {
                File cas = null;
                File coords = null;
                for (Object ofile : _files) {
                    File file = (File) ofile;
                    if (file.isFile() && file.getName().endsWith(".cas")) {
                        cas = file;
                    }
                    if (file.isFile() && file.getName().equalsIgnoreCase("poi.txt")) {
                        coords = file;
                    }
                }

                Properties poi = null;
                if (coords == null) {
                    System.err.println("Could not find poi.txt file !");
                    return ret;
                } else {
                    poi = new Properties();
                    try {
                        poi.load(new FileInputStream(coords));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        poi = null;
                    }
                }

                TelemacHelper.writeCSVfromRES(cas, poi);

                for (Object ofile : _files) {
                    File file = (File) ofile;
                    if (file.isFile() && file.getName().endsWith(".res")) {
                        file.delete();
                    }
                }

                return ret;
            } catch (Exception e) {
                write(new File("err_read_results.txt"), e.toString());
                return 666;
            } */
        }
    }

    /*public static void main(String[] args) throws IOException {
        String cas = "t2d_garonne_hydro.cas";//"TRI_Qmoy_DDOCE_final_qloi3.cas";
        String res = "r2d_garonne_RSUR.res";//"TRI_Qmoy_DDOCE_brecheAV100m_qloi3_3h.res";

        SerafinNewReader r = new SerafinNewReader();
        r.setFile(new File("src/test/resources/" + cas + "/output.orig/" + res));
        SerafinAdapter s = (SerafinAdapter) (r.read().getSource());
        System.err.println(printDoubleArray(s.getPasDeTemps()));
        System.err.println(Arrays.asList(s.getVariables()));
        write(new File("src/test/resources/" + cas + "/output/T.csv"), printDoubleArray(s.getPasDeTemps()));

        Properties poi = null;
        poi = new Properties();
        try {
            poi.load(new FileInputStream(new File("src/test/resources/" + cas + "/output.orig/poi.txt")));
        } catch (Exception ex) {
            ex.printStackTrace();
            poi = null;
        }
        System.err.println(poi);

        for (int vi = 0; vi < s.getVariables().length; vi++) {
            String v = s.getVariables()[vi];
            for (String p : poi.stringPropertyNames()) {
                if (!p.contains(",")) {
                    double[] d = new double[s.getPasDeTemps().length];
                    for (int i = 0; i < d.length; i++) {
                        d[i] = s.getDonnees(i, vi)[Integer.parseInt(poi.get(p).toString())];
                    }
                    write(new File("src/test/resources/" + cas + "/output/" + RES_VAR.get(v) + "_" + p + ".csv"), printDoubleArray(d));
                }
            }
        }
    }*/
}