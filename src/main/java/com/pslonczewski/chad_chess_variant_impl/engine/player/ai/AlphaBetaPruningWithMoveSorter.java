package com.pslonczewski.chad_chess_variant_impl.engine.player.ai;

import com.pslonczewski.chad_chess_variant_impl.engine.board.Board;
import com.pslonczewski.chad_chess_variant_impl.engine.board.BoardUtils;
import com.pslonczewski.chad_chess_variant_impl.engine.board.Move;
import com.pslonczewski.chad_chess_variant_impl.engine.board.MoveTransition;

import java.util.List;
import java.util.stream.Collectors;

public class AlphaBetaPruningWithMoveSorter implements MoveStrategy {

    private long boardsEvaluated = 0;
    private BoardEvaluator evaluator;

    public AlphaBetaPruningWithMoveSorter() {
        this.evaluator = new StandardBoardEvaluator();
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

        for (Move move : board.getCurrentPlayer().getLegalMoves()) {
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

                if (alpha >= beta) {
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

        for (Move move : board.getCurrentPlayer().getLegalMoves()) {
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

        for (Move move : board.getCurrentPlayer().getLegalMoves()) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                if (depth == 1 && move.isAttack()) {
                    alpha = Math.max(alpha,
                                     quietMin(moveTransition.getTransitionBoard(), alpha, beta));
                } else {
                    alpha = Math.max(alpha,
                            min(moveTransition.getTransitionBoard(), depth - 1,
                                    alpha, beta));
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }
        return alpha;
    }

    private int quietMin(final Board board, final int alpha, int beta) {
        List<Move> attackMoves = board.getCurrentPlayer().getLegalMoves().stream().filter(Move::isAttack).toList();
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
        List<Move> attackMoves = board.getCurrentPlayer().getLegalMoves().stream().filter(Move::isAttack).toList();
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
