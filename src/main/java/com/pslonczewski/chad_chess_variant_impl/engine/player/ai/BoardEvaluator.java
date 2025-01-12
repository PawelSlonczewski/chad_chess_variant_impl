package com.pslonczewski.chad_chess_variant_impl.engine.player.ai;

import com.pslonczewski.chad_chess_variant_impl.engine.board.Board;

public interface BoardEvaluator {

    int evaluate(Board board, int depth);
}
