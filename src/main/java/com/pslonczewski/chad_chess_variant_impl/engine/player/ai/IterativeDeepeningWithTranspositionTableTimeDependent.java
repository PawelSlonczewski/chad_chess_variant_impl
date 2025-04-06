package com.pslonczewski.chad_chess_variant_impl.engine.player.ai;

import com.pslonczewski.chad_chess_variant_impl.engine.board.*;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.*;

@Log4j2
public class IterativeDeepeningWithTranspositionTableTimeDependent implements MoveStrategy {
    private MoveSorter moveSorter = MoveSorter.SORT;
    private BoardEvaluator evaluator = new StandardBoardEvaluator();
    private long boardsEvaluated = 0;
    private long timer;
    private Thread timerThread;
    private Thread mainThread = Thread.currentThread();
    private final Map<String, BoardState> rememberedBoards;


    public IterativeDeepeningWithTranspositionTableTimeDependent(Map<String, BoardState> rememberedBoards, long timer) {
        this.timer = timer;
        this.rememberedBoards = rememberedBoards;
    }

    private enum MoveSorter {

        SORT {
            @Override
            Collection<Move> sort(final Collection<Move> moves) {
                return com.google.common.collect.Ordering.from(mvvLva).immutableSortedCopy(moves);
            }
        };

