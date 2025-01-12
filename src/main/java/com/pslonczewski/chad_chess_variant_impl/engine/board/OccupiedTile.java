package com.pslonczewski.chad_chess_variant_impl.engine.board;

import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Piece;

public final class OccupiedTile extends Tile {

    private final Piece pieceOnTile;

    protected OccupiedTile(final int tileCoordinate, final Piece pieceOnTile) {
        super(tileCoordinate);
        this.pieceOnTile = pieceOnTile;
    }

    @Override
    public boolean isTileOccupied() {
        return true;
    }

    @Override
    public Piece getPiece() {
        return this.pieceOnTile;
    }

    @Override
    public String toString() {
        return this.pieceOnTile.getPieceAlliance().isBlack() ? this.pieceOnTile.toString().toLowerCase()
                : this.pieceOnTile.toString();
    }
}
