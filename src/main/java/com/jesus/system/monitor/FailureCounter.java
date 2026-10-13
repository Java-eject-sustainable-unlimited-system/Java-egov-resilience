package com.jesus.system.monitor;

final class FailureCounter {

    //@ spec_public
    private int consecutiveFailures;

    //@ invariant consecutiveFailures >= 0;

    //@ ensures consecutiveFailures == (\old(consecutiveFailures) < Integer.MAX_VALUE
    //@     ? \old(consecutiveFailures) + 1 : Integer.MAX_VALUE);
    //@ ensures \result == consecutiveFailures;
    int recordFailure() {
        if (consecutiveFailures < Integer.MAX_VALUE) {
            consecutiveFailures++;
        }
        return consecutiveFailures;
    }

    //@ ensures consecutiveFailures == Math.max(0, \old(consecutiveFailures) - 1);
    //@ ensures \result == consecutiveFailures;
    int recoverOne() {
        consecutiveFailures = Math.max(0, consecutiveFailures - 1);
        return consecutiveFailures;
    }

    //@ ensures \result == (consecutiveFailures >= threshold);
    boolean hasReached(int threshold) {
        return consecutiveFailures >= threshold;
    }

    //@ ensures consecutiveFailures == 0;
    void reset() {
        consecutiveFailures = 0;
    }
}
