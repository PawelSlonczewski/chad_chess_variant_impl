package com.pslonczewski.chad_chess_variant_impl.engine.player.ai;

import com.pslonczewski.chad_chess_variant_impl.engine.board.Board;
import com.pslonczewski.chad_chess_variant_impl.engine.board.BoardUtils;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Piece;
import com.pslonczewski.chad_chess_variant_impl.engine.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class MyBoardEvaluator implements BoardEvaluator {

    private static final int CHECK_BONUS = 50;
    private static final int CHECK_MATE_BONUS = 10000;
    private static final int DEPTH_BONUS = 100;

    @Override
    public int evaluate(final Board board, final int depth) {
        return scorePlayer(board, board.getWhitePlayer(), depth)
                - scorePlayer(board, board.getBlackPlayer(), depth);
    }

    private int scorePlayer(final Board board, final Player player, final int depth) {
        return pieceValue(player)
                + mobility(player)
                + check(player)
                + checkmate(player, depth)
                + defendingKingBonus(board, player);
    }

    private static int checkmate(final Player player, final int depth) {
        return player.getOpponent().isInCheckMate() ? CHECK_MATE_BONUS * depthBonus(depth) : 0;
    }

    private static int depthBonus(final int depth) {
        return depth == 0 ? 1 : DEPTH_BONUS * depth;
    }

    private static int check(final Player player) {
        return player.getOpponent().isInCheckMate() ? CHECK_BONUS : 0;
    }

    private static int mobility(final Player player) {
        return player.getLegalMoves().size();
    }

    private static int pieceValue(final Player player) {
        int pieceValueScore = 0;
        for (final Piece piece : player.getActivePieces()) {
            pieceValueScore += piece.getPieceValue();
        }
        return pieceValueScore;
    }

    private static int defendingKingBonus(final Board board, final Player player) {
        int defendingKingBonus = 0;
        ArrayList<Integer> directions =  new ArrayList<>(List.of(-12, -1, 1, 12));
        for (final Piece piece : player.getActivePieces()) {
            for (int direction : directions) {
                int fieldPosition = piece.getPiecePosition() + direction;
                if (fieldPosition < 0 || fieldPosition >= BoardUtils.NUM_TILES) continue;
                Piece neighborPiece = board.getPiece(fieldPosition);
                if (neighborPiece != null
                        && neighborPiece.getPieceType() == Piece.PieceType.KING
                        && neighborPiece.getPieceAlliance() == player.getAlliance()) {
                    defendingKingBonus += 50;
                }
            }
        }
        return defendingKingBonus;
    }
}
