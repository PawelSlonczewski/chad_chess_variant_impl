package com.pslonczewski.chad_chess_variant_impl.engine.board;

import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Piece;

public final class NullMove extends Move {

    public NullMove() {
        super(null, -1);
    }

    @Override
    public Board execute() {
        throw new RuntimeException("Cannot execute the null move!");
    }

    @Override
    public int getCurrentCoordinate() {
        return -1;
    }
}
