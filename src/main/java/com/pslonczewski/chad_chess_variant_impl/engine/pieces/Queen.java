package com.pslonczewski.chad_chess_variant_impl.engine.pieces;

import com.google.common.collect.ImmutableList;
import com.pslonczewski.chad_chess_variant_impl.engine.Alliance;
import com.pslonczewski.chad_chess_variant_impl.engine.board.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Queen extends Piece {

    private final static int[] CANDIDATE_MOVE_VECTOR_COORDINATES = { -13, -12, -11, -1, 1, 11, 12, 13 };

    public Queen(final Alliance pieceAlliance, final int piecePosition) {
        super(PieceType.QUEEN, piecePosition, pieceAlliance);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {

        final List<Move> legalMoves = new ArrayList<>();

        for (final int candidateCoordinateOffset : CANDIDATE_MOVE_VECTOR_COORDINATES) {
            int candidateDestinationCoordinate = this.piecePosition;

            while (BoardUtils.isMoveWithinBoardBounds(candidateDestinationCoordinate)) {

                if (isFirstColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset)
                        || isTwelfthColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset)) {
                    break;
                }

                candidateDestinationCoordinate += candidateCoordinateOffset;

                if (BoardUtils.isMoveWithinBoardBounds(candidateDestinationCoordinate)) {

                    final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                    if (!candidateDestinationTile.isTileOccupied()) {
                        legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                    } else {
                        final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                        final Alliance pieceAtDestinationAlliance = pieceAtDestination.getPieceAlliance();
                        if (this.pieceAlliance != pieceAtDestinationAlliance) {
                            // can attack only when rook is in castle and enemy stands on my wall
                            // or when I stand on wall and enemy is inside castle
                            // rook attack king from outside the castle
                            if (pieceAtDestination.getPieceType().isKing()) {
                                legalMoves.add(new MajorAttackMove(board, this, candidateDestinationCoordinate,
                                        pieceAtDestination));
                            } else if (pieceAtDestinationAlliance.isWallTile(this.piecePosition)
                                    && pieceAtDestinationAlliance.isCastleTile(candidateDestinationCoordinate)) {
                                // attack move from wall
                                legalMoves.add(new MajorAttackMove(board, this, candidateDestinationCoordinate,
                                        pieceAtDestination));
                            } else if (this.pieceAlliance.isCastleTile(this.piecePosition)
                                    && this.pieceAlliance.isWallTile(candidateDestinationCoordinate)) {
                                legalMoves.add(new MajorAttackMove(board, this, candidateDestinationCoordinate,
                                        pieceAtDestination));
                            }
                        }
                        break;
                    }
                }
            }

        }

        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Queen movePiece(final Move move) {
        return new Queen(move.getMovedPiece().pieceAlliance, move.getDestinationCoordinate());
    }

    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.FIRST_COLUMN[currentPosition] && (candidateOffset == -13 || candidateOffset == -1
                || candidateOffset == 11);
    }

    private static boolean isTwelfthColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.TWELFTH_COLUMN[currentPosition] && (candidateOffset == -11 || candidateOffset == 1
                || candidateOffset == 13);
    }

    @Override
    public String toString() {
        return PieceType.QUEEN.toString();
    }
}
