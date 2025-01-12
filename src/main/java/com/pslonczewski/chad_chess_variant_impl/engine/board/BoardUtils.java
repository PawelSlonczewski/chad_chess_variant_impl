package com.pslonczewski.chad_chess_variant_impl.engine.board;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class BoardUtils {

    public static final boolean[] FIRST_COLUMN = initColumn(0);
    public static final boolean[] SECOND_COLUMN = initColumn(1);
    public static final boolean[] SEVENTH_COLUMN = initColumn(6);
    public static final boolean[] EIGHTH_COLUMN = initColumn(7);
    public static final boolean[] TWELFTH_COLUMN = initColumn(11);

    public static final boolean[] TWELFTH_RANK = initRow(0);
    public static final boolean[] ELEVENTH_RANK = initRow(1);
    public static final boolean[] TENTH_RANK = initRow(2);
    public static final boolean[] NINTH_RANK = initRow(3);
    public static final boolean[] EIGHTH_RANK = initRow(4);
    public static final boolean[] SEVENTH_RANK = initRow(5);
    public static final boolean[] SIXTH_RANK = initRow(6);
    public static final boolean[] FIFTH_RANK = initRow(7);
    public static final boolean[] FOURTH_RANK = initRow(8);
    public static final boolean[] THIRD_RANK = initRow(9);
    public static final boolean[] SECOND_RANK = initRow(10);
    public static final boolean[] FIRST_RANK = initRow(11);

    public static final boolean[] WHITE_CASTLE = initCastle(-1);
    public static final boolean[] BLACK_CASTLE = initCastle(1);

    public static final boolean[] WHITE_WALL = initWall(-1);
    public static final boolean[] BLACK_WALL = initWall(1);

    private static boolean[] initWall(final int alliance) {
        if (alliance != -1 && alliance != 1) {
            throw new RuntimeException("Alliance error in BoardUtils");
        }
        boolean[] result = new boolean[NUM_TILES];

        if (alliance == -1) {
            result[74] = true;
            result[75] = true;
            result[76] = true;
            result[85] = true;
            result[89] = true;
            result[97] = true;
            result[101] = true;
            result[109] = true;
            result[113] = true;
            result[122] = true;
            result[123] = true;
            result[124] = true;
        } else {
            result[19] = true;
            result[20] = true;
            result[21] = true;
            result[30] = true;
            result[34] = true;
            result[42] = true;
            result[46] = true;
            result[54] = true;
            result[58] = true;
            result[67] = true;
            result[68] = true;
            result[69] = true;
        }

        return result;
    }



    public static final String[] ALGEBRAIC_NOTATION = initializeAlgebraicNotation();
    public static final Map<String, Integer> POSITION_TO_COORDINATE = initializePositionToCoordinateMap();

    public static final int NUM_TILES = 144;
    public static final int NUM_TILES_PER_ROW = 12;

    private BoardUtils() {
        throw new RuntimeException("BoardUtils class cannot be instantiated!");
    }

    private static String[] initializeAlgebraicNotation() {
        return new String[] {
                "a12", "b12", "c12", "d12", "e12", "f12", "g12", "h12", "i12", "j12", "k12", "l12",
                "a11", "b11", "c11", "d11", "e11", "f11", "g11", "h11", "i11", "j11", "k11", "l11",
                "a10", "b10", "c10", "d10", "e10", "f10", "g10", "h10", "i10", "j10", "k10", "l10",
                "a9", "b9", "c9", "d9", "e9", "f9", "g9", "h9", "i9", "j9", "k9", "l9",
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8", "i8", "j8", "k8", "l8",
                "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7", "i7", "j7", "k7", "l7",
                "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6", "i6", "j6", "k6", "l6",
                "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5", "i5", "j5", "k5", "l5",
                "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4", "i4", "j4", "k4", "l4",
                "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3", "i3", "j3", "k3", "l3",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2", "i2", "j2", "k2", "l2",
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1", "i1", "j1", "k1", "l1"
        };
    }

    private static Map<String, Integer> initializePositionToCoordinateMap() {
        final Map<String, Integer> positionToCoordinate = new HashMap<>();

        for (int i = 0; i < NUM_TILES; i++) {
            positionToCoordinate.put(ALGEBRAIC_NOTATION[i], i);
        }

        return ImmutableMap.copyOf(positionToCoordinate);
    }


    private static boolean[] initCastle(final int alliance) {
        // -1 for white and 1 for black
        if (alliance != -1 && alliance != 1) {
            throw new RuntimeException("Alliance error in BoardUtils");
        }

        boolean[] result = new boolean[NUM_TILES];

        int start = (alliance == -1) ? 86 : 31;

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                result[start + j] = true;
            }
            start += NUM_TILES_PER_ROW;
        }

        return result;
    }

    public static boolean isMoveWithinBoardBounds(final int coordinate) {
        return coordinate >= 0 && coordinate < NUM_TILES;
    }

    private static boolean[] initColumn(int columnNumber) {
        final boolean[] column = new boolean[NUM_TILES];

        do {
            column[columnNumber] = true;
            columnNumber += NUM_TILES_PER_ROW;
        } while (columnNumber < NUM_TILES);

        return column;
    }

    private static boolean[] initRow(final int rowNumber) {
        final boolean[] row = new boolean[NUM_TILES];
        final int rowStart = rowNumber * BoardUtils.NUM_TILES_PER_ROW;

        for (int i = rowStart; i < rowStart + BoardUtils.NUM_TILES_PER_ROW; i++) {
            row[i] = true;
        }

        return row;
    }

    public static int getCoordinateAtPosition(final String position) {
        return POSITION_TO_COORDINATE.get(position);
    }

    public static String getPositionAtCoordinate(final int coordinate) {
        return ALGEBRAIC_NOTATION[coordinate];
    }
}
