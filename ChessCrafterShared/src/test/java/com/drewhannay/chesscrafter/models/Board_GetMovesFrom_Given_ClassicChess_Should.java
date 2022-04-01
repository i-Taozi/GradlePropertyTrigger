package com.drewhannay.chesscrafter.models;

import com.drewhannay.chesscrafter.logic.GameBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Board_GetMovesFrom_Given_ClassicChess_Should {

    Board mTarget;

    @Before
    public void setup() {
        mTarget = new Board(BoardSize.CLASSIC_SIZE);

        GameBuilder.setupClassicPieces(mTarget, 1, Piece.TEAM_ONE);
        GameBuilder.setupClassicNorthFacingPawns(mTarget, 2, Piece.TEAM_ONE);

        GameBuilder.setupClassicSouthFacingPawns(mTarget, 7, Piece.TEAM_TWO);
        GameBuilder.setupClassicPieces(mTarget, 8, Piece.TEAM_TWO);
    }

    @Test
    public void return1_3ForAPawnAt1_2() {
        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(1, 2));
        assertTrue(moves.contains(BoardCoordinate.at(1, 3)));
    }

    @Test
    public void return1_4ForAPawnAt1_2ThatHasNotMoved() {
        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(1, 2));
        assertTrue(moves.contains(BoardCoordinate.at(1, 4)));
    }

    @Test
    public void notReturn1_3ForAPawnAt1_2WhenOpposingPawnIsAt1_3() {
        mTarget.addPiece(Piece.newSouthFacingPawn(Piece.TEAM_TWO), BoardCoordinate.at(1, 3));

        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(1, 2));
        assertFalse(moves.contains(BoardCoordinate.at(1, 3)));
    }

    @Test
    public void notReturn1_4ForAPawnAt1_2WhenOpposingPawnIsAt1_4() {
        mTarget.addPiece(Piece.newSouthFacingPawn(Piece.TEAM_TWO), BoardCoordinate.at(1, 4));

        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(1, 2));
        assertFalse(moves.contains(BoardCoordinate.at(1, 4)));
    }

    @Test
    public void notReturn1_3ForAPawnAt2_2() {
        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(2, 2));
        assertFalse(moves.contains(BoardCoordinate.at(1, 3)));
    }

    @Test
    public void return1_3ForAPawnAt2_2WhenOpposingPawnIsAt1_3() {
        mTarget.addPiece(Piece.newSouthFacingPawn(Piece.TEAM_TWO), BoardCoordinate.at(1, 3));

        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(2, 2));
        assertTrue(moves.contains(BoardCoordinate.at(1, 3)));
    }

    @Test
    public void return3_3ForAPawnAt2_2WhenOpposingPawnIsAt3_3() {
        mTarget.addPiece(Piece.newSouthFacingPawn(Piece.TEAM_TWO), BoardCoordinate.at(3, 3));

        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(2, 2));
        assertTrue(moves.contains(BoardCoordinate.at(3, 3)));
    }

    @Test
    public void notReturn1_6ForAPawnAt2_7() {
        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(2, 7));
        assertFalse(moves.contains(BoardCoordinate.at(1, 6)));
    }

    @Test
    public void return1_6ForAPawnAt2_7WhenOpposingPawnIsAt1_6() {
        mTarget.addPiece(Piece.newNorthFacingPawn(Piece.TEAM_ONE), BoardCoordinate.at(1, 6));

        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(2, 7));
        assertTrue(moves.contains(BoardCoordinate.at(1, 6)));
    }

    @Test
    public void return3_6ForAPawnAt2_7WhenOpposingPawnIsAt3_6() {
        mTarget.addPiece(Piece.newNorthFacingPawn(Piece.TEAM_ONE), BoardCoordinate.at(3, 6));

        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(2, 7));
        assertTrue(moves.contains(BoardCoordinate.at(3, 6)));
    }

    @Test
    public void notReturn1_6ForAPawnAt1_7WhenOpposingPawnIsAt1_6() {
        mTarget.addPiece(Piece.newSouthFacingPawn(Piece.TEAM_ONE), BoardCoordinate.at(1, 6));

        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(1, 7));
        assertFalse(moves.contains(BoardCoordinate.at(1, 6)));
    }

    @Test
    public void notReturn1_5ForAPawnAt1_2WhenOpposingPawnIsAt1_5() {
        mTarget.addPiece(Piece.newSouthFacingPawn(Piece.TEAM_ONE), BoardCoordinate.at(1, 5));

        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(1, 7));
        assertFalse(moves.contains(BoardCoordinate.at(1, 5)));
    }

    @Test
    public void returnEmptySetForRookAt1_1() {
        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(1, 1));
        assertTrue(moves.isEmpty());
    }

    @Test
    public void returnEmptySetForRookAt8_1() {
        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(8, 1));
        assertTrue(moves.isEmpty());
    }

    @Test
    public void returnEmptySetForRookAt8_8() {
        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(8, 8));
        assertTrue(moves.isEmpty());
    }

    @Test
    public void returnSetWith3_3ForKnightAt2_1() {
        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(2, 1));
        assertTrue(moves.contains(BoardCoordinate.at(3, 3)));
    }

    @Test
    public void returnSetWith1_3ForKnightAt2_1() {
        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(2, 1));
        assertTrue(moves.contains(BoardCoordinate.at(1, 3)));
    }

    @Test
    public void notReturn4_2ForKnightAt2_1() {
        Set<BoardCoordinate> moves = mTarget.getMovesFrom(BoardCoordinate.at(2, 1));
        assertFalse(moves.contains(BoardCoordinate.at(4, 2)));
    }
}
