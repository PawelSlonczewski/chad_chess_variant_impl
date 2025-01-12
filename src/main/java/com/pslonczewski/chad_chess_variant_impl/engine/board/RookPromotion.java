package com.pslonczewski.chad_chess_variant_impl.engine.board;

import com.pslonczewski.chad_chess_variant_impl.engine.board.Board.Builder;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Piece;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Rook;

public class RookPromotion extends Move {

    final Move decoratedMove;
    final Rook promotedRook;

    public RookPromotion(final Move decoratedMove) {
        super(decoratedMove.getBoard(), decoratedMove.getMovedPiece(), decoratedMove.getDestinationCoordinate());
        this.decoratedMove = decoratedMove;
        this.promotedRook = (Rook)decoratedMove.getMovedPiece();
    }

    @Override
    public int hashCode() {
        return decoratedMove.hashCode() + (31 * promotedRook.hashCode());
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof RookPromotion && this.decoratedMove.equals(other);
    }

    @Override
    public Board execute() {
        final Board pawnMovedBoard = this.decoratedMove.execute();
        final Builder builder = new Builder();

        for (final Piece piece : pawnMovedBoard.getCurrentPlayer().getActivePieces()) {
            if (!this.promotedRook.equals(piece)) {
                builder.setPiece(piece);
            }
        }

        for (final Piece piece : pawnMovedBoard.getCurrentPlayer().getOpponent().getActivePieces()) {
            builder.setPiece(piece);
        }
        builder.setPiece(this.promotedRook.getPromotionPiece().movePiece(this));
        builder.setMoveMaker(pawnMovedBoard.getCurrentPlayer().getAlliance());

        return builder.build();
    }

    @Override
    public boolean isAttack() {
        return this.decoratedMove.isAttack();
    }

    @Override
    public Piece getAttackedPiece() {
        return this.decoratedMove.getAttackedPiece();
    }

    @Override
    public String toString() {
        return "";
    }
}
