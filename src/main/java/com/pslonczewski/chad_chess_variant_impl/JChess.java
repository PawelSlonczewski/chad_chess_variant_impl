package com.pslonczewski.chad_chess_variant_impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pslonczewski.chad_chess_variant_impl.gui.Table;


public class JChess {

    private static final Logger logger = LogManager.getLogger(JChess.class);

    public static void main(String[] args) {

//        Board board = Board.createStandardBoard();
//
//        System.out.println(board);

        Table.get().show();

    }
}
