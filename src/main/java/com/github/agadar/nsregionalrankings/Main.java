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

    public static void main(String[] args) {
        try {
            NationStates.setUserAgent("NationStates RMB Statistics (https://github.com/Agadar/NationStates-RMB-Analyzer)");
            final RmbStatisticsGenerator rmbStatsGen = new RmbStatisticsGenerator();
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            java.awt.EventQueue.invokeLater(() -> {
                final RmbStatisticsForm form = new RmbStatisticsForm(rmbStatsGen);
                form.setLocationRelativeTo(null);
                form.setVisible(true);
            });
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(RmbStatisticsForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