        public static Comparator<Move> mvvLva = new Comparator<Move>() {
            @Override
            public int compare(final Move move1, final Move move2) {
                if (!(move1.isAttack()) && !(move2.isAttack())) {
                    return 0;
                } else if (!(move1.isAttack())) {
                    return 1;
                } else if (!(move2.isAttack())) {
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
        throw new RuntimeException("Not implemented yet!");
    }

    @Override
    public Move execute(Board board, int depth) {
        System.out.println(board.getCurrentPlayer() + " THINKING with depth = " + depth);

        this.timerThread = new Thread(() -> {
            try {
                Thread.sleep(Duration.ofSeconds(timer).toMillis());
            } catch (InterruptedException e) {
                log.info("Timer was not needed");
            }
            mainThread.interrupt();
        });
        timerThread.start();

        MoveOrderingBuilder builder = new MoveOrderingBuilder();
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            builder.addMoveOrderingRecord(move, 0);
        }

        Move bestMove = null;
        int currentDepth = 1;

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        while (currentDepth <= depth) {

            if (Thread.currentThread().isInterrupted()) {
                log.info("Method interrupted");
                return bestMove != null ? bestMove : getDefaultMove(board);
            }

            int currentValue;
            final List<MoveScoreRecord> records = builder.build();
            builder = new MoveOrderingBuilder();
            for (final MoveScoreRecord record : records) {
                final Move move = record.getMove();
                final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
                if (moveTransition.getMoveStatus().isDone()) {
                    String boardHexString = Long.toHexString(moveTransition.getTransitionBoard().getZobristHashCode());
                    if (this.rememberedBoards.containsKey(boardHexString)
                        && (currentDepth - 1) <= this.rememberedBoards.get(boardHexString).depth()) {
                        currentValue = rememberedBoards.get(boardHexString).score();
                        log.info("Board' hash found in remembered board: {}", boardHexString);
                    } else {
                        currentValue = board.getCurrentPlayer().getAlliance().isWhite()
                                ? min(moveTransition.getTransitionBoard(), currentDepth - 1,
                                alpha, beta)
                                : max(moveTransition.getTransitionBoard(), currentDepth - 1,
                                alpha, beta);

                        this.rememberedBoards.put(boardHexString, new BoardState(currentDepth - 1, currentValue));
                    }
                    builder.addMoveOrderingRecord(move, currentValue);
                    if (board.getCurrentPlayer().getAlliance().isWhite() && currentValue > alpha) {
                        alpha = currentValue;
                        bestMove = move;
                    } else if (board.getCurrentPlayer().getAlliance().isBlack() && currentValue < beta) {
                        beta = currentValue;
                        bestMove = move;
                    }
                }
            }
            currentDepth++;
            String topBoardHexString = Long.toHexString(board.getZobristHashCode());
            this.rememberedBoards.put(topBoardHexString, new BoardState(depth,
                    board.getCurrentPlayer().getAlliance().isWhite()
                            ? alpha : beta));

            alpha = Integer.MIN_VALUE;
            beta = Integer.MAX_VALUE;

            if (Thread.currentThread().isInterrupted()) {
                log.info("Method interrupted");
                return bestMove != null ? bestMove : getDefaultMove(board);
            }
        }
        timerThread.interrupt();
        return bestMove;
    }

    private int min(final Board board, final int depth, final int alpha, int beta) {

        if (Thread.currentThread().isInterrupted()) {
            log.info("Method interrupted");
            return beta;
        }

        String boardHexString = Long.toHexString(board.getZobristHashCode());
        if (this.rememberedBoards.containsKey(boardHexString)
                && this.rememberedBoards.get(boardHexString).depth() >= depth) {
            log.info("Boards' hash found in remembered board: {}", boardHexString);
            return this.rememberedBoards.get(boardHexString).score();
        }

        if (depth == 0 || BoardUtils.isEndGameScenario(board)) {
            this.boardsEvaluated++;
            int evaluation = this.evaluator.evaluate(board, depth);
            this.rememberedBoards.put(boardHexString, new BoardState(depth, evaluation)); /* TODO Check if depth on end game scenario changes anything!!! */
            return evaluation;
        }

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves())) {
            if (Thread.currentThread().isInterrupted()) {
                log.info("Method interrupted");
                return beta;
            }

            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            String zobristHexHashCode = Long.toHexString(moveTransition.getTransitionBoard().getZobristHashCode());
            if (this.rememberedBoards.containsKey(zobristHexHashCode)
                    && this.rememberedBoards.get(zobristHexHashCode).depth() >= depth - 1) {
                log.info("Boards' hash found in remembered board: " + zobristHexHashCode);
                beta = Math.min(beta, this.rememberedBoards.get(zobristHexHashCode).score());
            }else {
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

        if (Thread.currentThread().isInterrupted()) {
            log.info("Method interrupted");
            return alpha;
        }

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
            return this.evaluator.evaluate(board, depth);
        }

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves())) {

            if (Thread.currentThread().isInterrupted()) {
                log.info("Method interrupted");
                return alpha;
            }

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

                if (Thread.currentThread().isInterrupted()) {
                    log.info("Method interrupted");
                    return beta;
                }

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

                if (Thread.currentThread().isInterrupted()) {
                    log.info("Method interrupted");
                    return beta;
                }

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

    private static class MoveScoreRecord implements Comparable<MoveScoreRecord> {
        private final Move move;
        private final int score;

        public MoveScoreRecord(Move move, int score) {
            this.move = move;
            this.score = score;
        }

        Move getMove() {
            return this.move;
        }

        int getScore() {
            return this.score;
        }

        @Override
        public int compareTo(MoveScoreRecord o) {
            return Integer.compare(this.score, o.score);
        }

        @Override
        public String toString() {
            return this.move + " : " + this.score;
        }
    }

    enum Ordering {
        ASC {
            @Override
            List<MoveScoreRecord> order(final List<MoveScoreRecord> moveScoreRecords) {
                Collections.sort(moveScoreRecords, new Comparator<MoveScoreRecord>() {
                    @Override
                    public int compare(final MoveScoreRecord o1,
                                       final MoveScoreRecord o2) {
                        return Integer.compare(o1.getScore(), o2.getScore());
                    }
                });
                return moveScoreRecords;
            }
        },
        DESC {
            @Override
            List<MoveScoreRecord> order(final List<MoveScoreRecord> moveScoreRecords) {
                Collections.sort(moveScoreRecords, new Comparator<MoveScoreRecord>() {
                    @Override
                    public int compare(final MoveScoreRecord o1,
                                       final MoveScoreRecord o2) {
                        return Integer.compare(o2.getScore(), o1.getScore());
                    }
                });
                return moveScoreRecords;
            }
        };

        abstract List<MoveScoreRecord> order(final List<MoveScoreRecord> moveScoreRecords);
    }

    private static class MoveOrderingBuilder {
        List<MoveScoreRecord> moveScoreRecords;
        Ordering ordering;

        MoveOrderingBuilder() {
            this.moveScoreRecords = new ArrayList<>();
        }

        void addMoveOrderingRecord(final Move move,
                                   final int score) {
            this.moveScoreRecords.add(new MoveScoreRecord(move, score));
        }

        List<MoveScoreRecord> build() {
            return Ordering.DESC.order(moveScoreRecords);
        }
    }

    private Move getDefaultMove(Board board) {
        Collection<Move> legalMoves = board.getCurrentPlayer().getLegalMoves();
        return legalMoves.isEmpty() ? null : legalMoves.stream().findFirst().get();
    }
}
