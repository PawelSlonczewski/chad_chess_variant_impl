package com.pslonczewski.chad_chess_variant_impl.engine.player.ai;

import com.google.common.collect.Ordering;
import com.pslonczewski.chad_chess_variant_impl.engine.board.*;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class AlphaBetaPruningWithMoveSorter implements MoveStrategy {

    private long boardsEvaluated = 0;
    private BoardEvaluator evaluator;
    private MoveSorter moveSorter;
    private Move[][] killerMoves;
    private int depth;

    private enum MoveSorter {

        SORT {
            @Override
            Collection<Move> sort(final Collection<Move> moves, final int depth, final Move[][] killerMoves) {
                return Ordering.from(new Comparator<Move>() {

                    @Override
                    public int compare(Move move1, Move move2) {
                        if (move1.isAttack() && !move2.isAttack()) {
                            return -1;
                        }
                        if (!move1.isAttack() && move2.isAttack()) {
                            return 1;
                        }

                        if (move1.isAttack()) {
                            int attackingPieceMove1 = move1.getMovedPiece().getPieceValue();
                            int attackedPieceMove1 = move1.getAttackedPiece().getPieceValue();
                            int attackingPieceMove2 = move2.getMovedPiece().getPieceValue();
                            int attackedPieceMove2 = move2.getAttackedPiece().getPieceValue();

                            return (attackedPieceMove2 - attackingPieceMove2)
                                    - (attackedPieceMove1 - attackingPieceMove1);
                        }

                        boolean m1Killer = isKiller(move1, depth, killerMoves);
                        boolean m2Killer = isKiller(move2, depth, killerMoves);

                        if (m1Killer) {
                            log.info("");
                        }
                        if (m2Killer) {
                            log.info("Killer move found!");
                        }

                        if (m1Killer && !m2Killer) return -1;
                        if (!m1Killer && m2Killer) return 1;
                        return 0;
                    }

                    private boolean isKiller(final Move move, final int depth, final Move[][] killerMoves) {
                        return move.equals(killerMoves[depth - 1][0]) || move.equals(killerMoves[depth - 1][1]);
                    }
                }).immutableSortedCopy(moves);
            }

        };

        abstract Collection<Move> sort(Collection<Move> moves, final int depth, final Move[][] killerMoves);
    }

    public AlphaBetaPruningWithMoveSorter(final int depth) {
        this.evaluator = new StandardBoardEvaluator();
        this.moveSorter = MoveSorter.SORT;
        this.depth = depth;
        this.killerMoves = new Move[this.depth][2];
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

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves(), depth, this.killerMoves)) {
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

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves(), depth, this.killerMoves)) {
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
                    this.storeKillerMove(move, depth);
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

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves(), depth, this.killerMoves)) {
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
                    this.storeKillerMove(move, depth);
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
                                                      .toList(), -1, this.killerMoves)
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
                                                      .toList(), -1, this.killerMoves)
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

    private void storeKillerMove(final Move move, final int depth) {
        if (move.isAttack()) {
            return;
        }

        if (!move.equals(this.killerMoves[depth][0])) {
            this.killerMoves[depth - 1][1] = this.killerMoves[depth][0];
            this.killerMoves[depth - 1][0] = move;
        }
    }

    @Override
    public String toString() {
        return "AlphaBetaPruning";
    }
}
