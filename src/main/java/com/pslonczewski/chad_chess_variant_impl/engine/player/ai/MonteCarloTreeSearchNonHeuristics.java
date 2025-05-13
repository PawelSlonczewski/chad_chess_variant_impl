package com.pslonczewski.chad_chess_variant_impl.engine.player.ai;

import com.pslonczewski.chad_chess_variant_impl.engine.board.Board;
import com.pslonczewski.chad_chess_variant_impl.engine.board.BoardUtils;
import com.pslonczewski.chad_chess_variant_impl.engine.board.Move;
import com.pslonczewski.chad_chess_variant_impl.engine.board.MoveTransition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.StreamSupport;

@Log4j2
public class MonteCarloTreeSearchNonHeuristics implements MoveStrategy {

    private final Random random = new Random(31);
    private Node root;
    private Thread mainThread;
    private final long timer;

    public MonteCarloTreeSearchNonHeuristics(long timer) {
        this.timer = timer;
    }

    @Override
    public long getNumBoardsEvaluated() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public Move execute(Board board, int depth) {
        this.root = new Node(board, null, null);
        log.info("Monte carlo tree search THINKING for: {} seconds", this.timer);
        this.mainThread = Thread.currentThread();

        Thread.interrupted();

        Thread timerThread = new Thread(() -> {
            try {
                Thread.sleep(Duration.ofSeconds(this.timer).toMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                log.info("Monte carlo tree search interruption");
                mainThread.interrupt();
            }
        });
        timerThread.start();

        do {
            Node node = select(this.root);
            double reward = simulate(node);
            backpropagate(node, reward);
        } while (!Thread.currentThread().isInterrupted());
        return getBestMove();
    }

    private Node select(Node node) {
        log.trace("Entering select");
        while (!node.isEndGameScenario()) {
            if (!node.isFullyExpanded()) {
                Node expanded = node.expand();
                if (expanded != null) {
                    return expanded;
                }
            } else {
                node = node.selectChild();
            }
        }
        return node;
    }

    private double simulate(Node node) {
        log.trace("Entering simulate");
        Board board = node.getBoard();
        while (!BoardUtils.isEndGameScenario(board)) {
            List<Move> moves = StreamSupport.stream(board.getAllLegalMoves().spliterator(), false)
                                            .toList();
            Move randomMove;
            MoveTransition moveTransition;
            do {
                randomMove = moves.get(this.random.nextInt(moves.size()));
                moveTransition = board.getCurrentPlayer().makeMove(randomMove);
            } while (!moveTransition.getMoveStatus().isDone());
            board = moveTransition.getTransitionBoard();
        }

        return this.root.getBoard().getCurrentPlayer().getAlliance() == board.getCurrentPlayer().getAlliance() ? 0 : 1;
    }

    private void backpropagate(Node node, double reward) {
        while (node != null) {
            node.update(reward);
            node = node.getParent();
        }
    }

    private Move getBestMove() {
        Node bestChild = null;
        double bestScore = -1.0;
        for (Node child : root.getChildren()) {
            double score = child.getTotalReward() / child.getVisits();
            if (score > bestScore) {
                bestScore = score;
                bestChild = child;
            }
        }
        System.out.println("Best move: " + bestChild.getMove());
        return bestChild != null ? bestChild.getMove() : null;
    }

    @Getter
    private static class Node {

        @Getter(AccessLevel.NONE)
        private static final double EXPLORATION_CONSTANT = Math.sqrt(2.0);
        private final Board board;
        private final Node parent;
        private final Move move;
        private final List<Node> children = new ArrayList<>();
        @Getter(AccessLevel.NONE)
        private final List<Move> untriedMoves;
        private int visits = 0;
        private double totalReward = 0.0;

        public Node(Board board, Node parent, Move move) {
            this.board = board;
            this.parent = parent;
            this.move = move;
            this.untriedMoves = new ArrayList<>(board.getCurrentPlayer().getLegalMoves());
        }

        public boolean isFullyExpanded() {
            return this.untriedMoves.isEmpty();
        }

        public boolean isEndGameScenario() {
            return BoardUtils.isEndGameScenario(this.board);
        }

        public Node selectChild() {
            Node bestChild = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            for (Node child : this.children) {
                if (child.visits == 0) {
                    return child;
                }
                double score = (child.totalReward / child.visits)
                               + EXPLORATION_CONSTANT * Math.sqrt(Math.log(this.visits) / (double) child.visits);
                if (score > bestScore) {
                    bestChild = child;
                    bestScore = score;
                }
            }
            return bestChild;
        }

        public Node expand() {
            Move move;
            MoveTransition moveTransition;
            Random random = new Random();
            do {
                if (this.untriedMoves.isEmpty()) {
                    return null;
                }
                move = untriedMoves.remove(random.nextInt(this.untriedMoves.size()));
                moveTransition = board.getCurrentPlayer().makeMove(move);
            } while (!moveTransition.getMoveStatus().isDone());
            Node child = new Node(moveTransition.getTransitionBoard(), this, move);
            this.children.add(child);
            return child;
        }

        public void update(double reward) {
            visits++;
            totalReward += reward;
        }
    }
}
