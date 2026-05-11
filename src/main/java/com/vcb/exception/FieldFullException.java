package com.vcb.exception;

/**
 * 場地已滿例外 — 場上已有 4 位 VTuber 時拋出
 * 繼承 Exception（checked exception），呼叫端必須明確處理
 */
public class FieldFullException extends Exception {
    public FieldFullException() {
        super("場上已有 4 位 VTuber，無法再召喚！");
    }
}
