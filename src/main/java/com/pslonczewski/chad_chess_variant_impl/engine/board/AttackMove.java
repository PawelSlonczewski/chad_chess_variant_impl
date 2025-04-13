package com.pslonczewski.chad_chess_variant_impl.engine.board;

import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Piece;

public class AttackMove extends Move {

    final Piece attackedPiece;

    public AttackMove(final Board board, final Piece movedPiece, final int destinationCoordinate,
                      final Piece attackedPiece) {
        super(board, movedPiece, destinationCoordinate);
        this.attackedPiece = attackedPiece;
    }

    public AttackMove(AttackMove other) {
        super(other.board, other.movedPiece, other.destinationCoordinate);
        this.attackedPiece = other.attackedPiece;
    }

    @Override
    public boolean isAttack() {
        return true;
    }

    @Override
    public Piece getAttackedPiece() {
        return this.attackedPiece;
    }

    @Override
    public Move copy() {
        return new AttackMove(this);
    }

    @Override
    public int hashCode() {
        return this.attackedPiece.hashCode() + super.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof AttackMove)) {
            return false;
        }

        final AttackMove otherAttackMove = (AttackMove) other;
        return super.equals(otherAttackMove)
                && this.getAttackedPiece().equals(otherAttackMove.getAttackedPiece());
    }
}
