package com.tests.pslonczewski.chad_chess_variant_impl.engine.board;

import com.pslonczewski.chad_chess_variant_impl.engine.Alliance;
import com.pslonczewski.chad_chess_variant_impl.engine.board.*;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.King;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Rook;
import com.pslonczewski.chad_chess_variant_impl.engine.player.ai.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChadProblemsTest {

    @Test
    public void testProblem1() {
        SoftAssertions softly = new SoftAssertions();

        Board board = problem1Board();

        Map<String, BoardState> testmap = new HashMap<>();

        int moveStrategyNumber = 4;
        int depth = 6;
        int timer = 60;

        MoveStrategy ms = moveStrategyChooser(moveStrategyNumber, testmap, depth, timer, board);

        // 1st move
        System.out.println("1st move");
        Move aiMove = ms.execute(board, depth);

        Move bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("d7"),
                BoardUtils.getCoordinateAtPosition("h7"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory
                        .createMove(board, BoardUtils.getCoordinateAtPosition("i8"), BoardUtils.getCoordinateAtPosition("h8")))
                .getTransitionBoard();


        // 2nd move
        System.out.println("2nd move");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h7"),
                BoardUtils.getCoordinateAtPosition("h8"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory
                        .createMove(board, BoardUtils.getCoordinateAtPosition("i9"), BoardUtils.getCoordinateAtPosition("j9")))
                .getTransitionBoard();

        // 3rd move
        System.out.println("3rd move");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("g3"),
                BoardUtils.getCoordinateAtPosition("g9"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory
                        .createMove(board, BoardUtils.getCoordinateAtPosition("j10"), BoardUtils.getCoordinateAtPosition("i10")))
                .getTransitionBoard();

        // 4th move
        System.out.println("4th move");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("g9"),
                BoardUtils.getCoordinateAtPosition("h9"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory
                        .createMove(board, BoardUtils.getCoordinateAtPosition("i10"), BoardUtils.getCoordinateAtPosition("i9")))
                .getTransitionBoard();

        // 5th move
        System.out.println("5th move");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h8"),
                BoardUtils.getCoordinateAtPosition("i8"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory
                        .createMove(board, BoardUtils.getCoordinateAtPosition("j9"), BoardUtils.getCoordinateAtPosition("j10")))
                .getTransitionBoard();

        // 6th move
        System.out.println("6th move");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("i8"),
                BoardUtils.getCoordinateAtPosition("k10"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        softly.assertAll();
    }

    @Test
    public void testProblem2() {
        SoftAssertions softly = new SoftAssertions();
        Board board = problem2Board();

        Map<String, BoardState> testmap = new HashMap<>();

        int moveStrategyNumber = 4;
        int depth = 2;
        int timer = 60;

        MoveStrategy ms = moveStrategyChooser(moveStrategyNumber, testmap, depth, timer, board);

        // 1st move
        System.out.println("1st move:");
        Move aiMove = ms.execute(board, depth);

        Move bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("c8"),
                BoardUtils.getCoordinateAtPosition("h8"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("i9"),
                BoardUtils.getCoordinateAtPosition("h8"))).getTransitionBoard();

        // 2nd move
        System.out.println("2nd move:");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("c7"),
                BoardUtils.getCoordinateAtPosition("h7"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h8"),
                BoardUtils.getCoordinateAtPosition("i9"))).getTransitionBoard();

        // 3rd move
        System.out.println("3rd move:");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("g4"),
                BoardUtils.getCoordinateAtPosition("g10"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h10"),
                BoardUtils.getCoordinateAtPosition("g10"))).getTransitionBoard();

        // 4th move
        System.out.println("4th move:");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h7"),
                BoardUtils.getCoordinateAtPosition("h9"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("i9"),
                BoardUtils.getCoordinateAtPosition("j10"))).getTransitionBoard();

        // 5th move
        System.out.println("5th move:");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h12"),
                BoardUtils.getCoordinateAtPosition("h10"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("i8"),
                BoardUtils.getCoordinateAtPosition("i10"))).getTransitionBoard();

        // 6th move
        System.out.println("6th move:");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h9"),
                BoardUtils.getCoordinateAtPosition("i9"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        softly.assertAll();
    }

    @Test
    public void testProblem3() {
        SoftAssertions softly = new SoftAssertions();
        Board board = problem3Board();

        Map<String, BoardState> testmap = new HashMap<>();

        int moveStrategyNumber = 2;
        int depth = 6;
        int timer = 60;

        MoveStrategy ms = moveStrategyChooser(moveStrategyNumber, testmap, depth, timer, board);

        // 1st move
        System.out.println("1st move:");
        Move aiMove = ms.execute(board, depth);

        Move bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h3"),
                BoardUtils.getCoordinateAtPosition("h7"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h8"),
                BoardUtils.getCoordinateAtPosition("h7"))).getTransitionBoard();

        // 2nd move
        System.out.println("2nd move:");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("c8"),
                BoardUtils.getCoordinateAtPosition("h8"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("i9"),
                BoardUtils.getCoordinateAtPosition("h8"))).getTransitionBoard();

        // 3rd move
        System.out.println("3rd move:");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("j3"),
                BoardUtils.getCoordinateAtPosition("j10"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h9"),
                BoardUtils.getCoordinateAtPosition("i9"))).getTransitionBoard();

        // 4th move
        System.out.println("4th move:");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("j10"),
                BoardUtils.getCoordinateAtPosition("j6"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("i5"),
                BoardUtils.getCoordinateAtPosition("i7"))).getTransitionBoard();

        // 5th move
        System.out.println("5th move:");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("d6"),
                BoardUtils.getCoordinateAtPosition("d8"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h8"),
                BoardUtils.getCoordinateAtPosition("h9"))).getTransitionBoard();

        // 6th move
        System.out.println("6th move:");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("e5"),
                BoardUtils.getCoordinateAtPosition("e9"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h9"),
                BoardUtils.getCoordinateAtPosition("h10"))).getTransitionBoard();

        // 7th move
        System.out.println("7th move:");
        aiMove = ms.execute(board, depth);

        bestMove = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("d8"),
                BoardUtils.getCoordinateAtPosition("d10"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        softly.assertAll();
    }

    private MoveStrategy moveStrategyChooser(int moveStrategyNumber, final Map<String, BoardState> transpositionTable,
                                             final int depth, final int timer, final Board board) {
        return switch (moveStrategyNumber) {
            case 0 -> new MiniMax();
            case 1 -> new AlphaBetaPruningWithMoveSorterAndTranspositionTable(transpositionTable, depth);
            case 2 -> new IterativeDeepeningWithTranspositionTable(transpositionTable, depth);
            case 3 -> new MonteCarloTreeSearchNonHeuristics(timer);
            case 4 -> new MonteCarloTreeSearchHeuristics(timer);
            default -> new MiniMax();
        };
    }

    private Board problem1Board() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new Rook(Alliance.BLACK, 26));
        builder.setPiece(new Rook(Alliance.BLACK, 28));
        builder.setPiece(new Rook(Alliance.BLACK, 33));
        builder.setPiece(new Rook(Alliance.BLACK, 43));
        builder.setPiece(new King(Alliance.BLACK, 44));
        builder.setPiece(new Rook(Alliance.BLACK, 56));
        builder.setPiece(new Rook(Alliance.BLACK, 57));
        builder.setPiece(new Rook(Alliance.WHITE, 63));
        builder.setPiece(new Rook(Alliance.WHITE, 86));
        builder.setPiece(new Rook(Alliance.WHITE, 88));
        builder.setPiece(new King(Alliance.WHITE, 98));
        builder.setPiece(new Rook(Alliance.WHITE, 100));
        builder.setPiece(new Rook(Alliance.BLACK, 105));
        builder.setPiece(new Rook(Alliance.WHITE, 110));
        builder.setPiece(new Rook(Alliance.WHITE, 114));
        builder.setPiece(new Rook(Alliance.WHITE, 115));

        builder.setMoveMaker(Alliance.WHITE);

        return builder.build();
    }

    private Board problem2Board() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new Rook(Alliance.WHITE, 7));
        builder.setPiece(new Rook(Alliance.BLACK, 8));
        builder.setPiece(new Rook(Alliance.BLACK, 31));
        builder.setPiece(new Rook(Alliance.BLACK, 43));
        builder.setPiece(new King(Alliance.BLACK, 44));
        builder.setPiece(new Rook(Alliance.WHITE, 50));
        builder.setPiece(new Rook(Alliance.BLACK, 56));
        builder.setPiece(new Rook(Alliance.WHITE, 62));
        builder.setPiece(new Rook(Alliance.BLACK, 77));
        builder.setPiece(new Rook(Alliance.WHITE, 87));
        builder.setPiece(new Rook(Alliance.WHITE, 88));
        builder.setPiece(new Rook(Alliance.BLACK, 93));
        builder.setPiece(new Rook(Alliance.WHITE, 102));
        builder.setPiece(new King(Alliance.WHITE, 111));
        builder.setPiece(new Rook(Alliance.WHITE, 112));
        builder.setPiece(new Rook(Alliance.BLACK, 114));
        builder.setPiece(new Rook(Alliance.WHITE, 123));
        builder.setPiece(new Rook(Alliance.BLACK, 129));

        builder.setMoveMaker(Alliance.WHITE);

        return builder.build();
    }

    private Board problem3Board() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new Rook(Alliance.BLACK, 26));
        builder.setPiece(new Rook(Alliance.BLACK, 32));
        builder.setPiece(new Rook(Alliance.BLACK, 43));
        builder.setPiece(new King(Alliance.BLACK, 44));
        builder.setPiece(new Rook(Alliance.WHITE, 50));
        builder.setPiece(new Rook(Alliance.BLACK, 55));
        builder.setPiece(new Rook(Alliance.BLACK, 56));
        builder.setPiece(new Rook(Alliance.WHITE, 75));
        builder.setPiece(new Rook(Alliance.WHITE, 88));
        builder.setPiece(new Rook(Alliance.BLACK, 92));
        builder.setPiece(new King(Alliance.WHITE, 99));
        builder.setPiece(new Rook(Alliance.WHITE, 100));
        builder.setPiece(new Rook(Alliance.WHITE, 115));
        builder.setPiece(new Rook(Alliance.WHITE, 117));

        builder.setMoveMaker(Alliance.WHITE);

        return builder.build();
    }

    @Test
    public void countBranchingFactor() {
        Board board;
        Random rand = new Random();
        long sumOfAllMoves = 0;
        int numberOfGames = 1000;

        for (int i = 0; i < numberOfGames; i++) {
            MoveTransition moveTransition;
            board = Board.createStandardBoard();
            System.out.println("Board: #" + i );
            int movesNumber = rand.nextInt(27) + 3;
            for (int j = 0; j < movesNumber; j++) {
                List<Move> availableMoves = board.getCurrentPlayer().getLegalMoves().stream().toList();
                System.out.println("j = " + j);
                do {
                    Move move = availableMoves.get(rand.nextInt(availableMoves.size()));
                    moveTransition = board.getCurrentPlayer().makeMove(move);
                    System.out.println("Move to be made: " + move);
                } while (!moveTransition.getMoveStatus().isDone());
                System.out.println("Last move was successful!");
                if (BoardUtils.isEndGameScenario(moveTransition.getTransitionBoard())) {
                    System.out.println("Check mate! Restarting board");
                    j = -1;
                    board = Board.createStandardBoard();
                    continue;
                }
                board = moveTransition.getTransitionBoard();

            }
            sumOfAllMoves += this.countLegalMoves(board);
            System.out.println("End of board simulation. Current sum of branches: " + sumOfAllMoves);
        }
        double averageBranchingFactor = sumOfAllMoves / (double) numberOfGames;
        System.out.println("Average branching factor: " + averageBranchingFactor);
    }

    private int countLegalMoves(Board board) {
        List<Move> moves = board.getCurrentPlayer().getLegalMoves().stream().toList();
        int count = 0;
        for (Move move : moves) {
            MoveTransition transition = board.getCurrentPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) count++;
        }
        return count;
    }
}
