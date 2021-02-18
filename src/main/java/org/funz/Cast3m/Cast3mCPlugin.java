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
import java.util.Properties;
import org.funz.calculator.plugin.CodeLauncher;
import org.funz.calculator.plugin.DataChannel;
import org.funz.calculator.plugin.DefaultCalculatorPlugin;
import org.funz.calculator.plugin.DefaultCodeLauncher;
import org.funz.calculator.plugin.OutputReader;
import static org.funz.util.ParserUtils.countLines;

public class Cast3mCPlugin extends DefaultCalculatorPlugin {

    public Cast3mCPlugin () {}

    public CodeLauncher createCodeLauncher(Properties variables, DataChannel channel) {
        super.createCodeLauncher(variables, channel);
        return new Cast3mLauncher(this);
    }

    private class Cast3mLauncher extends DefaultCodeLauncher {

        private class Cast3mOutReader extends OutputReader {

            public Cast3mOutReader(DefaultCodeLauncher l) {
                super(l);
                _information = "?";
            }

            public void run() {
                if (getDataChannel() == null) {
                    return;
                }
                while (!_stopMe) {
                    synchronized (this) {
                        try {
                            wait(1000);
                        } catch (Exception e) {
                        }
                    }

                    File out = new File(_dir, "castem.out");
                    if (out.exists()) {
                        _information = "" + countLines(out, "", true);
                    } else {
                        _information = "0";
                    }

                    System.out.println("> Information sent : " + _information);

                    if (!getDataChannel().sendInfomationLineToConsole(_information)) {
                        break;
                    }
                }
            }
        }

        Cast3mLauncher(Cast3mCPlugin plugin) {
            super(plugin);
            _progressSender = new Cast3mOutReader(this);
        }

    }
}