package com.tests.pslonczewski.chad_chess_variant_impl.engine.board;

import com.google.common.collect.Iterables;
import com.pslonczewski.chad_chess_variant_impl.engine.board.*;
import com.pslonczewski.chad_chess_variant_impl.engine.board.Move.MoveFactory;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.King;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Piece;
import org.junit.jupiter.api.Test;

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


}