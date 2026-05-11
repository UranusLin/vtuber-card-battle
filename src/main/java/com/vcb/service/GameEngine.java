package com.vcb.service;

import com.vcb.model.*;
import com.vcb.model.enums.*;
import java.util.*;

/**
 * 遊戲核心引擎 — 處理所有卡牌效果和戰鬥邏輯
 */
public class GameEngine {
    private final Random random = new Random();

    /** 召喚 VTuber 到場上 */
    public String summonVTuber(Player player, int handIndex) {
        Card card = player.getHand().get(handIndex);
        if (!(card instanceof VTuberCard vtuber)) return "❌ 這不是 VTuber 卡！";
        if (!player.canAfford(card.getManaCost())) return "❌ SC 能量不足！需要 " + card.getManaCost();
        if (player.isFieldFull()) return "❌ 場上已有 4 位 VTuber！";

        player.spendMana(card.getManaCost());
        player.getHand().remove(handIndex);  // 從手牌移除
        player.getField().add(vtuber);        // 放上場

        StringBuilder sb = new StringBuilder();
        sb.append("✨ 召喚了 ").append(vtuber.getName()).append("！");

        // 戰吼能力：進場時觸發（目前僅印出提示，效果在 EffectType 中定義）
        if (vtuber.getAbility() == Ability.BATTLE_CRY && vtuber.getEffectType() != null) {
            sb.append("\n").append("  🎤 戰吼觸發！");
        }
        return sb.toString();
    }

    /** 使用才藝卡 */
    public String playTalent(Player caster, Player opponent, int handIndex, int targetIndex) {
        Card card = caster.getHand().get(handIndex);
        if (!(card instanceof TalentCard talent)) return "❌ 這不是才藝卡！";
        if (!caster.canAfford(card.getManaCost())) return "❌ SC 能量不足！需要 " + card.getManaCost();

        caster.spendMana(card.getManaCost());
        caster.getHand().remove(handIndex);  // 才藝卡用完即棄

        return executeTalentEffect(talent, caster, opponent, targetIndex);
    }

