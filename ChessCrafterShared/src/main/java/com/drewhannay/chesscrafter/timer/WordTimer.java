package com.drewhannay.chesscrafter.timer;

class WordTimer extends ChessTimer {
    public WordTimer(long startTime) {
        mCurrentTime = startTime;
        mInitialStartTime = startTime;
        init();
    }

    @Override
    public void startTimer() {
        mClockLastUpdatedTime = System.currentTimeMillis();
        if (mListener != null)
            mListener.onTimerStart();
    }

    @Override
    public void stopTimer() {
        mClockLastUpdatedTime = System.currentTimeMillis();
        updateDisplay();
        if (mListener != null)
            mListener.onTimerStop();
    }

    @Override
    public void timeElapsed() {
        mClockDirection = -1;
    }
}
