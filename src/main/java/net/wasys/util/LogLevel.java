package net.wasys.util;

import java.io.Serializable;

public enum LogLevel implements Serializable {

    FATAL(0),
    ERROR(1),
    WARN(2),
    PROF(3),
    INFO(3),
    DEBUG(4),
    FINE(5),
    FINER(6),
    FINEST(7);

    protected int precedence;

    LogLevel(int precedence) {
        this.precedence = precedence;
    }

    public int getPrecedence() {
        return precedence;
    }

    public boolean encompasses(LogLevel level) {
        return level.getPrecedence() <= this.getPrecedence();
    }
}
