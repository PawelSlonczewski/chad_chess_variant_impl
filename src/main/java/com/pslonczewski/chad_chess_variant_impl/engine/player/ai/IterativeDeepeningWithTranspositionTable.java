package com.pslonczewski.chad_chess_variant_impl.engine.player.ai;

import com.pslonczewski.chad_chess_variant_impl.engine.board.*;
import com.pslonczewski.chad_chess_variant_impl.engine.board.BoardState.NodeType;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.*;

@Log4j2
public class IterativeDeepeningWithTranspositionTable implements MoveStrategy {
    private MoveSorter moveSorter = MoveSorter.SORT;
    private BoardEvaluator evaluator = new MyBoardEvaluator();
    private long boardsEvaluated = 0;
    private final Map<String, BoardState> rememberedBoards;
    private Move[][] killerMoves;
    private int depth;


    public IterativeDeepeningWithTranspositionTable(Map<String, BoardState> rememberedBoards, final int depth) {
        this.rememberedBoards = rememberedBoards;
        this.depth = depth;
        this.killerMoves = new Move[this.depth][2];
    }

    private enum MoveSorter {

        SORT {
            @Override
            Collection<Move> sort(final Collection<Move> moves, final int depth, final Move[][] killerMoves) {
                return com.google.common.collect.Ordering.from(new Comparator<Move>() {

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

    @Override
    public long getNumBoardsEvaluated() {
        throw new RuntimeException("Not implemented yet!");
    }

    @Override
    public Move execute(Board board, int depth) {
        System.out.println(board.getCurrentPlayer() + " THINKING with depth = " + depth);


        MoveOrderingBuilder builder = new MoveOrderingBuilder();
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            builder.addMoveOrderingRecord(move, 0);
        }

        Move bestMove = null;
        int currentDepth = 1;

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        long startTime = System.currentTimeMillis();

        while (currentDepth <= depth) {

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

                        this.rememberedBoards.put(boardHexString,
                                new BoardState(currentDepth - 1, currentValue,
                                        BoardState.getNodeType(alpha, beta, currentValue)
                                ));
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
//                String topBoardHexString = Long.toHexString(board.getZobristHashCode());
//                this.rememberedBoards.put(topBoardHexString, new BoardState(currentDepth,
//                        board.getCurrentPlayer().getAlliance().isWhite()
//                                ? alpha : beta)); // ?
            currentDepth++;

            alpha = Integer.MIN_VALUE;
            beta = Integer.MAX_VALUE;
        }
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("The best move was: " + bestMove);
        System.out.println("Time elapsed: " + endTime / 1000 + " s");
        return bestMove;
    }

    private int min(final Board board, final int depth, int alpha, int beta) {
        String boardHexString = Long.toHexString(board.getZobristHashCode());
        if (this.rememberedBoards.containsKey(boardHexString)
                && this.rememberedBoards.get(boardHexString).depth() >= depth) {
            log.info("Boards' hash found in remembered board: {}", boardHexString);
            BoardState boardState = rememberedBoards.get(boardHexString);
            switch (boardState.nodeType()) {
                case EXACT: return boardState.score();
                case LOWER: alpha = Math.max(alpha, boardState.score()); break;
                case UPPER: beta = Math.min(beta, boardState.score()); break;
            }
            if (alpha >= beta) return boardState.nodeType() == NodeType.LOWER ? beta : alpha;
        }

        if (depth == 0 || BoardUtils.isEndGameScenario(board)) {
            this.boardsEvaluated++;
            int evaluation = this.evaluator.evaluate(board, depth);
            this.rememberedBoards.put(boardHexString, new BoardState(depth, evaluation, NodeType.EXACT)); /* TODO Check if depth on end game scenario changes anything!!! */
            return evaluation;
        }

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves(), depth, killerMoves)) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            String zobristHexHashCode = Long.toHexString(moveTransition.getTransitionBoard().getZobristHashCode());
            if (moveTransition.getMoveStatus().isDone()) {
                if (depth == 1 && move.isAttack()) {
                    beta = Math.min(beta,
                            quietMax(moveTransition.getTransitionBoard(), alpha, beta));
                } else {
                    int moveScore = max(moveTransition.getTransitionBoard(), depth - 1,
                            alpha, beta);
                    beta = Math.min(beta, moveScore);
                }

                if (beta <= alpha) {
                    this.storeKillerMove(move, depth);
                    break;
                }
            }

        }

        this.rememberedBoards.put(boardHexString, new BoardState(depth, beta, alpha >= beta ? NodeType.LOWER : NodeType.EXACT));
        return beta;
    }

    private int max(final Board board, final int depth, int alpha, int beta) {
        String boardHexString = Long.toHexString(board.getZobristHashCode());
        if (this.rememberedBoards.containsKey(boardHexString)
                && this.rememberedBoards.get(boardHexString).depth() >= depth) {
            BoardState boardState = rememberedBoards.get(boardHexString);
            switch (boardState.nodeType()) {
                case EXACT: return boardState.score();
                case LOWER: alpha = Math.max(alpha, boardState.score()); break;
                case UPPER: beta = Math.min(beta, boardState.score()); break;
            }
            if (alpha >= beta) return boardState.nodeType() == NodeType.LOWER ? beta : alpha;
        }

        if (depth == 0 || BoardUtils.isEndGameScenario(board)) {
            this.boardsEvaluated++;
            int evaluation = this.evaluator.evaluate(board, depth);
            this.rememberedBoards.put(boardHexString, new BoardState(depth, evaluation, NodeType.EXACT));
            return evaluation;
        }

        for (Move move : this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves(),depth, killerMoves)) {
            MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            String zobristHexHashCode = Long.toHexString(moveTransition.getTransitionBoard().getZobristHashCode());

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
            if (beta <= alpha) {
                this.storeKillerMove(move, depth);
                break;
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

    private void storeKillerMove(final Move move, final int depth) {
        if (move.isAttack()) {
            return;
        }

        if (!move.equals(this.killerMoves[depth][0])) {
            this.killerMoves[depth - 1][1] = this.killerMoves[depth][0];
            this.killerMoves[depth - 1][0] = move;
        }
    }

    private Move getDefaultMove(Board board) {
        Collection<Move> legalMoves = board.getCurrentPlayer().getLegalMoves();
        return legalMoves.isEmpty() ? null : legalMoves.stream().findFirst().get();
    }
}
