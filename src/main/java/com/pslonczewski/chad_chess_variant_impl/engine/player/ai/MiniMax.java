package com.pslonczewski.chad_chess_variant_impl.engine.player.ai;

import com.pslonczewski.chad_chess_variant_impl.engine.board.Board;
import com.pslonczewski.chad_chess_variant_impl.engine.board.BoardUtils;
import com.pslonczewski.chad_chess_variant_impl.engine.board.Move;
import com.pslonczewski.chad_chess_variant_impl.engine.board.MoveTransition;

import java.util.concurrent.atomic.AtomicLong;

public class MiniMax implements MoveStrategy {

    private final BoardEvaluator boardEvaluator;
    private long boardsEvaluated = 0;
    private long executionTime;
    private FreqTableRow[] freqTable;
    private int freqTableIndex;

    public MiniMax() {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.boardsEvaluated = 0;
    }

    @Override
    public String toString() {
        return "MiniMax";
    }

    @Override
    public long getNumBoardsEvaluated() {
        return this.boardsEvaluated;
    }

    @Override
    public Move execute(final Board board, final int depth) {
        final long startTime = System.currentTimeMillis() / 1000;
        Move bestMove = null;
        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;

        System.out.println(board.getCurrentPlayer() + " THINKING with depth = " + depth);

        this.freqTable = new FreqTableRow[board.getCurrentPlayer().getLegalMoves().size()];
        this.freqTableIndex = 0;
        int moveCounter = 0;
        int numMoves = board.getCurrentPlayer().getLegalMoves().size();

        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final FreqTableRow row = new FreqTableRow(move);
                this.freqTable[this.freqTableIndex] = row;
                currentValue = board.getCurrentPlayer().getAlliance().isWhite()
                        ? min(moveTransition.getTransitionBoard(), depth - 1)
                        : max(moveTransition.getTransitionBoard(), depth - 1);

                System.out.println("\t" + this.toString() + " analyzing move (" + moveCounter + "/"
                        + numMoves + ") " + move + " scores " + currentValue + " "
                        + this.freqTable[this.freqTableIndex]);
                this.freqTableIndex++;
                if (board.getCurrentPlayer().getAlliance().isWhite() && currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                    bestMove = move;

                } else if (board.getCurrentPlayer().getAlliance().isBlack() && currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                    bestMove = move;
                }
            } else {
                System.out.println("\t" + this.toString() + " can't execute move ("
                        + moveCounter + "/" + numMoves + ") " + move);
            }
            moveCounter++;
        }
        this.executionTime = (System.currentTimeMillis() / 1000) - startTime;

        System.out.printf("%s SELECTS %s [#boards = %d, time taken = %d s, rate = %.1f\n",
                board.getCurrentPlayer(), bestMove, this.boardsEvaluated, this.executionTime,
                (1000 * ((double)this.boardsEvaluated/this.executionTime)));

        long total = 0;
        for (final FreqTableRow row : this.freqTable) {
            if (row != null) {
                total += row.getCount();
            }
        }

        if (this.boardsEvaluated != total) {
            System.out.println("something wrong with the # of boards evaluated!");
        }

        return bestMove;
    }

    public int min(final Board board, final int depth) {

        if (depth == 0) {
            this.boardsEvaluated++;
            this.freqTable[this.freqTableIndex].increment();
            return this.boardEvaluator.evaluate(board, depth);
        }

        if (BoardUtils.isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }

        int lowestSeenValue = Integer.MAX_VALUE;

        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = max(moveTransition.getTransitionBoard(), depth - 1);
                if (currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                }
            }
        }

        return lowestSeenValue;
    }

    public int max(final Board board, final int depth) {

        if (depth == 0) {
            this.boardsEvaluated++;
            this.freqTable[this.freqTableIndex].increment();
            return this.boardEvaluator.evaluate(board, depth);
        }

        if (BoardUtils.isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }

        int highestSeenValue = Integer.MIN_VALUE;

        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = min(moveTransition.getTransitionBoard(), depth - 1);
                if (currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                }
            }
        }

        return highestSeenValue;
    }

    private static class FreqTableRow {
        private Move move;
        private final AtomicLong count;

        FreqTableRow(final Move move) {
            this.count = new AtomicLong();
            this.move = move;
        }

        public long getCount() {
            return this.count.get();
        }

        public void increment() {
            this.count.incrementAndGet();
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.move.getCurrentCoordinate())
                    + BoardUtils.getPositionAtCoordinate(move.getDestinationCoordinate()) + " : "
                    + this.count;
        }
    }
}
