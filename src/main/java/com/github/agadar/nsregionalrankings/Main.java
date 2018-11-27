package com.github.agadar.nsregionalrankings;

import com.github.agadar.nationstates.NationStates;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * @author Agadar (https://github.com/Agadar/)
 */
public class Main {

    private static final String USER_AGENT = "NationStates RMB Statistics (https://github.com/Agadar/NationStates-RMB-Analyzer)";

    public static void main(String[] args) {
        try {
            var nationStates = new NationStates(USER_AGENT);
            var rmbStatsGen = new RmbStatisticsGenerator(nationStates);

            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            java.awt.EventQueue.invokeLater(() -> {
                var form = new RmbStatisticsForm(rmbStatsGen);
                form.setLocationRelativeTo(null);
                form.setVisible(true);
            });
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(RmbStatisticsForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
