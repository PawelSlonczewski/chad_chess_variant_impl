package com.pslonczewski.chad_chess_variant_impl.engine.board;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pslonczewski.chad_chess_variant_impl.engine.Alliance;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.*;
import com.pslonczewski.chad_chess_variant_impl.engine.pieces.Piece.PieceType;
import com.pslonczewski.chad_chess_variant_impl.engine.player.BlackPlayer;
import com.pslonczewski.chad_chess_variant_impl.engine.player.Player;
import com.pslonczewski.chad_chess_variant_impl.engine.player.WhitePlayer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Board {

    private final List<Tile> gameBoard;
    private final Collection<Piece> whitePieces;
    private final Collection<Piece> blackPieces;

    private final WhitePlayer whitePlayer;
    private final BlackPlayer blackPlayer;
    private final Player currentPlayer;
    private final long zobristHashCode;

    private Board(final Builder builder) {
        this.gameBoard = createGameBoard(builder);
        this.whitePieces = calculateActivePieces(this.gameBoard, Alliance.WHITE);
        this.blackPieces = calculateActivePieces(this.gameBoard, Alliance.BLACK);

        final Collection<Move> whiteStandardLegalMoves = calculateLegalMoves(this.whitePieces);
        final Collection<Move> blackStandardLegalMoves = calculateLegalMoves(this.blackPieces);

        this.whitePlayer = new WhitePlayer(this, whiteStandardLegalMoves, blackStandardLegalMoves);
        this.blackPlayer = new BlackPlayer(this, whiteStandardLegalMoves, blackStandardLegalMoves);

        this.currentPlayer = builder.nextMoveMaker.choosePlayer(this.whitePlayer, this.blackPlayer);

        this.zobristHashCode = calculateZobristHashCode(this.whitePieces, this.blackPieces, this.currentPlayer);
//        System.out.println("[FROM CONSTRUCTOR] Hash code for this board: " + this.zobristHashCode);
    }

    private long calculateZobristHashCode(Collection<Piece> whitePieces, Collection<Piece> blackPieces, Player currentPlayer) {
        long hash = 0L;
        for (final Piece piece : Iterables.concat(whitePieces, blackPieces)) {
            int tileCoordinate = piece.getPiecePosition() * PieceType.values().length * 2;
            int pieceAlliance = piece.getPieceAlliance() == Alliance.WHITE ? 0 : 3;
            int pieceType = piece.getPieceType() == PieceType.ROOK ? 0
                            : piece.getPieceType() == PieceType.QUEEN ? 1 : 2;
            hash ^= BoardUtils.ZOBRIST_TABLE[tileCoordinate + pieceAlliance + pieceType];
        }

        return (hash ^ (currentPlayer.getAlliance().isWhite() ? 0 : 1)) /* & 0x7FFFFFFFFFFFFFFFL */;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            final String tileText = this.gameBoard.get(i).toString();
            builder.append(String.format("%3s", tileText));
            if ((i + 1) % BoardUtils.NUM_TILES_PER_ROW == 0) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public Player getWhitePlayer() {
        return this.whitePlayer;
    }

    public Player getBlackPlayer() {
        return this.blackPlayer;
    }

    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    public Piece getPiece(final int coordinate) {
        return this.gameBoard.get(coordinate).getPiece();
    }

    public Collection<Piece> getBlackPieces() {
        return this.blackPieces;
    }

    public Collection<Piece> getWhitePieces() {
        return this.whitePieces;
    }

    public Collection<Piece> getAllPieces() {
        return Stream.concat(this.whitePieces.stream(),
                this.blackPieces.stream()).collect(Collectors.toList());
    }

    public boolean isADraw() {
        return this.getAllPieces().size() == 2;
    }

    private Collection<Move> calculateLegalMoves(final Collection<Piece> pieces) {

        final List<Move> legalMoves = new ArrayList<>();

        for (final Piece piece : pieces) {
            legalMoves.addAll(piece.calculateLegalMoves(this));
        }

        return ImmutableList.copyOf(legalMoves);
    }

    private static Collection<Piece> calculateActivePieces(final List<Tile> gameBoard, final Alliance alliance) {

        final List<Piece> activePieces = new ArrayList<>();

        for (final Tile tile : gameBoard) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() == alliance) {
                    activePieces.add(piece);
                }
            }
        }

        return ImmutableList.copyOf(activePieces);
    }

    public Tile getTile(final int tileCoordinate) {
        return this.gameBoard.get(tileCoordinate);
    }

    private static List<Tile> createGameBoard(final Builder boardBuilder) {
        final Tile[] tiles = new Tile[BoardUtils.NUM_TILES];

        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            tiles[i] = Tile.createTile(i, boardBuilder.boardConfig.get(i));
        }

        return ImmutableList.copyOf(tiles);
    }

    public static Board createStandardBoard() {
        final Builder builder = new Builder();
        // Black Layout
        builder.setPiece(new Rook(Alliance.BLACK, 31));
        builder.setPiece(new Rook(Alliance.BLACK, 32));
        builder.setPiece(new Rook(Alliance.BLACK, 33));
        builder.setPiece(new Rook(Alliance.BLACK, 43));
        builder.setPiece(new King(Alliance.BLACK, 44));
        builder.setPiece(new Rook(Alliance.BLACK, 45));
        builder.setPiece(new Rook(Alliance.BLACK, 55));
        builder.setPiece(new Rook(Alliance.BLACK, 56));
        builder.setPiece(new Rook(Alliance.BLACK, 57));


        //White Layout
        builder.setPiece(new Rook(Alliance.WHITE, 86));
        builder.setPiece(new Rook(Alliance.WHITE, 87));
        builder.setPiece(new Rook(Alliance.WHITE, 88));
        builder.setPiece(new Rook(Alliance.WHITE, 98));
        builder.setPiece(new King(Alliance.WHITE, 99));
        builder.setPiece(new Rook(Alliance.WHITE, 100));
        builder.setPiece(new Rook(Alliance.WHITE, 110));
        builder.setPiece(new Rook(Alliance.WHITE, 111));
        builder.setPiece(new Rook(Alliance.WHITE, 112));

        builder.setMoveMaker(Alliance.WHITE);

        return builder.build();
    }

    public static Board createProblem1Board() {
        final Builder builder = new Builder();

        builder.setPiece(new Rook(Alliance.BLACK, 26));
        builder.setPiece(new Rook(Alliance.BLACK, 28));
        builder.setPiece(new Rook(Alliance.BLACK, 33));
        builder.setPiece(new Rook(Alliance.BLACK, 43));
        builder.setPiece(new King(Alliance.BLACK, 44));
        builder.setPiece(new Rook(Alliance.BLACK, 56));
        builder.setPiece(new Rook(Alliance.BLACK, 57));
        builder.setPiece(new Rook(Alliance.WHITE, 63));
        builder.setPiece(new Rook(Alliance.WHITE, 86));
        builder.setPiece(new Rook(Alliance.WHITE, 88));
        builder.setPiece(new King(Alliance.WHITE, 98));
        builder.setPiece(new Rook(Alliance.WHITE, 100));
        builder.setPiece(new Rook(Alliance.BLACK, 105));
        builder.setPiece(new Rook(Alliance.WHITE, 110));
        builder.setPiece(new Rook(Alliance.WHITE, 114));
        builder.setPiece(new Rook(Alliance.WHITE, 115));

        builder.setMoveMaker(Alliance.WHITE);

        return builder.build();
    }

    public Iterable<Move> getAllLegalMoves() {
        return Iterables.unmodifiableIterable(
                Iterables.concat(this.whitePlayer.getLegalMoves(), this.blackPlayer.getLegalMoves())
        );
    }

    public long getZobristHashCode() {
        return this.zobristHashCode;
    }

    public static class Builder {

        Map<Integer, Piece> boardConfig;
        Alliance nextMoveMaker;

        public Builder() {
            this.boardConfig = new HashMap<>();
        }

        public Builder setPiece(final Piece piece) {
            this.boardConfig.put(piece.getPiecePosition(), piece);
            return this;
        }

        public Builder setMoveMaker(final Alliance nextMoveMaker) {
            this.nextMoveMaker = nextMoveMaker;
            return this;
        }

        public Board build() {
            return new Board(this);
        }
    }
}
