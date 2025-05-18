package com.pslonczewski.chad_chess_variant_impl.engine.board;


public record BoardState(int depth, int score, NodeType nodeType) {

    public enum NodeType {
        LOWER,
        UPPER,
        EXACT,
        NONE
    }

    public static NodeType getNodeType(int alpha, int beta, int score) {
        if (score <= alpha) return NodeType.UPPER;
        if (score >= beta) return NodeType.LOWER;
        return NodeType.EXACT;
    }
}
