package com.pslonczewski.chad_chess_variant_impl.engine.board;

import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Piece;

public final class NullMove extends Move {

    public NullMove() {
        super(null, -1);
    }

    public NullMove(NullMove other) {
        super(other.board, other.destinationCoordinate);
    }

    @Override
    public Board execute() {
        throw new RuntimeException("Cannot execute the null move!");
    }

    @Override
    public Move copy() {
        return new NullMove(this);
    }

    @Override
    public int getCurrentCoordinate() {
        return -1;
    }
}
