package com.pslonczewski.chad_chess_variant_impl.engine.board;

import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Piece;

public final class MajorMove extends Move {

    public MajorMove(final Board board, final Piece movedPiece, final int destinationCoordinate) {
        super(board, movedPiece, destinationCoordinate);
    }

    public MajorMove(MajorMove other) {
        super(other.board, other.movedPiece, other.destinationCoordinate);
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof MajorMove && super.equals(other);
    }

    @Override
    public Move copy() {
        return new MajorMove(this);
    }

    @Override
    public String toString() {
        return BoardUtils.getPositionAtCoordinate(movedPiece.getPiecePosition()).toUpperCase()
                + "-" + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate).toUpperCase();
    }

}
