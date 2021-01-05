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

package org.funz.cast3m;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class DGibiHelperTest {
    final String path = "./src/test/java/org/funz/cast3m/";

    public static void main(final String args[]) {
        org.junit.runner.JUnitCore.main(DGibiHelperTest.class.getName());
    }

    @Test
    public final void filterFilesTest() {
        final File f = new File(this.path + "outvar.dgibi");
        final File f2 = new File(this.path + "outfilevar.dgibi");
        final File f3 = new File(this.path + "DGibiHelperTest.java");
        final File[] files = new File[3];
        files[0] = f;
        files[1] = f2;
        files[2] = f3;
        final File[] res = DGibiHelper.filterFiles(".dgibi", files);
        Assert.assertEquals(res.length, 2);
    }

    @Test
    public final void filterMessTest() {
        final String[] lines = DGibiHelper.loadDgibi(this.path + "outvar.dgibi");
        final String[] res = DGibiHelper.filterMess(lines);
        Assert.assertEquals(res.length, 2);
        Assert.assertEquals(res[0], "var");
        Assert.assertEquals(res[1], "var2");
    }

    @Test
    public final void filterPoutreTest() {
        final String[] lines = DGibiHelper.loadDgibi(this.path + "poutre.dgibi");
        final String[] res = DGibiHelper.filterMess(lines);
        Assert.assertEquals(res.length, 1);
        Assert.assertEquals(res[0], "dep_P2");
    }

    @Test
    public final void filterSortExcelTest() {
        final String[] lines = DGibiHelper.loadDgibi(this.path + "outfilevar.dgibi");
        final Map<String, String> map = DGibiHelper.filterSortExcel(lines);

        final List<String> res = new ArrayList<>(map.keySet());
        final List<String> exp = Arrays.asList("ta", "ta2");
        res.sort(String::compareTo);

        Assert.assertEquals(exp, res);
    }

    @Test
    public final void filterSortChaiTest() {
        final String[] lines = DGibiHelper.loadDgibi(this.path + "outfilevar.dgibi");
        final Map<String, String> map = DGibiHelper.filterSortChai(lines);

        final List<String> res = new ArrayList<>(map.keySet());
        final List<String> exp = Arrays.asList("variable");
        res.sort(String::compareTo);

        Assert.assertEquals(exp, res);
    }

    @Test
    public final void filterTableTest() {
        final String[] lines = DGibiHelper.loadDgibi(this.path + "table.dgibi");
        final String[] res = DGibiHelper.filterTable(lines, "ta");
        Assert.assertEquals(res.length, 18);
        Assert.assertEquals(res[0], "TEMPS");
        Assert.assertEquals(res[1], "DX1");
    }

    @Test
    public final void filterTable2Test() {
        final String[] lines = DGibiHelper.loadDgibi(this.path + "table2.dgibi");
        final String[] res = DGibiHelper.filterTable(lines, "ta");
        Assert.assertEquals(res.length, 3);
        Assert.assertEquals(res[0], "TEMPS");
        Assert.assertEquals(res[1], "DX1");
        final String[] res2 = DGibiHelper.filterTable(lines, "ta2");
        Assert.assertEquals(res2.length, 3);
        Assert.assertEquals(res2[0], "TEMPO");
    }

    @Test
    public final void csvTest() {
        final List<String[]> columns = DGibiHelper.readCSV(new File(this.path + "res.csv"));
        Assert.assertEquals(columns.size(), 18);

        // Test column 0
        final List<String> column0 = Arrays.stream(columns.get(0)).collect(Collectors.toList());
        final String title0 = column0.remove(0);
        Assert.assertEquals("TEMPS", title0);
        final double[] double0 = column0.stream().mapToDouble(Double::parseDouble).toArray();
        final double[] temps = { 0.0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1 };
        Assert.assertTrue(Arrays.equals(double0, temps));

        // Test column 17
        final List<String> column17 = Arrays.stream(columns.get(17)).collect(Collectors.toList());
        final String title17 = column17.remove(0);
        Assert.assertEquals("REACT", title17);
        final double[] double17 = column17.stream().mapToDouble(Double::parseDouble).toArray();
        final double[] react = { 0.0, 1.8459E+003, 3.6918E+003, 5.5377E+003, 7.3837E+003, 9.1997E+003, 1.0739E+004,
                1.1983E+004, 1.3024E+004, 1.3944E+004, 1.4750E+004 };
        Assert.assertTrue(Arrays.equals(double17, react));
    }

    @Test
    public final void splitTest() {
        final HashMap<String, Object> outputs = new HashMap<>();
        outputs.put("var1", DGibiHelper.CASE_OUTPUT_KEY);
        outputs.put("var2", DGibiHelper.FILE_OUTPUT_PREFIX + "fileone.csv");
        outputs.put("var3", DGibiHelper.FILE_OUTPUT_PREFIX + "other.csv");
        outputs.put("var4", DGibiHelper.CASE_OUTPUT_KEY);
        outputs.put("var5_", DGibiHelper.CASE_OUTPUT_KEY);

        // ?1
        final List<String> g1 = DGibiHelper.extractSimpleVariables(outputs);
        Assert.assertEquals(3, g1.size());

        // ?.csv
        final Map<String, String> g2 = DGibiHelper.extractCsvVariables(outputs);
        Assert.assertEquals(2, g2.size());
    }

    @Test
    public final void look4ScalarTest() {
        final String[] lines = { "var=2.3;", "var2" };
        final Double res = DGibiHelper.lookForScalar(lines, "var");
        final Double expected = 2.3;
        Assert.assertEquals(res, expected);
    }

    @Test
    public final void look4Scalar2Test() {
        final String[] lines = { "var=+2.322;", "var2" };
        final Double res = DGibiHelper.lookForScalar(lines, "var");
        final Double expected = 2.322;
        Assert.assertEquals(res, expected);
    }

    @Test
    public final void look4Scalar3Test() {
        final String[] lines = { "var=+2.32E2;", "var2" };
        final Double res = DGibiHelper.lookForScalar(lines, "var");
        final Double expected = 2.32E2;
        Assert.assertEquals(res, expected);
    }

    @Test
    public final void look4Scalar4Test() {
        final String[] lines = DGibiHelper.loadDgibi(this.path + "poutre.out");
        final Double res = DGibiHelper.lookForScalar(lines, "dep_P2");
        final Double expected = -0.0514286;
        Assert.assertEquals(res, expected);
    }
}
