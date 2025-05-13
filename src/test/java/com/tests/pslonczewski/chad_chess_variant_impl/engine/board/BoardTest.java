package com.tests.pslonczewski.chad_chess_variant_impl.engine.board;

import com.google.common.collect.Iterables;
import com.pslonczewski.chad_chess_variant_impl.engine.Alliance;
import com.pslonczewski.chad_chess_variant_impl.engine.board.*;
import com.pslonczewski.chad_chess_variant_impl.engine.board.Board.Builder;
import com.pslonczewski.chad_chess_variant_impl.engine.board.Move.MoveFactory;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.King;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Piece;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Rook;
import com.pslonczewski.chad_chess_variant_impl.engine.player.ai.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    public void initialBoard() {

        final Board board = Board.createStandardBoard();
        assertEquals(54, board.getCurrentPlayer().getLegalMoves().size());
        assertEquals(54, board.getCurrentPlayer().getOpponent().getLegalMoves().size());
        assertFalse(board.getCurrentPlayer().isInCheck());
        assertFalse(board.getCurrentPlayer().isInCheckMate());
        assertEquals(board.getCurrentPlayer(), board.getWhitePlayer());

        assertEquals(board.getCurrentPlayer().getOpponent(), board.getBlackPlayer());
        assertFalse(board.getCurrentPlayer().getOpponent().isInCheck());
        assertFalse(board.getCurrentPlayer().getOpponent().isInCheckMate());
        assertEquals("White", board.getWhitePlayer().toString());
        assertEquals("Black", board.getBlackPlayer().toString());

//        assertEquals(new StandardBoardEvaluator().evaluate(board, 0), 0);

        final Iterable<Piece> allPieces = board.getAllPieces();
        final Iterable<Move> allMoves = Iterables.concat(board.getWhitePlayer().getLegalMoves(), board.getBlackPlayer().getLegalMoves());
        for(final Move move : allMoves) {
            assertFalse(move.isAttack());
//          assertEquals(MoveUtils.exchangeScore(move), 1);
        }

        assertEquals(108, Iterables.size(allMoves));
        assertEquals(18, Iterables.size(allPieces));
//        assertFalse(BoardUtils.isEndGame(board));
//        assertFalse(BoardUtils.isThreatenedBoardImmediate(board));
//        assertEquals(StandardBoardEvaluator.get().evaluate(board, 0), 0);
        assertEquals(King.class, board.getPiece(44).getClass());
    }

    @Test
    public void testBoardConsistency() {
        final Board board = Board.createStandardBoard();
        assertEquals(board.getCurrentPlayer(), board.getWhitePlayer());

        final MoveTransition t1 = board.getCurrentPlayer()
                .makeMove(MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("e2"),
                        BoardUtils.getCoordinateAtPosition("e4")));
        final MoveTransition t2 = t1.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t1.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("e7"),
                        BoardUtils.getCoordinateAtPosition("e5")));

        final MoveTransition t3 = t2.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t2.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("g1"),
                        BoardUtils.getCoordinateAtPosition("f3")));
        final MoveTransition t4 = t3.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t3.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("d7"),
                        BoardUtils.getCoordinateAtPosition("d5")));

        final MoveTransition t5 = t4.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t4.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("e4"),
                        BoardUtils.getCoordinateAtPosition("d5")));
        final MoveTransition t6 = t5.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t5.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("d8"),
                        BoardUtils.getCoordinateAtPosition("d5")));

        final MoveTransition t7 = t6.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t6.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("f3"),
                        BoardUtils.getCoordinateAtPosition("g5")));
        final MoveTransition t8 = t7.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t7.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("f7"),
                        BoardUtils.getCoordinateAtPosition("f6")));

        final MoveTransition t9 = t8.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t8.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("d1"),
                        BoardUtils.getCoordinateAtPosition("h5")));
        final MoveTransition t10 = t9.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t9.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("g7"),
                        BoardUtils.getCoordinateAtPosition("g6")));

        final MoveTransition t11 = t10.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t10.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("h5"),
                        BoardUtils.getCoordinateAtPosition("h4")));
        final MoveTransition t12 = t11.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t11.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("f6"),
                        BoardUtils.getCoordinateAtPosition("g5")));

        final MoveTransition t13 = t12.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t12.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("h4"),
                        BoardUtils.getCoordinateAtPosition("g5")));
        final MoveTransition t14 = t13.getTransitionBoard()
                .getCurrentPlayer()
                .makeMove(MoveFactory.createMove(t13.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("d5"),
                        BoardUtils.getCoordinateAtPosition("e4")));
    }

    @Test
    public void testProblem1() {
        SoftAssertions softly = new SoftAssertions();

        Board board = problem1Board();

        Map<String, BoardState> testmap = new HashMap<>();

        int moveStrategyNumber = 4;
        int depth = 12;
        int timer = 60;

        MoveStrategy ms = moveStrategyChooser(moveStrategyNumber, testmap, depth, timer, board);

        // 1st move
        System.out.println("1st move");
        Move aiMove = ms.execute(board, depth);

        Move bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("d7"),
                                               BoardUtils.getCoordinateAtPosition("h7"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory
                        .createMove(board, BoardUtils.getCoordinateAtPosition("i8"), BoardUtils.getCoordinateAtPosition("h8")))
                        .getTransitionBoard();


        // 2nd move
        System.out.println("2nd move");
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h7"),
                                          BoardUtils.getCoordinateAtPosition("h8"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory
                        .createMove(board, BoardUtils.getCoordinateAtPosition("i9"), BoardUtils.getCoordinateAtPosition("j9")))
                        .getTransitionBoard();

        // 3rd move
        System.out.println("3rd move");
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("g3"),
                BoardUtils.getCoordinateAtPosition("g9"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory
                        .createMove(board, BoardUtils.getCoordinateAtPosition("j10"), BoardUtils.getCoordinateAtPosition("i10")))
                        .getTransitionBoard();

        // 4th move
        System.out.println("4th move");
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("g9"),
                BoardUtils.getCoordinateAtPosition("h9"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory
                        .createMove(board, BoardUtils.getCoordinateAtPosition("i10"), BoardUtils.getCoordinateAtPosition("i9")))
                .getTransitionBoard();

        // 5th move
        System.out.println("5th move");
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h8"),
                BoardUtils.getCoordinateAtPosition("i8"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory
                        .createMove(board, BoardUtils.getCoordinateAtPosition("j9"), BoardUtils.getCoordinateAtPosition("j10")))
                .getTransitionBoard();

        // 6th move
        System.out.println("6th move");
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("i8"),
                BoardUtils.getCoordinateAtPosition("k10"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        softly.assertAll();
    }

    @Test
    public void testProblem2() {
        SoftAssertions softly = new SoftAssertions();
        Board board = problem2Board();

        Map<String, BoardState> testmap = new HashMap<>();

        int moveStrategyNumber = 0;
        int depth = 2;
        int timer = 60;

        MoveStrategy ms = moveStrategyChooser(moveStrategyNumber, testmap, depth, timer, board);

        // 1st move
        Move aiMove = ms.execute(board, depth);

        Move bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("c8"),
                                                   BoardUtils.getCoordinateAtPosition("h8"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("i9"),
                BoardUtils.getCoordinateAtPosition("h8"))).getTransitionBoard();

        // 2nd move
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("c7"),
                BoardUtils.getCoordinateAtPosition("h7"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h8"),
                BoardUtils.getCoordinateAtPosition("i9"))).getTransitionBoard();

        // 3rd move
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("g4"),
                BoardUtils.getCoordinateAtPosition("g10"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h10"),
                BoardUtils.getCoordinateAtPosition("g10"))).getTransitionBoard();

        // 4th move
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h7"),
                BoardUtils.getCoordinateAtPosition("h9"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("i9"),
                BoardUtils.getCoordinateAtPosition("j10"))).getTransitionBoard();

        // 5th move
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h12"),
                BoardUtils.getCoordinateAtPosition("h10"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("i8"),
                BoardUtils.getCoordinateAtPosition("i10"))).getTransitionBoard();

        // 6th move
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h9"),
                BoardUtils.getCoordinateAtPosition("i9"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        softly.assertAll();
    }

    @Test
    public void testProblem3() {
        SoftAssertions softly = new SoftAssertions();
        Board board = problem3Board();

        Map<String, BoardState> testmap = new HashMap<>();

        int moveStrategyNumber = 0;
        int depth = 2;
        int timer = 60;

        MoveStrategy ms = moveStrategyChooser(moveStrategyNumber, testmap, depth, timer, board);

        // 1st move
        Move aiMove = ms.execute(board, depth);

        Move bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h3"),
                BoardUtils.getCoordinateAtPosition("h7"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h8"),
                BoardUtils.getCoordinateAtPosition("h7"))).getTransitionBoard();

        // 2nd move
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("c8"),
                BoardUtils.getCoordinateAtPosition("h8"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("i9"),
                BoardUtils.getCoordinateAtPosition("h8"))).getTransitionBoard();

        // 3rd move
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("j3"),
                BoardUtils.getCoordinateAtPosition("j10"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h9"),
                BoardUtils.getCoordinateAtPosition("i9"))).getTransitionBoard();

        // 4th move
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("j10"),
                BoardUtils.getCoordinateAtPosition("j6"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("i5"),
                BoardUtils.getCoordinateAtPosition("i7"))).getTransitionBoard();

        // 5th move
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("d6"),
                BoardUtils.getCoordinateAtPosition("d8"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h8"),
                BoardUtils.getCoordinateAtPosition("h9"))).getTransitionBoard();

        // 6th move
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("e5"),
                BoardUtils.getCoordinateAtPosition("e9"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        board = board.getCurrentPlayer().makeMove(bestMove).getTransitionBoard();

        board = board.getCurrentPlayer().makeMove(MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("h9"),
                BoardUtils.getCoordinateAtPosition("h10"))).getTransitionBoard();

        // 7th move
        aiMove = ms.execute(board, depth);

        bestMove = MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("d8"),
                BoardUtils.getCoordinateAtPosition("d10"));

        softly.assertThat(aiMove).isEqualTo(bestMove);

        softly.assertAll();
    }

    private MoveStrategy moveStrategyChooser(int moveStrategyNumber, final Map<String, BoardState> transpositionTable,
                                             final int depth, final int timer, final Board board) {
        return switch (moveStrategyNumber) {
            case 0 -> new MiniMax();
            case 1 -> new AlphaBetaPruningWithMoveSorterAndTranspositionTable(transpositionTable, depth);
            case 2 -> new IterativeDeepeningWithTranspositionTableTimeDependent(transpositionTable, timer);
            case 3 -> new MonteCarloTreeSearchNonHeuristics(timer);
            case 4 -> new MonteCarloTreeSearchHeuristics(timer);
            default -> new MiniMax();
        };
    }

    private Board problem1Board() {
        Builder builder = new Builder();

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
        Builder builder = new Builder();

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
        Builder builder = new Builder();

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
}