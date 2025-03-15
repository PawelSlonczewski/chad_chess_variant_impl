package com.pslonczewski.chad_chess_variant_impl.engine.player.ai;

import com.pslonczewski.chad_chess_variant_impl.engine.board.Board;
import com.pslonczewski.chad_chess_variant_impl.engine.board.Move;

public interface MoveStrategy {

    long getNumBoardsEvaluated();

    Move execute(final Board board, final int depth);
}
