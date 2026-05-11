package com.vcb.view;

import com.vcb.model.*;
import com.vcb.model.enums.*;
import com.vcb.service.*;
import java.util.*;

/**
 * 對戰畫面 — CLI 渲染戰場與處理玩家輸入
 */
public class BattleView {
    private final Scanner scanner;
    private final BattleService battleService;

    public BattleView(Scanner scanner) {
        this.scanner = scanner;
        this.battleService = new BattleService();
    }

    /** 開始對戰主迴圈 */
    public void startBattle(Player player, Deck deck) {
        player.setFaction(Faction.fromString(deck.getFaction()));
        battleService.initBattle(player, deck);
        System.out.println("\n⚔️ 對手是：" + battleService.getComputer().getUsername() + "！");
        System.out.println("═".repeat(50));

        while (!battleService.isGameOver()) {
            // === 玩家回合 ===
            System.out.println("\n╔══════════ 第 " + (battleService.getTurnCount() + 1) + " 回合 ══════════╗");
            String drawMsg = battleService.startHumanTurn();
            System.out.println(drawMsg);
            renderField();
            playerTurn();

            if (battleService.isGameOver()) break;

            // 回合結束效果（如被動回血、清除死亡隨從）
            List<String> endMsgs = battleService.endHumanTurn();
            endMsgs.forEach(System.out::println);
            if (battleService.isGameOver()) break;

            // === AI 回合 ===
            System.out.println("\n🤖 ── 對手回合 ──");
            List<String> aiActions = battleService.executeAITurn();
            aiActions.forEach(System.out::println);
            if (battleService.isGameOver()) break;
        }

        showResult();
    }

    /** 玩家行動迴圈（出牌/攻擊/結束回合/查看狀態） */
    private void playerTurn() {
        while (true) {
            System.out.println("\n[P] 出牌  [A] 攻擊  [E] 結束回合  [S] 查看狀態");
            System.out.print("> ");
            String input = scanner.nextLine().trim().toUpperCase();

            switch (input) {
                case "P" -> handlePlayCard();
                case "A" -> handleAttack();
                case "E" -> { return; }  // 結束回合
                case "S" -> renderField();
                default  -> System.out.println("❌ 無效指令");
            }

            if (battleService.isGameOver()) return;
        }
    }

    /** 處理出牌：依卡牌類型分派到對應的引擎方法 */
    private void handlePlayCard() {
        Player human = battleService.getHuman();
        if (human.getHand().isEmpty()) { System.out.println("❌ 手牌為空！"); return; }

        System.out.println("\n── 你的手牌 ──");
        for (int i = 0; i < human.getHand().size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, human.getHand().get(i).handDisplay());
        }
        System.out.print("選擇卡牌編號（0=取消）: ");
        int choice = readInt() - 1;
        if (choice < 0 || choice >= human.getHand().size()) return;

        Card card = human.getHand().get(choice);
        GameEngine engine = battleService.getEngine();
        String msg;

        if (card instanceof VTuberCard) {
            msg = engine.summonVTuber(human, choice);
        } else if (card instanceof TalentCard talent) {
            int target = -1;
            if (needsTarget(talent.getTargetType())) {
                target = askTarget(talent.getTargetType(), human, battleService.getComputer());
                if (target == -99) return;  // 玩家取消選擇
            }
            msg = engine.playTalent(human, battleService.getComputer(), choice, target);
        } else if (card instanceof SupportCard) {
            if (human.getField().isEmpty()) { System.out.println("❌ 場上沒有 VTuber 可裝備！"); return; }
            System.out.println("選擇裝備目標：");
            for (int i = 0; i < human.getField().size(); i++) {
                System.out.printf("  [%d] %s%n", i + 1, human.getField().get(i).display());
            }
            int target = readInt() - 1;
            if (target < 0) return;
            msg = engine.equipSupport(human, choice, target);
        } else { return; }

