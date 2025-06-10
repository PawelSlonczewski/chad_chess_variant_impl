package com.pslonczewski.chad_chess_variant_impl.gui;

import com.pslonczewski.chad_chess_variant_impl.engine.Alliance;
import com.pslonczewski.chad_chess_variant_impl.engine.player.Player;
import com.pslonczewski.chad_chess_variant_impl.gui.Table.PlayerType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameSetup extends JDialog {

    private PlayerType whitePlayerType;
    private PlayerType blackPlayerType;
    private final JSpinner searchDepthSpinner;
    private final JSpinner timeSpinner;

    private static final String HUMAN_TEXT = "Człowiek";
    private static final String COMPUTER_TEXT = "Komputer";

    private AiType selectedAiType = AiType.ALPHA_BETA;

    GameSetup(final JFrame frame, final boolean modal) {
        super(frame, "Konfiguracja gry", modal);
        final JPanel myPanel = new JPanel(new GridLayout(0, 1));
        this.setResizable(false);
        final JRadioButton whiteHumanButton = new JRadioButton(HUMAN_TEXT);
        final JRadioButton whiteComputerButton = new JRadioButton(COMPUTER_TEXT);
        final JRadioButton blackHumanButton = new JRadioButton(HUMAN_TEXT);
        final JRadioButton blackComputerButton = new JRadioButton(COMPUTER_TEXT);
        whiteHumanButton.setActionCommand(HUMAN_TEXT);
        final ButtonGroup whiteGroup = new ButtonGroup();
        whiteGroup.add(whiteHumanButton);
        whiteGroup.add(whiteComputerButton);
        whiteHumanButton.setSelected(true);

        final ButtonGroup blackGroup = new ButtonGroup();
        blackGroup.add(blackHumanButton);
        blackGroup.add(blackComputerButton);
        blackHumanButton.setSelected(true);

        getContentPane().add(myPanel);
        myPanel.add(new JLabel("Biały"));
        myPanel.add(whiteHumanButton);
        myPanel.add(whiteComputerButton);
        myPanel.add(new JLabel("Czarny"));
        myPanel.add(blackHumanButton);
        myPanel.add(blackComputerButton);

        myPanel.add(new JLabel("Przeszukiwanie"));
        this.searchDepthSpinner = addLabeledSpinner(myPanel, "Głębokość przeszukiwania", new SpinnerNumberModel(6, 0,
                Integer.MAX_VALUE, 1));
        this.searchDepthSpinner.setValue(5);

        this.timeSpinner = addLabeledSpinner(myPanel, "Limit czasu",
                                             new SpinnerNumberModel(30, 0, Long.MAX_VALUE, 1));


        myPanel.add(new JLabel("Algorytmy SI:"));
        JComboBox<AiType> aiTypeComboBox = new JComboBox<>(AiType.values());
        aiTypeComboBox.setSelectedItem(AiType.ALPHA_BETA);
        myPanel.add(aiTypeComboBox);

        final JButton cancelButton = new JButton("Anuluj");
        final JButton okButton = new JButton("OK");

        okButton.addActionListener(e -> {
            this.whitePlayerType = whiteComputerButton.isSelected() ? PlayerType.COMPUTER : PlayerType.HUMAN;
            this.blackPlayerType = blackComputerButton.isSelected() ? PlayerType.COMPUTER : PlayerType.HUMAN;
            this.selectedAiType = (AiType) aiTypeComboBox.getSelectedItem();
            GameSetup.this.setVisible(false);
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Cancel");
                GameSetup.this.setVisible(false);
            }
        });

        myPanel.add(cancelButton);
        myPanel.add(okButton);

        setLocationRelativeTo(frame);
        pack();
        setVisible(false);
    }

    public AiType getSelectedAiType() {
        return this.selectedAiType;
    }

    public int getSearchDepthSpinnerValue() {
        return (Integer) this.searchDepthSpinner.getValue();
    }

    public long getTimeSpinnerSpinnerValue() {
        return (long) this.timeSpinner.getValue();
    }

    void promptUser() {
        setVisible(true);
        repaint();
    }

    boolean isAIPlayer(final Player player) {
        if (player.getAlliance() == Alliance.WHITE) {
            return getWhitePlayerType() == PlayerType.COMPUTER;
        }
        return getBlackPlayerType() == PlayerType.COMPUTER;
    }

    private PlayerType getBlackPlayerType() {
        return this.blackPlayerType;
    }

    private PlayerType getWhitePlayerType() {
        return this.whitePlayerType;
    }

    private static JSpinner addLabeledSpinner(final Container c, final String label,
                                              final SpinnerModel model) {

        final JLabel l = new JLabel(label);
        c.add(l);
        final JSpinner spinner = new JSpinner(model);
        l.setLabelFor(spinner);
        c.add(spinner);
        return spinner;
    }

    public enum AiType {
        MIN_MAX,
        ALPHA_BETA,
        ITERATIVE_DEEPENING,
        MCTS_NON_HEURISTIC,
        MCTS_HEURISTIC
    }
}
