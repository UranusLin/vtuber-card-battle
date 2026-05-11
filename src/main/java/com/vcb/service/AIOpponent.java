package com.vcb.service;

import com.vcb.model.*;
import com.vcb.model.enums.*;
import java.util.*;

/**
 * AI 對手 — 簡易決策引擎
 * 策略：Phase 1 先盡量花光魔力出牌，Phase 2 再讓場上能攻擊的 VTuber 全部出擊
 */
public class AIOpponent {
    private final Random random = new Random();
    private final GameEngine engine;

    public AIOpponent(GameEngine engine) {
        this.engine = engine;
    }

    /** AI 執行整個回合，回傳所有行動訊息 */
    public List<String> takeTurn(Player ai, Player human) {
        List<String> actions = new ArrayList<>();

        // Phase 1: 出牌（每次出一張後重新掃描手牌，直到無牌可出）
        boolean played = true;
        while (played) {
            played = false;
            for (int i = 0; i < ai.getHand().size(); i++) {
                Card card = ai.getHand().get(i);
                if (!ai.canAfford(card.getManaCost())) continue;

                if (card instanceof VTuberCard && !ai.isFieldFull()) {
                    String msg = engine.summonVTuber(ai, i);
                    actions.add("🤖 " + msg);
                    played = true;
                    break;  // 出牌後手牌索引變動，重新掃描
                } else if (card instanceof TalentCard talent) {
                    int target = pickTalentTarget(talent, ai, human);
                    String msg = engine.playTalent(ai, human, i, target);
                    actions.add("🤖 " + msg);
                    played = true;
                    break;
                } else if (card instanceof SupportCard && !ai.getField().isEmpty()) {
                    int target = random.nextInt(ai.getField().size());
                    String msg = engine.equipSupport(ai, i, target);
                    actions.add("🤖 " + msg);
                    played = true;
                    break;
                }
            }
        }

        // Phase 2: 攻擊（所有可攻擊的隨從都出擊）
        for (int i = 0; i < ai.getField().size(); i++) {
            VTuberCard v = ai.getField().get(i);
            if (!v.canAttack()) continue;

            int targetIdx = pickAttackTarget(ai, human);
            String msg = engine.attack(ai, human, i, targetIdx);
            actions.add("🤖 " + msg);

            // 攻擊後立即清理死亡隨從，避免索引錯位
            ai.removeDeadMinions();
            human.removeDeadMinions();
            if (i >= ai.getField().size()) break;  // 自己也陣亡時提前結束
        }

        return actions;
    }

    /** 根據才藝卡的目標類型選擇合適的目標索引 */
    private int pickTalentTarget(TalentCard talent, Player ai, Player human) {
        String target = talent.getTargetType();
        if (target == null) return -1;
        return switch (target) {
            case "SINGLE_ENEMY" -> human.getField().isEmpty() ? -1 : pickWeakest(human.getField());
            case "SINGLE_ALLY"  -> ai.getField().isEmpty() ? -1 : random.nextInt(ai.getField().size());
            default -> -1;
        };
    }

    /**
     * 選擇攻擊目標
     * 規則：有嘲諷必打嘲諷；否則 50% 打英雄，50% 打最低血量的隨從
     */
    private int pickAttackTarget(Player ai, Player human) {
        if (human.getField().isEmpty()) return -1;  // 場上無隨從，直接打英雄

        if (human.hasTauntOnField()) {
            // 找到第一個有嘲諷的隨從
            for (int i = 0; i < human.getField().size(); i++) {
                if (human.getField().get(i).hasTaunt()) return i;
            }
        }

        // 沒有嘲諷時隨機決定打英雄或打隨從
        if (random.nextBoolean() && !human.hasTauntOnField()) return -1;

        return pickWeakest(human.getField());
    }

    /** 找場上血量最低的隨從（優先擊殺高威脅目標） */
    private int pickWeakest(List<VTuberCard> field) {
        int minHp = Integer.MAX_VALUE;
        int idx = 0;
        for (int i = 0; i < field.size(); i++) {
            if (field.get(i).getHealth() < minHp) {
                minHp = field.get(i).getHealth();
                idx = i;
            }
        }
        return idx;
    }
}
