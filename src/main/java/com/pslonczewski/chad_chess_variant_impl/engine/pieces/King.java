package com.pslonczewski.chad_chess_variant_impl.engine.pieces;

import com.google.common.collect.ImmutableList;
import com.pslonczewski.chad_chess_variant_impl.engine.Alliance;
import com.pslonczewski.chad_chess_variant_impl.engine.board.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class King extends Piece {

    private static final int[] CANDIDATE_MOVE_COORDINATE = { -25, -23, -14, -13, -12, -11, -10, -1, 1, 10, 11, 12, 13, 14, 23, 25 };

    public King(final Alliance pieceAlliance, final int piecePosition) {
        super(PieceType.KING, piecePosition, pieceAlliance);
    }

    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATE) {
            final int candidateDestinationCoordinate = this.piecePosition + currentCandidateOffset;

            if (isFirstColumnExclusion(this.piecePosition, currentCandidateOffset)
                    || isTwelfthColumnExclusion(this.piecePosition, currentCandidateOffset)) {
                continue;
            }

            if (BoardUtils.isMoveWithinBoardBounds(candidateDestinationCoordinate)) {
                final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                if (BoardUtils.WHITE_CASTLE[candidateDestinationCoordinate]
                        || BoardUtils.BLACK_CASTLE[candidateDestinationCoordinate]) {
                    if (!candidateDestinationTile.isTileOccupied()
                            && (BoardUtils.WHITE_CASTLE[candidateDestinationCoordinate]
                            || BoardUtils.BLACK_CASTLE[candidateDestinationCoordinate])) {
                        legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                    } else {
                        // also can attack only if attacked piece is in castle
                        final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                        final Alliance pieceAtDestinationAlliance = pieceAtDestination.getPieceAlliance();
                        if (this.pieceAlliance != pieceAtDestinationAlliance) {
                            legalMoves.add(new MajorAttackMove(board, this, candidateDestinationCoordinate,
                                    pieceAtDestination));
                        }
                    }
                }
            }
        }

        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public King movePiece(final Move move) {
        return new King(move.getMovedPiece().pieceAlliance, move.getDestinationCoordinate());
    }

    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.FIRST_COLUMN[currentPosition] && (candidateOffset == -25 || candidateOffset == -14
                || candidateOffset == -13 || candidateOffset == -1 || candidateOffset == 10 || candidateOffset == 11
                || candidateOffset == 23);
    }

    // 12TH column probably not needed
    private static boolean isTwelfthColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.TWELFTH_COLUMN[currentPosition] && (candidateOffset == -23 || candidateOffset == -11
                || candidateOffset == -10 || candidateOffset == 1 || candidateOffset == 13 || candidateOffset == 14
                || candidateOffset == 25);
    }

    @Override
    public String toString() {
        return PieceType.KING.toString();
    }
}
