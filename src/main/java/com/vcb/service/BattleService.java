package com.vcb.service;

import com.vcb.dao.*;
import com.vcb.model.*;
import com.vcb.model.enums.*;
import java.util.*;

/**
 * 對戰服務 — 控制完整對戰流程（初始化、回合推進、勝負判定、存檔）
 */
public class BattleService {
    private final GameEngine engine = new GameEngine();
    private final AIOpponent ai = new AIOpponent(engine);
    private final MatchDAO matchDAO = new MatchDAO();
    private final CardDAO cardDAO = new CardDAO();

    private Player human;
    private Player computer;
    private int turnCount;
    private boolean gameOver;

    /** 初始化對戰（設定雙方玩家、牌組、起始手牌） */
    public void initBattle(Player humanPlayer, Deck humanDeck) {
        this.human = humanPlayer;
        // 重置 HP/魔力/手牌/場地（資料庫載入的玩家只有帳號資料，每場對戰都要重新初始化）
        this.human.initForBattle();
        this.human.setDeck(humanDeck);
        this.human.getDeck().shuffle();

        // AI 隨機選一個陣營
        Faction[] factions = {Faction.DEEP_SPACE, Faction.RESCUTE, Faction.CELESTIAL,
                               Faction.TIMAEUS, Faction.PARADOX};
        Faction aiFaction = factions[new Random().nextInt(factions.length)];
        this.computer = new Player("AI-" + aiFaction.getDisplayName(), aiFaction);

        Deck aiDeck = buildRandomDeck(aiFaction);
        this.computer.setDeck(aiDeck);
        this.computer.getDeck().shuffle();

        // 雙方各抽 3 張起始手牌
        human.drawInitialHand(3);
        computer.drawInitialHand(3);

        this.turnCount = 0;
        this.gameOver = false;
    }

    /** 為 AI 從指定陣營 + 中立牌池隨機組一副 20 張的牌組 */
    private Deck buildRandomDeck(Faction faction) {
        Deck deck = new Deck("AI-" + faction.getDisplayName(), faction.name());
        List<Card> pool = cardDAO.findByFaction(faction.name());
        pool.addAll(cardDAO.findByFaction("NEUTRAL"));

        Random rand = new Random();
        int count = 0;
        // 最多放 20 張，每張最多放 2 次（簡易限制）
        while (count < 20 && !pool.isEmpty()) {
            Card card = pool.get(rand.nextInt(pool.size()));
            deck.addCard(card);
            count++;
            if (count % 2 == 0) {
                pool.remove(card);  // 每隔一張從池中移除，避免某張牌超過 2 份
            }
        }
        return deck;
    }

    /** 玩家回合開始：計數器 +1、魔力回滿、抽牌 */
    public String startHumanTurn() {
        turnCount++;
        human.startTurn();
        Card drawn = human.drawCard();
        if (drawn == null) {
            // 牌組已空，疲勞傷害遞增
            return "⚠️ 牌組已空！疲勞傷害 -" + human.getFatigueDamage() + " HP";
        }
        return "🃏 抽到了：" + drawn.handDisplay();
    }

    /** 執行 AI 整個回合（出牌 → 攻擊 → 回合結束效果） */
    public List<String> executeAITurn() {
        computer.startTurn();
        computer.drawCard();
        List<String> actions = ai.takeTurn(computer, human);
        List<String> endMsgs = engine.onTurnEnd(computer, human);
        actions.addAll(endMsgs);
        checkGameOver();
        return actions;
    }

    /** 玩家回合結束：觸發回合結束效果並檢查勝負 */
    public List<String> endHumanTurn() {
        List<String> msgs = engine.onTurnEnd(human, computer);
        checkGameOver();
        return msgs;
    }

    /** 任一方 HP 歸零即結束對戰 */
    private void checkGameOver() {
        if (!human.isAlive() || !computer.isAlive()) {
            gameOver = true;
        }
    }

    /** 將對戰結果寫入資料庫（訪客帳號 id<=0 時跳過） */
    public void saveResult() {
        if (human.getId() <= 0) return;
        String result = human.isAlive() ? "WIN" : "LOSE";
        matchDAO.save(human.getId(), computer.getUsername(),
                human.getFaction() != null ? human.getFaction().name() : "NEUTRAL",
                computer.getFaction().name(), result, turnCount, human.getHp());

        PlayerDAO playerDAO = new PlayerDAO();
        if ("WIN".equals(result)) {
            playerDAO.addWin(human.getId());
        } else {
            playerDAO.addLoss(human.getId());
        }
    }

    // === Getters ===
    public Player getHuman()    { return human; }
    public Player getComputer() { return computer; }
    public GameEngine getEngine() { return engine; }
    public int getTurnCount()   { return turnCount; }
    public boolean isGameOver() { return gameOver; }
}
