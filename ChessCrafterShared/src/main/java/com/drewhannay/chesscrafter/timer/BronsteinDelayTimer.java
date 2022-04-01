package com.drewhannay.chesscrafter.timer;

class BronsteinDelayTimer extends ChessTimer {
    public BronsteinDelayTimer(long incrementAmount, long currentTime, boolean isBlackTeamTimer) {
        mIncrementAmount = incrementAmount;
        mIsBlackTeamTimer = isBlackTeamTimer;
        mCurrentTime = currentTime;
        mInitialStartTime = mCurrentTime;
        mClockLastUpdatedTime = System.currentTimeMillis();
    }

    @Override
    public void startTimer() {
        mClockLastUpdatedTime = System.currentTimeMillis();
        mLagTime = System.currentTimeMillis();
        if (mListener != null)
            mListener.onTimerStart();
    }

    @Override
    public void reset() {
        mClockLastUpdatedTime = System.currentTimeMillis();
        mCurrentTime = mInitialStartTime;
        updateDisplay();
        mIsFirstRun = true;
    }

    @Override
    public void stopTimer() {
        mClockLastUpdatedTime = System.currentTimeMillis();
        long delay = System.currentTimeMillis() - mLagTime;

        if (!mIsFirstRun && !mTimeWasRecentlyReset)
            mCurrentTime += (delay >= mIncrementAmount ? mIncrementAmount : delay);
        else
            mIsFirstRun = false;

        mTimeWasRecentlyReset = false;
        updateDisplay();
        if (mListener != null)
            mListener.onTimerStop();
    }

    private long mIncrementAmount;
    private boolean mIsFirstRun = true;
    private long mLagTime;
}
