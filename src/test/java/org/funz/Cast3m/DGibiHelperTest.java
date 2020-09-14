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
import java.util.Arrays;
import java.util.Map;
import org.funz.Cast3m.DGibiHelper;
import static org.junit.Assert.*;

import org.junit.Test;

public class DGibiHelperTest {
    final String path = "./src/test/java/org/funz/Cast3m/";
    public static void main (String args[]) {
        org.junit.runner.JUnitCore.main(DGibiHelperTest.class.getName());
    }

    @Test
    public final void filterFilesTest () {
        File f = new File(path + "outvar.dgibi");
        File f2 = new File(path + "outfilevar.dgibi");
        File f3 = new File(path + "DGibiHelperTest.java");
        File[] files = new File[3];
        files[0] = f;
        files[1] = f2;
        files[2] = f3;
        File[] res = DGibiHelper.filterFiles(".dgibi", files);
        assertEquals(res.length, 2);
    }

    @Test
    public final void filterMessTest () {
        String[] lines = DGibiHelper.loadDgibi(path + "outvar.dgibi");
        String[] res = DGibiHelper.filterMess(lines);
        // System.out.println(Arrays.toString(res));
        assertEquals(res.length, 2);
        assertEquals(res[0], "var");
        assertEquals(res[1], "var2");
    }

    @Test
    public final void filterOptiSortTest () {
        String[] lines = DGibiHelper.loadDgibi(path + "outfilevar.dgibi");
        String[] res = DGibiHelper.filterOptiSort(lines);
        // System.out.println(Arrays.toString(res));
        assertEquals(res.length, 2);
        assertEquals(res[0], "ta");
        assertEquals(res[1], "ta2");
    }

    @Test
    public final void filterTableTest () {
        String[] lines = DGibiHelper.loadDgibi(path + "table.dgibi");
        String[] res = DGibiHelper.filterTable(lines, "ta");
        // System.out.println(Arrays.toString(res));
        assertEquals(res.length, 18);
        assertEquals(res[0], "TEMPS");
        assertEquals(res[1], "DX1");
    }

    @Test
    public final void filterTable2Test () {
        String[] lines = DGibiHelper.loadDgibi(path + "table2.dgibi");
        String[] res = DGibiHelper.filterTable(lines, "ta");
        assertEquals(res.length, 3);
        assertEquals(res[0], "TEMPS");
        assertEquals(res[1], "DX1");
        String[] res2 = DGibiHelper.filterTable(lines, "ta2");
        // System.out.println(Arrays.toString(res2));
        assertEquals(res2.length, 3);
        assertEquals(res2[0], "TEMPO");
    }

    @Test
    public final void csvTest () {
        Map<String, Double[]> columns = DGibiHelper.readCSV(new File(path + "res.csv"));
        assertEquals(columns.size(), 18);
        Double[] temps = {0.0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1};
        assertArrayEquals(columns.get("TEMPS"), temps);
        Double[] react = {0.0, 1.8459E+003, 3.6918E+003, 5.5377E+003, 7.3837E+003, 9.1997E+003, 1.0739E+004, 1.1983E+004, 1.3024E+004, 1.3944E+004, 1.4750E+004};
        assertArrayEquals(columns.get("REACT"), react);
    }
}
