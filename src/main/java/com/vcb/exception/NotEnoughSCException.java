package com.vcb.exception;

public class NotEnoughSCException extends Exception {
    public NotEnoughSCException(int required, int current) {
        super("SC 能量不足！需要 " + required + "，目前只有 " + current);
    }
}