        System.out.println(msg);
        // 出牌後立即清理可能因效果死亡的隨從
        human.removeDeadMinions();
        battleService.getComputer().removeDeadMinions();
    }

    /** 處理攻擊：選擇攻擊者 → 選擇目標 → 執行 */
    private void handleAttack() {
        Player human = battleService.getHuman();
        Player comp  = battleService.getComputer();
        if (human.getField().isEmpty()) { System.out.println("❌ 場上沒有 VTuber！"); return; }

        System.out.println("選擇攻擊者：");
        for (int i = 0; i < human.getField().size(); i++) {
            VTuberCard v = human.getField().get(i);
            String status = v.canAttack() ? "✅" : "💤";  // ✅ 可攻擊 / 💤 本回合已攻擊或被凍結
            System.out.printf("  [%d] %s %s%n", i + 1, status, v.display());
        }
        System.out.print("選擇攻擊者（0=取消）: ");
        int atkIdx = readInt() - 1;
        if (atkIdx < 0 || atkIdx >= human.getField().size()) return;

        System.out.println("選擇目標：");
        System.out.println("  [0] 🏠 敵方英雄（HP: " + comp.getHp() + "）");
        for (int i = 0; i < comp.getField().size(); i++) {
            VTuberCard v = comp.getField().get(i);
            String taunt = v.hasTaunt() ? "🛡️" : "";
            System.out.printf("  [%d] %s%s%n", i + 1, v.display(), taunt);
        }
        System.out.print("選擇目標: ");
        int defIdx = readInt() - 1;  // -1 表示選了英雄（輸入 0 → -1）

        String msg = battleService.getEngine().attack(human, comp, atkIdx, defIdx);
        System.out.println(msg);

        // 攻擊後清理死亡隨從
        human.removeDeadMinions();
        comp.removeDeadMinions();
    }

    /** 渲染戰場（AI 在上方，玩家在下方） */
    private void renderField() {
        Player human = battleService.getHuman();
        Player comp  = battleService.getComputer();

        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.printf("║  📺 %s  👥 HP:%d/%d  💰 SC:%d/%d  🃏 %d張%n",
                comp.getUsername(), comp.getHp(), comp.getMaxHp(),
                comp.getMana(), comp.getMaxMana(),
                comp.getDeck() != null ? comp.getDeck().size() : 0);
        System.out.print("║  場上: ");
        if (comp.getField().isEmpty()) System.out.println("（空）");
        else {
            comp.getField().forEach(v -> System.out.print(v.display() + " "));
            System.out.println();
        }
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.print("║  場上: ");
        if (human.getField().isEmpty()) System.out.println("（空）");
        else {
            human.getField().forEach(v -> System.out.print(v.display() + " "));
            System.out.println();
        }
        System.out.printf("║  📺 %s  👥 HP:%d/%d  💰 SC:%d/%d  🃏 %d張%n",
                human.getUsername(), human.getHp(), human.getMaxHp(),
                human.getMana(), human.getMaxMana(),
                human.getDeck() != null ? human.getDeck().size() : 0);
        System.out.println("╚════════════════════════════════════════════╝");

        System.out.println("── 手牌（" + human.getHand().size() + " 張）──");
        for (int i = 0; i < human.getHand().size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, human.getHand().get(i).handDisplay());
        }
    }

    /** 顯示對戰結果並存檔 */
    private void showResult() {
        Player human = battleService.getHuman();
        System.out.println("\n" + "═".repeat(50));
        if (human.isAlive()) {
            System.out.println("🎉🎉🎉 你贏了！恭喜！🎉🎉🎉");
        } else {
            System.out.println("💀 你輸了... 再接再厲！");
        }
        System.out.println("回合數: " + battleService.getTurnCount());
        System.out.println("剩餘 HP: " + human.getHp());
        battleService.saveResult();  // 寫入資料庫（訪客帳號會自動跳過）
        System.out.println("═".repeat(50));
    }

    /** 才藝卡是否需要玩家手動選擇目標 */
    private boolean needsTarget(String targetType) {
        return targetType != null &&
               (targetType.contains("SINGLE") || targetType.equals("HERO"));
    }

    /** 詢問玩家選擇目標，回傳索引（-99 表示取消） */
    private int askTarget(String targetType, Player human, Player comp) {
        if (targetType.contains("ENEMY")) {
            if (comp.getField().isEmpty()) { System.out.println("❌ 敵方場上沒有目標"); return -99; }
            System.out.println("選擇敵方目標：");
            for (int i = 0; i < comp.getField().size(); i++) {
                System.out.printf("  [%d] %s%n", i + 1, comp.getField().get(i).display());
            }
            return readInt() - 1;
        } else {
            if (human.getField().isEmpty()) { System.out.println("❌ 場上沒有友方目標"); return -99; }
            System.out.println("選擇友方目標：");
            for (int i = 0; i < human.getField().size(); i++) {
                System.out.printf("  [%d] %s%n", i + 1, human.getField().get(i).display());
            }
            return readInt() - 1;
        }
    }

    /** 安全讀取整數（輸入非數字時回傳 -1 而非拋出例外） */
    private int readInt() {
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}
