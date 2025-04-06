package com.pslonczewski.chad_chess_variant_impl.engine.player.ai;

import com.google.common.collect.Ordering;
import com.pslonczewski.chad_chess_variant_impl.JChess;
import com.pslonczewski.chad_chess_variant_impl.engine.board.*;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Log4j2
public class AlphaBetaPruningWithMoveSorterAndTranspositionTable implements MoveStrategy {

    private long boardsEvaluated = 0;
    private final BoardEvaluator evaluator = new StandardBoardEvaluator();
    private final MoveSorter moveSorter = MoveSorter.SORT;;
    private final Map<String, BoardState> rememberedBoards;

    public AlphaBetaPruningWithMoveSorterAndTranspositionTable(final Map<String, BoardState> rememberedBoards) {
        this.rememberedBoards = rememberedBoards;
    }

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

    @Override
    public long getNumBoardsEvaluated() {
        return this.boardsEvaluated;
    }

    @Override
    public Move execute(final Board board, final int depth) {
        long startTime = System.currentTimeMillis();
        Move bestMove = null;
        int currentValue = 0;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // Probably not needed
//        if (this.rememberedBoards.containsKey(boardHexString)) {
//            System.out.println("Boards' hash found in remembered board!");
//        }

        System.out.println(board.getCurrentPlayer() + " THINKING with depth = " + depth);

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves())) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                String boardHexString = Long.toHexString(moveTransition.getTransitionBoard().getZobristHashCode());
                if (this.rememberedBoards.containsKey(boardHexString)
                        && (depth - 1) <= this.rememberedBoards.get(boardHexString).depth() ) {
                    currentValue = rememberedBoards.get(boardHexString).score();
                    log.info("Board' hash found in remembered board: " + boardHexString);
                } else {
                    currentValue = board.getCurrentPlayer().getAlliance().isWhite()
                            ? min(moveTransition.getTransitionBoard(), depth - 1,
                            alpha, beta)
                            : max(moveTransition.getTransitionBoard(), depth - 1,
                            alpha, beta);

                    this.rememberedBoards.put(boardHexString, new BoardState(depth - 1, currentValue));
                }

                if (board.getCurrentPlayer().getAlliance().isWhite() && currentValue > alpha) {
                    alpha = currentValue;
                    bestMove = move;
                } else if (board.getCurrentPlayer().getAlliance().isBlack() && currentValue < beta) {
                    beta = currentValue;
                    bestMove = move;
                }
            }
        }
        String topBoardHexString = Long.toHexString(board.getZobristHashCode());
        this.rememberedBoards.put(topBoardHexString, new BoardState(depth,
                                                                    board.getCurrentPlayer().getAlliance().isWhite()
                                                                    ? alpha : beta));
        log.info("Boards' hash added: " + topBoardHexString + " on depth: " + depth);
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed: " + endTime / 1000 + " s");

        return bestMove;
    }

    private int min(final Board board, final int depth, final int alpha, int beta) {
        String boardHexString = Long.toHexString(board.getZobristHashCode());
        if (this.rememberedBoards.containsKey(boardHexString)
            && this.rememberedBoards.get(boardHexString).depth() >= depth) {
            log.info("Boards' hash found in remembered board: " + boardHexString);
            return this.rememberedBoards.get(boardHexString).score();
        }

        if (depth == 0 || BoardUtils.isEndGameScenario(board)) {
            this.boardsEvaluated++;
            int evaluation = this.evaluator.evaluate(board, depth);
            this.rememberedBoards.put(boardHexString, new BoardState(depth, evaluation)); /* TODO Check if depth on end game scenario changes anything!!! */
            return evaluation;
        }

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves())) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            String zobristHexHashCode = Long.toHexString(moveTransition.getTransitionBoard().getZobristHashCode());
            if (this.rememberedBoards.containsKey(zobristHexHashCode)
                && this.rememberedBoards.get(zobristHexHashCode).depth() >= depth - 1) {
                log.info("Boards' hash found in remembered board: " + zobristHexHashCode);
                beta = Math.min(beta, this.rememberedBoards.get(zobristHexHashCode).score());
            } else {
                if (moveTransition.getMoveStatus().isDone()) {
                    if (depth == 1 && move.isAttack()) {
                        beta = Math.min(beta,
                                quietMax(moveTransition.getTransitionBoard(), alpha, beta));
                    } else {
                        int moveScore = max(moveTransition.getTransitionBoard(), depth - 1,
                                alpha, beta);
                        beta = Math.min(beta, moveScore);
                        this.rememberedBoards.put(zobristHexHashCode, new BoardState(depth - 1, moveScore));
                        log.info("Boards' hash added: " + zobristHexHashCode + " on depth: " + (depth - 1));
                    }

                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }
        return beta;
    }

    private int max(final Board board, final int depth, int alpha, final int beta) {
        String boardHexString = Long.toHexString(board.getZobristHashCode());
        if (this.rememberedBoards.containsKey(boardHexString)
                && this.rememberedBoards.get(boardHexString).depth() >= depth) {
            log.info("Boards' hash found in remembered board: {}", boardHexString);
            if (this.rememberedBoards.get(boardHexString).depth() > depth) {
                return this.rememberedBoards.get(boardHexString).score();
            }
        }

        if (depth == 0 || BoardUtils.isEndGameScenario(board)) {
            this.boardsEvaluated++;
            int evaluation = this.evaluator.evaluate(board, depth);
            this.rememberedBoards.put(boardHexString, new BoardState(0, evaluation));
            return evaluation;
        }

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves())) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            String zobristHexHashCode = Long.toHexString(moveTransition.getTransitionBoard().getZobristHashCode());
            if (this.rememberedBoards.containsKey(zobristHexHashCode)
                    && this.rememberedBoards.get(zobristHexHashCode).depth() >= depth - 1) {
                log.info("Boards' hash found in remembered board: " + zobristHexHashCode + ". Current depth : "
                            + depth + ". Remembered depth : " + this.rememberedBoards.get(zobristHexHashCode).depth());
                alpha = Math.max(alpha, this.rememberedBoards.get(zobristHexHashCode).score());
            } else {
                if (moveTransition.getMoveStatus().isDone()) {
                    if (depth == 1 && move.isAttack()) {
                        alpha = Math.max(alpha,
                                quietMin(moveTransition.getTransitionBoard(), alpha, beta));
                    } else {
                        int moveScore = min(moveTransition.getTransitionBoard(), depth - 1,
                                alpha, beta);
                        alpha = Math.max(alpha, moveScore);
                        this.rememberedBoards.put(zobristHexHashCode, new BoardState(depth - 1, moveScore));
                        log.info("Boards' hash added: " + zobristHexHashCode + " on depth: " + (depth - 1));
                    }
                }
            }
            if (beta <= alpha) {
                break;
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
        return "AlphaBetaPruningWithMoveSorterAndTranspositionTable";
    }
}
