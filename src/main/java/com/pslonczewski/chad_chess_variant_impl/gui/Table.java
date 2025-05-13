package com.pslonczewski.chad_chess_variant_impl.gui;

import com.google.common.collect.Lists;
import com.pslonczewski.chad_chess_variant_impl.engine.board.*;
import com.pslonczewski.chad_chess_variant_impl.engine.board.Move.MoveFactory;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Piece;
import com.pslonczewski.chad_chess_variant_impl.engine.board.MoveTransition;
import com.pslonczewski.chad_chess_variant_impl.engine.player.ai.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

public class Table extends Observable {

    private final JFrame gameFrame;
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private final BoardPanel boardPanel;
    private final MoveLog moveLog;
    private final GameSetup gameSetup;
    private Board chessBoard;
    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;
    private BoardDirection boardDirection;
    private AtomicBoolean MOVE_FLAG = new AtomicBoolean(false);
    private List<String> boardHistory = new ArrayList<>();

    private Move computerMove;

    private boolean highlightLegalMoves;

    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
    private final static Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);
    private final static Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);
    private final static String defaultPieceImagesPath = "art/pieces/plain/";

    private final Color lightTileColor = Color.decode("#FFFACD");
    private final Color darkTileColor = Color.decode("#593E1A");
    private final Color wallTileColor = Color.decode("#3a2222");

    protected static final Map<String, BoardState> rememberedBoards = new HashMap<>();

    private static final Table INSTANCE = new Table();

    private Table() {
        this.gameFrame = new JFrame("Chad");
        this.gameFrame.setLayout(new BorderLayout());
        final JMenuBar tableMenuBar = createTableMenuBar();
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);

        this.chessBoard = Board.createProblem1Board();
        this.boardHistory.add(Long.toHexString(this.chessBoard.getZobristHashCode()));
        this.gameHistoryPanel = new GameHistoryPanel();
        this.takenPiecesPanel = new TakenPiecesPanel();
        this.boardPanel = new BoardPanel();
        this.moveLog = new MoveLog();
        this.addObserver(new TableGameAIWatcher());
        this.gameSetup = new GameSetup(this.gameFrame, true);
        this.boardDirection = BoardDirection.NORMAL;
        this.highlightLegalMoves = false;
        this.gameFrame.add(this.takenPiecesPanel, BorderLayout.WEST);
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        this.gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);

        this.gameFrame.setVisible(true);
        this.gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static Table get() {
        return INSTANCE;
    }

    public void show() {
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
    }

    private GameSetup getGameSetup() {
        return this.gameSetup;
    }

    private Board getGameBoard() {
        return this.chessBoard;
    }

    public Board getBoard() {
        return this.chessBoard;
    }

    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferenceMenu());
        tableMenuBar.add(createOptionsMenu());
        return tableMenuBar;
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");
        
        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO dispose frame
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

    private JMenu createPreferenceMenu() {
        final JMenu preferenceMenu = new JMenu("Preferences");
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
        flipBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardDirection = boardDirection.opposite();
                boardPanel.drawBoard(chessBoard);
            }
        });
        preferenceMenu.add(flipBoardMenuItem);

        preferenceMenu.addSeparator();

        final JCheckBoxMenuItem legalMoveHighlighterCheckBox = new JCheckBoxMenuItem("Highlight Legal Moves", false);
        legalMoveHighlighterCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightLegalMoves = legalMoveHighlighterCheckBox.isSelected();
                boardPanel.drawBoard(chessBoard);
            }
        });

        preferenceMenu.add(legalMoveHighlighterCheckBox);

        return preferenceMenu;
    }

    private JMenu createOptionsMenu() {
        final JMenu optionsMenu = new JMenu("Options");

        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game");
        setupGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Table.get().getGameSetup().promptUser();
                Table.get().setupUpdate(Table.get().getGameSetup());
            }
        });

        optionsMenu.add(setupGameMenuItem);

        return optionsMenu;
    }

    private void setupUpdate(final GameSetup gameSetup) {
        setChanged();
        notifyObservers(gameSetup);
    }

    boolean isThreefoldRepetition() {
        Map<String, Integer> counts = new HashMap<>();
        for (String state : this.boardHistory) {
            counts.put(state, counts.getOrDefault(state, 0) + 1);
            if (counts.get(state) >= 3) {
                return true;
            }
        }
        return false;
    }

    private static class TableGameAIWatcher implements Observer {

        @Override
        public void update(final Observable o, final Object arg) {
            if (Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().getCurrentPlayer())
                    && !Table.get().getGameBoard().getCurrentPlayer().isInCheckMate()
                    && !Table.get().getGameBoard().getCurrentPlayer().isInStaleMate()) {
                System.out.println(Table.get().getGameBoard().getCurrentPlayer() + " is set to AI, thinking....");
                final AIThinkTank thinkTank = new AIThinkTank();
                thinkTank.execute();
            }

            if (Table.get().getGameBoard().getCurrentPlayer().isInCheckMate()) {
                JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                        "Game Over: Player " + Table.get().getGameBoard().getCurrentPlayer() + " is in checkmate!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            if (Table.get().getGameBoard().getCurrentPlayer().isInStaleMate()) {
                JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                        "Game Over: Player " + Table.get().getGameBoard().getCurrentPlayer() + " is in stalemate!", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            if (Table.get().getGameBoard().isADraw()) {
                JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                        "Game Over: Draw", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            if (get().isThreefoldRepetition()) {
                JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                        "Game Over: Draw Three Fold Repetition", "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            }


        }
    }

    public void updateGameBoard(final Board board) {
        this.boardHistory.add(Long.toHexString(board.getZobristHashCode()));
        this.chessBoard = board;
    }

    public void updateComputerMove(final Move move) {
        this.computerMove = move;
    }

    private MoveLog getMoveLog() {
        return this.moveLog;
    }

    private GameHistoryPanel getGameHistoryPanel() {
        return this.gameHistoryPanel;
    }

    private TakenPiecesPanel getTakenPiecesPanel() {
        return this.takenPiecesPanel;
    }

    private BoardPanel getBoardPanel() {
        return this.boardPanel;
    }

    private void moveMadeUpdate(final PlayerType playerType) {
        setChanged();
        notifyObservers(playerType);
    }

    private static class AIThinkTank extends SwingWorker<Move, String> {

        private AIThinkTank() {

        }

        @Override
        protected Move doInBackground() throws Exception {

            int spinnerDepthValue = Table.get().getGameSetup().getSearchDepthSpinnerValue();

//            final MoveStrategy miniMax = new MiniMax();
//
//            return miniMax.execute(Table.get().getGameBoard(),
//                                   Table.get().getGameSetup().getSearchDepthSpinnerValue());

            final MoveStrategy alphaBetaPrun = new AlphaBetaPruningWithMoveSorter(spinnerDepthValue);

            return alphaBetaPrun.execute(Table.get().getGameBoard(), spinnerDepthValue);

//            final MoveStrategy iterativeDeepeningWithPruning = new IterativeDeepening();
//
//            return iterativeDeepeningWithPruning.execute(Table.get().getGameBoard(),
//                    Table.get().getGameSetup().getSearchDepthSpinnerValue());

//            final MoveStrategy alphaBetaPrunWithTable = new AlphaBetaPruningWithMoveSorterAndTranspositionTable(rememberedBoards);
//
//            return alphaBetaPrunWithTable.execute(Table.get().getGameBoard(),
//                                                  Table.get().getGameSetup().getSearchDepthSpinnerValue());

//            final MoveStrategy iterativeDeepeningTimeDependent = new IterativeDeepeningTimeDependent(30);
//
//            return iterativeDeepeningTimeDependent.execute(Table.get().getGameBoard(),
//                    Table.get().getGameSetup().getSearchDepthSpinnerValue());

//            final MoveStrategy iterativeDeepeningTimeDependent = new IterativeDeepeningWithTranspositionTableTimeDependent(rememberedBoards, 30);
//
//            return iterativeDeepeningTimeDependent.execute(Table.get().getGameBoard(),
//                    Table.get().getGameSetup().getSearchDepthSpinnerValue());

//            final MoveStrategy monteCarloTreeSearch = new MonteCarloTreeSearchNonHeuristics(Table.get().getGameBoard(),
//                                                                               Table.get().getGameSetup()
//                                                                                    .getTimeSpinnerSpinnerValue());
//
//            return monteCarloTreeSearch.execute(Table.get().getGameBoard(),
//                    Table.get().getGameSetup().getSearchDepthSpinnerValue());
        }

        @Override
        public void done() {
            try {
                final Move bestMove = get();

                Table.get().updateComputerMove(bestMove);
                Table.get().updateGameBoard(Table.get().getGameBoard().getCurrentPlayer().makeMove(bestMove).getTransitionBoard());
                Table.get().getMoveLog().addMove(bestMove);
                Table.get().getGameHistoryPanel().redo(Table.get().getGameBoard(), Table.get().getMoveLog());
                Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
                Table.get().moveMadeUpdate(PlayerType.COMPUTER);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class BoardPanel extends JPanel {

        final List<TilePanel> boardTiles;

        BoardPanel() {
            super(new GridLayout(BoardUtils.NUM_TILES / BoardUtils.NUM_TILES_PER_ROW, BoardUtils.NUM_TILES_PER_ROW));
            this.boardTiles = new ArrayList<>();
            for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();
        }

        public void drawBoard(final Board board) {
            removeAll();
            for (final TilePanel tilePanel : boardDirection.traverse(boardTiles)) {
                tilePanel.drawTile(board);
                add(tilePanel);
            }
            validate();
            repaint();
        }
    }

    public static class MoveLog {
        private final List<Move> moves;

        MoveLog() {
            this.moves = new ArrayList<>();
        }

        public List<Move> getMoves() {
            return this.moves;
        }

        public void addMove(final Move move) {
            this.moves.add(move);
        }

        public int size() {
            return this.moves.size();
        }

        public void clear() {
            this.moves.clear();
        }

        public Move removeMove(int index) {
            return this.moves.remove(index);
        }

        public boolean removeMove(final Move move) {
            return this.moves.remove(move);
        }
    }

    private class TilePanel extends JPanel {

        private final int tileId;

        TilePanel(final BoardPanel boardPanel, final int tileId) {
            super(new GridBagLayout());
            this.tileId = tileId;
            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(final MouseEvent e) {

                    if (isRightMouseButton(e)) {
                        // canceling selection of figure
                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;
                    } else if (isLeftMouseButton(e) && !gameSetup.isAIPlayer(chessBoard.getCurrentPlayer())) {
                        if (sourceTile == null) {
                            // first click
                            sourceTile = chessBoard.getTile(tileId);
                            humanMovedPiece = sourceTile.getPiece();
                            if (humanMovedPiece == null) {
                                sourceTile = null;
                            }
                        } else {
                            // second click
                            destinationTile = chessBoard.getTile(tileId);
                            final Move move = MoveFactory.createMove(chessBoard,
                                                                          sourceTile.getTileCoordinate(),
                                                                          destinationTile.getTileCoordinate());

                            final MoveTransition transition = chessBoard.getCurrentPlayer().makeMove(move);
                            if (transition.getMoveStatus().isDone()) {
                                updateGameBoard(transition.getTransitionBoard());
                                moveLog.addMove(move);
                                MOVE_FLAG.set(true);
                            }
                            sourceTile = null;
                            destinationTile = null;
                            humanMovedPiece = null;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                gameHistoryPanel.redo(chessBoard, moveLog);
                                takenPiecesPanel.redo(moveLog);
//                                if (gameSetup.isAIPlayer(chessBoard.getCurrentPlayer())) {
                                if (MOVE_FLAG.get()) {
                                    Table.get().moveMadeUpdate(PlayerType.HUMAN);
                                    MOVE_FLAG.set(false);
                                }
//                              }
                                boardPanel.drawBoard(chessBoard);
//                                System.out.println("Board hash code: " + Long.toHexString(chessBoard.getZobristHashCode()));
                            }
                        });
                    }
                }


                @Override
                public void mousePressed(final MouseEvent e) {

                }

                @Override
                public void mouseReleased(final MouseEvent e) {

                }

                @Override
                public void mouseEntered(final MouseEvent e) {

                }

                @Override
                public void mouseExited(final MouseEvent e) {

                }
            });

            validate();
        }

        public void drawTile(final Board board) {
            assignTileColor();
            assignTilePieceIcon(board);
            highlightLegals(board);
            validate();
            repaint();
        }

        private void assignTilePieceIcon(final Board board) {
            this.removeAll();
            if (board.getTile(this.tileId).isTileOccupied()) {
                final BufferedImage image;
                try {
                    image = ImageIO.read(
                            new File(defaultPieceImagesPath
                                    + board.getTile(this.tileId).getPiece().getPieceAlliance().toString().substring(0, 1)
                                    + board.getTile(this.tileId).getPiece().toString() + ".gif")
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                add(new JLabel(new ImageIcon(image)));
            }
        }

        private void highlightLegals(final Board board) {
            if (highlightLegalMoves) {
                for (final Move move : pieceLegalMoves(board)) {
                    if (move.getDestinationCoordinate() == this.tileId) {
                        try {
                            add(new JLabel(new ImageIcon(ImageIO.read(new File("art/misc/green_dot.png")))));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        private Collection<Move> pieceLegalMoves(Board board) {
            if (humanMovedPiece != null
                && humanMovedPiece.getPieceAlliance() == board.getCurrentPlayer().getAlliance()) {

                return board.getCurrentPlayer().getLegalMoves().stream()
                        .filter(move -> move.getMovedPiece().equals(humanMovedPiece))
                        .collect(Collectors.toCollection(ArrayList::new));
            }
            return Collections.emptyList();
        }

        private void assignTileColor() {
            if (BoardUtils.WHITE_WALL[this.tileId]
                    || BoardUtils.BLACK_WALL[this.tileId]) {
                setBackground(wallTileColor);
            } else if (BoardUtils.TWELFTH_RANK[this.tileId]
                    || BoardUtils.TENTH_RANK[this.tileId]
                    || BoardUtils.EIGHTH_RANK[this.tileId]
                    || BoardUtils.SIXTH_RANK[this.tileId]
                    || BoardUtils.FOURTH_RANK[this.tileId]
                    || BoardUtils.SECOND_RANK[this.tileId]) {
                setBackground(this.tileId % 2 == 0 ? lightTileColor : darkTileColor);
            } else {
                setBackground(this.tileId % 2 != 0 ? lightTileColor : darkTileColor);
            }
        }
    }

    public enum BoardDirection {

        NORMAL {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return boardTiles;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return Lists.reverse(boardTiles);
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
        abstract BoardDirection opposite();
    }

    enum PlayerType {
        HUMAN,
        COMPUTER;
    }
}