    /** 執行才藝卡效果（依 effectType 分派） */
    private String executeTalentEffect(TalentCard talent, Player caster, Player opponent, int targetIdx) {
        String type = talent.getEffectType();
        int value = talent.getEffectValue();
        if (type == null) return "✨ 使用了 " + talent.getName();

        return switch (type) {
            case "HEAL" -> {
                caster.heal(value);
                yield "💚 " + talent.getName() + "：恢復了 " + value + " HP！";
            }
            case "DAMAGE" -> {
                String target = talent.getTargetType();
                if ("ALL_ENEMIES".equals(target)) {
                    // 對全體敵方隨從造成傷害
                    for (VTuberCard v : opponent.getField()) v.takeDamage(value);
                    yield "💥 " + talent.getName() + "：對全體敵方造成 " + value + " 點傷害！";
                } else if ("ENEMY_HERO".equals(target)) {
                    // 直接打臉
                    opponent.takeDamage(value);
                    yield "💥 " + talent.getName() + "：對敵方英雄造成 " + value + " 點傷害！";
                } else {
                    // 指定單一目標
                    if (targetIdx >= 0 && targetIdx < opponent.getField().size()) {
                        opponent.getField().get(targetIdx).takeDamage(value);
                        yield "💥 " + talent.getName() + "：對 " + opponent.getField().get(targetIdx).getName() + " 造成 " + value + " 傷害！";
                    }
                    yield "❌ 無效目標";
                }
            }
            case "DRAW" -> {
                for (int i = 0; i < value; i++) caster.drawCard();
                yield "🃏 " + talent.getName() + "：抽了 " + value + " 張牌！";
            }
            case "FREEZE" -> {
                String target = talent.getTargetType();
                if ("ALL_ENEMIES".equals(target)) {
                    for (VTuberCard v : opponent.getField()) v.freeze();
                    yield "❄️ " + talent.getName() + "：凍結全體敵方！";
                } else if (targetIdx >= 0 && targetIdx < opponent.getField().size()) {
                    opponent.getField().get(targetIdx).freeze();
                    yield "❄️ " + talent.getName() + "：凍結了 " + opponent.getField().get(targetIdx).getName() + "！";
                }
                yield "❌ 無效目標";
            }
            case "BUFF_ATK", "BUFF" -> {
                String target = talent.getTargetType();
                if ("ALL_ALLIES".equals(target)) {
                    for (VTuberCard v : caster.getField()) v.buffAttack(value);
                    yield "⬆️ " + talent.getName() + "：全體友方 ATK +" + value + "！";
                } else if (targetIdx >= 0 && targetIdx < caster.getField().size()) {
                    caster.getField().get(targetIdx).buffAttack(value);
                    yield "⬆️ " + talent.getName() + "：" + caster.getField().get(targetIdx).getName() + " ATK +" + value + "！";
                }
                yield "❌ 無效目標";
            }
            case "BUFF_HP" -> {
                if (targetIdx >= 0 && targetIdx < caster.getField().size()) {
                    caster.getField().get(targetIdx).buffHealth(value);
                    yield "⬆️ " + talent.getName() + "：" + caster.getField().get(targetIdx).getName() + " HP +" + value + "！";
                }
                yield "❌ 無效目標";
            }
            case "HEAL_FULL" -> {
                if (targetIdx >= 0 && targetIdx < caster.getField().size()) {
                    VTuberCard v = caster.getField().get(targetIdx);
                    v.heal(v.getMaxHealth());  // 傳入最大值，heal() 內部會自動 clamp 不超上限
                    yield "💚 " + talent.getName() + "：" + v.getName() + " 完全恢復！";
                }
                yield "❌ 無效目標";
            }
            case "HEAL_AND_DRAW" -> {
                caster.heal(value);
                caster.drawCard();
                yield "💚🃏 " + talent.getName() + "：恢復 " + value + " HP 並抽 1 張牌！";
            }
            case "SWAP_STATS" -> {
                if (targetIdx >= 0 && targetIdx < opponent.getField().size()) {
                    VTuberCard v = opponent.getField().get(targetIdx);
                    int tmp = v.getAttack();
                    v.setAttack(v.getHealth());
                    v.setHealth(tmp);
                    yield "🔮 " + talent.getName() + "：" + v.getName() + " 的 ATK/HP 互換！";
                }
                yield "❌ 無效目標";
            }
            case "DESTROY_RANDOM" -> {
                // 合併雙方場上隨從後隨機消滅一隻
                List<VTuberCard> all = new ArrayList<>();
                all.addAll(caster.getField());
                all.addAll(opponent.getField());
                if (!all.isEmpty()) {
                    VTuberCard victim = all.get(random.nextInt(all.size()));
                    victim.takeDamage(999);  // 999 傷害確保一擊斃命
                    yield "💀 " + talent.getName() + "：" + victim.getName() + " 被消滅了！";
                }
                yield "💀 場上沒有目標";
            }
            case "DAMAGE_RANDOM" -> {
                // 造成 1~value 點隨機傷害
                int dmg = random.nextInt(value) + 1;
                if (!opponent.getField().isEmpty()) {
                    VTuberCard v = opponent.getField().get(random.nextInt(opponent.getField().size()));
                    v.takeDamage(dmg);
                    yield "🎲 " + talent.getName() + "：對 " + v.getName() + " 造成 " + dmg + " 點隨機傷害！";
                }
                yield "❌ 場上沒有敵方隨從";
            }
            case "GRANT_TAUNT" -> {
                for (VTuberCard v : caster.getField()) v.grantAbility(Ability.TAUNT);
                yield "🛡️ " + talent.getName() + "：全體友方獲得嘲諷！";
            }
            default -> "✨ 使用了 " + talent.getName();
        };
    }

    /** 裝備應援卡到場上指定的 VTuber */
    public String equipSupport(Player player, int handIndex, int fieldIndex) {
        Card card = player.getHand().get(handIndex);
        if (!(card instanceof SupportCard support)) return "❌ 這不是應援卡！";
        if (!player.canAfford(card.getManaCost())) return "❌ SC 能量不足！";
        if (fieldIndex < 0 || fieldIndex >= player.getField().size()) return "❌ 無效目標！";

        player.spendMana(card.getManaCost());
        player.getHand().remove(handIndex);
        VTuberCard target = player.getField().get(fieldIndex);
        support.equip(target);  // 將加成直接寫到 VTuberCard 上

        return "🎁 為 " + target.getName() + " 裝備了 " + support.getName() + "！";
    }

