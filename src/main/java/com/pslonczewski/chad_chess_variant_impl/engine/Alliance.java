package com.pslonczewski.chad_chess_variant_impl.engine;

import com.pslonczewski.chad_chess_variant_impl.engine.board.BoardUtils;
import com.pslonczewski.chad_chess_variant_impl.engine.player.BlackPlayer;
import com.pslonczewski.chad_chess_variant_impl.engine.player.Player;
import com.pslonczewski.chad_chess_variant_impl.engine.player.WhitePlayer;

public enum Alliance {
    WHITE {
        @Override
        public boolean isWhite() {
            return true;
        }

        @Override
        public boolean isBlack() {
            return false;
        }

        @Override
        public boolean isCastleTile(int coordinate) {
            return BoardUtils.WHITE_CASTLE[coordinate];
        }

        @Override
        public boolean isWallTile(int coordinate) {
            return BoardUtils.WHITE_WALL[coordinate];
        }

        @Override
        public boolean isPromotionTile(int coordinate) {
            return BoardUtils.BLACK_CASTLE[coordinate];
        }

        @Override
        public Player choosePlayer(final WhitePlayer whitePlayer, final BlackPlayer blackPlayer) {
            return whitePlayer;
        }
    },
    BLACK {
        @Override
        public boolean isWhite() {
            return false;
        }

        @Override
        public boolean isBlack() {
            return true;
        }

        @Override
        public boolean isCastleTile(int coordinate) {
            return BoardUtils.BLACK_CASTLE[coordinate];
        }

        @Override
        public boolean isWallTile(int coordinate) {
            return BoardUtils.BLACK_WALL[coordinate];
        }

        @Override
        public boolean isPromotionTile(int coordinate) {
            return BoardUtils.WHITE_CASTLE[coordinate];
        }

        @Override
        public Player choosePlayer(final WhitePlayer whitePlayer, final BlackPlayer blackPlayer) {
            return blackPlayer;
        }
    };

    public abstract boolean isWhite();
    public abstract boolean isBlack();
    public abstract boolean isCastleTile(int coordinate);
    public abstract boolean isWallTile(int coordinate);
    public abstract boolean isPromotionTile(int coordinate);

    public abstract Player choosePlayer(WhitePlayer whitePlayer, BlackPlayer blackPlayer);
}
