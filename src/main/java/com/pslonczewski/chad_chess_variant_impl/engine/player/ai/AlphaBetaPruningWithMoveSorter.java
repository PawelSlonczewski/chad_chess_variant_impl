package com.pslonczewski.chad_chess_variant_impl.engine.player.ai;

import com.google.common.collect.Ordering;
import com.pslonczewski.chad_chess_variant_impl.engine.board.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class AlphaBetaPruningWithMoveSorter implements MoveStrategy {

    private long boardsEvaluated = 0;
    private BoardEvaluator evaluator;
    private MoveSorter moveSorter;

    private enum MoveSorter {

        SORT {
            @Override
            Collection<Move> sort(final Collection<Move> moves) {
                return Ordering.from(mvvLva).immutableSortedCopy(moves);
            }
        };

        public static Comparator<Move> mvvLva = new Comparator<Move>() {
            @Override
            public int compare(final Move move1, final Move move2) {
                if (!(move1 instanceof MajorAttackMove) && !(move2 instanceof MajorAttackMove)) {
                    return 0;
                } else if (!(move1 instanceof MajorAttackMove)) {
                    return 1;
                } else if (!(move2 instanceof MajorAttackMove)) {
                    return -1;
                }
                return (move2.getAttackedPiece().getPieceValue() - move2.getMovedPiece().getPieceValue())
                        - (move1.getAttackedPiece().getPieceValue() - move1.getMovedPiece().getPieceValue());
            }
        };

        abstract Collection<Move> sort(Collection<Move> moves);
    }

    public AlphaBetaPruningWithMoveSorter() {
        this.evaluator = new StandardBoardEvaluator();
        this.moveSorter = MoveSorter.SORT;
    }

    @Override
    public long getNumBoardsEvaluated() {
        return this.boardsEvaluated;
    }

    @Override
    public Move execute(final Board board, final int depth) {
        long startTime = System.currentTimeMillis();
        Move bestMove = null;
        int currentValue;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        System.out.println(board.getCurrentPlayer() + " THINKING with depth = " + depth);

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves())) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {

                currentValue = board.getCurrentPlayer().getAlliance().isWhite()
                                   ? min(moveTransition.getTransitionBoard(), depth - 1,
                                           alpha, beta)
                                   : max(moveTransition.getTransitionBoard(), depth - 1,
                                           alpha, beta);

                if (board.getCurrentPlayer().getAlliance().isWhite() && currentValue > alpha) {
                    alpha = currentValue;
                    bestMove = move;
                } else if (board.getCurrentPlayer().getAlliance().isBlack() && currentValue < beta) {
                    beta = currentValue;
                    bestMove = move;
                }
            }
        }
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed: " + endTime / 1000 + " s");

        return bestMove;
    }

    private int min(final Board board, final int depth, final int alpha, int beta) {
        if (depth == 0 || BoardUtils.isEndGameScenario(board)) {
            this.boardsEvaluated++;
            return this.evaluator.evaluate(board, depth);
        }

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves())) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                if (depth == 1 && move.isAttack()) {
                    beta = Math.min(beta,
                                    quietMax(moveTransition.getTransitionBoard(), alpha, beta));
                } else {
                    beta = Math.min(beta,
                                    max(moveTransition.getTransitionBoard(), depth - 1,
                                        alpha, beta));
                }

                if (beta <= alpha) {
                    break;
                }
            }
        }
        return beta;
    }

    private int max(final Board board, final int depth, int alpha, final int beta) {
        if (depth == 0 || BoardUtils.isEndGameScenario(board)) {
            this.boardsEvaluated++;
            return this.evaluator.evaluate(board, depth);
        }

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves())) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                if (depth == 1 && move.isAttack()) {
                    alpha = Math.max(alpha,
                                     quietMin(moveTransition.getTransitionBoard(), alpha, beta));
                } else {
                    alpha = Math.max(alpha,
                            min(moveTransition.getTransitionBoard(), depth - 1,
                                    alpha, beta));
                }
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return alpha;
    }

    private int quietMin(final Board board, final int alpha, int beta) {
        List<Move> attackMoves = this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves()
                                                      .stream()
                                                      .filter(Move::isAttack)
                                                      .toList())
                                                .stream().toList();
        if (attackMoves.isEmpty()) {
            return this.evaluator.evaluate(board, 0);
        } else {
            for (Move move : attackMoves) {
                MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
                if (moveTransition.getMoveStatus().isDone()) {
                    beta = Math.min(beta,
                                    quietMax(moveTransition.getTransitionBoard(),
                                             alpha, beta));
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }
        return beta;
    }

    private int quietMax(final Board board, int alpha, final int beta) {
        List<Move> attackMoves = this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves()
                                                      .stream()
                                                      .filter(Move::isAttack)
                                                      .toList())
                                                .stream().toList();
        if (attackMoves.isEmpty()) {
            return this.evaluator.evaluate(board, 0);
        } else {
            for (Move move : attackMoves) {
                MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
                if (moveTransition.getMoveStatus().isDone()) {
                    alpha = Math.max(alpha, quietMin(moveTransition.getTransitionBoard(), alpha, beta));
                }
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return alpha;
    }

    @Override
    public String toString() {
        return "AlphaBetaPruning";
    }
}
