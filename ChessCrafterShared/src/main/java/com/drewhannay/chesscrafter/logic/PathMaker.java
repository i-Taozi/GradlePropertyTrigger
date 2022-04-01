package com.drewhannay.chesscrafter.logic;

import com.drewhannay.chesscrafter.models.BoardCoordinate;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathMaker {
    private final BoardCoordinate mOrigin;
    private final BoardCoordinate mDestination;

    public PathMaker(BoardCoordinate origin, BoardCoordinate destination) {
        mOrigin = origin;
        mDestination = destination;
    }

    public List<BoardCoordinate> getPathToDestination() {
        return getPathToDestination(Integer.MAX_VALUE);
    }

    public List<BoardCoordinate> getPathToDestination(int maxSteps) {
        if (mOrigin.isOnSameHorizontalPathAs(mDestination)) {
            return getHorizontalPathSpaces(maxSteps);
        } else if (mOrigin.isOnSameVerticalPathAs(mDestination)) {
            return getVerticalPathSpaces(maxSteps);
        } else if (mOrigin.isOnSameDiagonalPathAs(mDestination)) {
            return getDiagonalPathSpaces(maxSteps);
        } else {
            return Collections.emptyList();
        }
    }

    private List<BoardCoordinate> getHorizontalPathSpaces(int maxSteps) {
        int least = Math.min(mOrigin.x, mDestination.x);
        int most = Math.max(mOrigin.x, mDestination.x);

        List<BoardCoordinate> spaces = new ArrayList<>();
        for (int x = least; x <= most; x++) {
            if (Math.abs(mOrigin.x - x) <= maxSteps) {
                spaces.add(BoardCoordinate.at(x, mOrigin.y));
            }
        }
        // don't include mOrigin in the path
        spaces.remove(mOrigin);

        int direction = (int) Math.signum(mDestination.x - mOrigin.x);
        if (direction < 0) {
            spaces = Lists.reverse(spaces);
        }

        return spaces;
    }

    private List<BoardCoordinate> getVerticalPathSpaces(int maxSteps) {
        int least = Math.min(mOrigin.y, mDestination.y);
        int most = Math.max(mOrigin.y, mDestination.y);

        List<BoardCoordinate> spaces = new ArrayList<>();
        for (int y = least; y <= most; y++) {
            if (Math.abs(mOrigin.y - y) <= maxSteps) {
                spaces.add(BoardCoordinate.at(mOrigin.x, y));
            }
        }
        // don't include mOrigin in the path
        spaces.remove(mOrigin);

        int direction = (int) Math.signum(mDestination.y - mOrigin.y);
        if (direction < 0) {
            spaces = Lists.reverse(spaces);
        }

        return spaces;
    }

    private List<BoardCoordinate> getDiagonalPathSpaces(int maxSteps) {
        int absoluteDistance = Math.abs(mOrigin.x - mDestination.x);
        int xDirection = (mDestination.x - mOrigin.x) / absoluteDistance;
        int yDirection = (mDestination.y - mOrigin.y) / absoluteDistance;

        List<BoardCoordinate> spaces = new ArrayList<>();
        for (int index = 1; index <= absoluteDistance && index <= maxSteps; index++) {
            spaces.add(BoardCoordinate.at(mOrigin.x + index * xDirection, mOrigin.y + index * yDirection));
        }
        return spaces;
    }
}
