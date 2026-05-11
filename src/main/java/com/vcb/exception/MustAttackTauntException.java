package com.vcb.exception;

/**
 * 嘲諷攻擊限制例外 — 場上有嘲諷隨從時必須優先攻擊嘲諷目標
 */
public class MustAttackTauntException extends Exception {
    public MustAttackTauntException(String tauntName) {
        super("必須先攻擊有嘲諷的 " + tauntName + "！");
    }
}