    /** 攻擊邏輯（含嘲諷規則與陣營剋制） */
    public String attack(Player attacker, Player defender, int attackerIdx, int defenderIdx) {
        if (attackerIdx < 0 || attackerIdx >= attacker.getField().size()) return "❌ 無效攻擊者！";
        VTuberCard atk = attacker.getField().get(attackerIdx);

        if (!atk.canAttack()) return "❌ " + atk.getName() + " 本回合無法攻擊！";

        // 攻擊敵方英雄（defenderIdx == -1）
        if (defenderIdx == -1) {
            if (defender.hasTauntOnField()) {
                return "❌ 必須先攻擊有嘲諷的隨從！";
            }
            int damage = calculateDamage(atk, null, attacker, defender);
            defender.takeDamage(damage);
            atk.setCanAttack(false);  // 每回合只能攻擊一次
            return "⚔️ " + atk.getName() + " 對敵方英雄造成 " + damage + " 點傷害！";
        }

        // 攻擊敵方隨從
        if (defenderIdx < 0 || defenderIdx >= defender.getField().size()) return "❌ 無效目標！";
        VTuberCard def = defender.getField().get(defenderIdx);

        // 嘲諷規則：場上有嘲諷隨從時必須優先攻擊
        if (defender.hasTauntOnField() && !def.hasTaunt()) {
            return "❌ 必須先攻擊有嘲諷的 " + defender.getTauntMinions().get(0).getName() + "！";
        }

        // 雙方互相傷害（攻擊者承受防禦方的 ATK）
        int atkDamage = calculateDamage(atk, def, attacker, defender);
        def.takeDamage(atkDamage);
        atk.takeDamage(def.getAttack());
        atk.setCanAttack(false);

        StringBuilder sb = new StringBuilder();
        sb.append("⚔️ ").append(atk.getName()).append(" (").append(atkDamage).append(" 傷害) vs ")
          .append(def.getName()).append(" (").append(def.getAttack()).append(" 反擊)");
        if (def.isDead()) sb.append("\n  💀 ").append(def.getName()).append(" 陣亡！");
        if (atk.isDead()) sb.append("\n  💀 ").append(atk.getName()).append(" 陣亡！");
        return sb.toString();
    }

    /**
     * 計算實際傷害（含陣營剋制加成）
     * 相剋關係：極深空→瀕臨絕種→瑟拉斯蒂歐→極深空（×1.5），反向×0.75
     */
    private int calculateDamage(VTuberCard attacker, VTuberCard defender,
                                 Player atkPlayer, Player defPlayer) {
        int baseDamage = attacker.getAttack();
        if (defender == null) {
            // 攻擊英雄時，以玩家設定的陣營計算剋制
            double mult = attacker.getFaction().getAdvantageMultiplier(
                    defPlayer.getFaction() != null ? defPlayer.getFaction() : Faction.NEUTRAL);
            return (int) Math.round(baseDamage * mult);
        }
        double mult = attacker.getFaction().getAdvantageMultiplier(defender.getFaction());
        return (int) Math.round(baseDamage * mult);
    }

    /** 回合結束時觸發持續效果，並清理雙方死亡隨從 */
    public List<String> onTurnEnd(Player current, Player opponent) {
        List<String> messages = new ArrayList<>();

        // 觸發場上 VTuber 的回合結束被動效果
        for (VTuberCard v : current.getField()) {
            if (v.getEffectType() == null) continue;
            switch (v.getEffectType()) {
                case "HEAL_ALL_ALLY" -> {
                    int heal = v.getEffectValue();
                    for (VTuberCard ally : current.getField()) ally.heal(heal);
                    messages.add("💚 " + v.getName() + " 的療癒歌聲恢復了全體 " + heal + " HP！");
                }
                case "BUFF_CONDITIONAL" -> {
                    // 場上有同陣營夥伴時才觸發強化
                    boolean hasAlly = current.getField().stream()
                            .anyMatch(a -> a != v && a.getFaction() == Faction.DEEP_SPACE);
                    if (hasAlly) {
                        messages.add("⚡ " + v.getName() + " 感受到夥伴的力量！ATK +2");
                    }
                }
                case "PHASE_SHIFT" -> {
                    messages.add("🌙 " + v.getName() + " 月相變化！");
                }
            }
        }

        // 清除雙方場上已死亡的隨從（分開呼叫，避免操作不可變列表）
        current.removeDeadMinions();
        opponent.removeDeadMinions();

        return messages;
    }
}
