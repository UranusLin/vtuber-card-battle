package com.vcb.service;

import com.vcb.dao.*;
import com.vcb.model.*;
import java.util.*;

/**
 * 牌組服務 — 牌組驗證與建立邏輯
 * 作為 DAO 的上層，包含遊戲規則驗證（張數、稀有度限制等）
 */
public class DeckService {
    private final CardDAO cardDAO = new CardDAO();
    private final DeckDAO deckDAO = new DeckDAO();

    public static final int DECK_SIZE      = 20;  // 每副牌固定 20 張
    public static final int MAX_SAME_CARD  = 2;   // 同一張牌最多 2 份
    public static final int MAX_LEGENDARY  = 1;   // 傳說卡全副牌最多 1 張

    /** 從資料庫載入玩家的牌組，並填入卡牌物件 */
    public Deck loadDeck(int deckId) {
        Map<Integer, Integer> cardIds = deckDAO.getDeckCardIds(deckId);

        Deck deck = new Deck("loaded", "NEUTRAL");
        for (Map.Entry<Integer, Integer> entry : cardIds.entrySet()) {
            Card card = cardDAO.findById(entry.getKey());
            if (card != null) {
                // entry.getValue() 是張數，例如同一張牌放 2 份就 addCard 兩次
                for (int i = 0; i < entry.getValue(); i++) {
                    deck.addCard(card);
                }
            }
        }
        return deck;
    }

    /**
     * 驗證牌組是否符合規則
     * 回傳錯誤訊息列表，空列表表示合法
     */
    public List<String> validateDeck(Map<Integer, Integer> cardQuantities) {
        List<String> errors = new ArrayList<>();
        int totalCards = cardQuantities.values().stream().mapToInt(i -> i).sum();

        if (totalCards != DECK_SIZE) {
            errors.add("牌組需要 " + DECK_SIZE + " 張卡，目前 " + totalCards + " 張");
        }

        int legendaryCount = 0;
        for (Map.Entry<Integer, Integer> entry : cardQuantities.entrySet()) {
            if (entry.getValue() > MAX_SAME_CARD) {
                errors.add("卡牌 #" + entry.getKey() + " 超過上限（最多 " + MAX_SAME_CARD + " 張）");
            }
            Card card = cardDAO.findById(entry.getKey());
            if (card != null && card.getRarity().getStars() == 4) {
                legendaryCount += entry.getValue();
            }
        }

        if (legendaryCount > MAX_LEGENDARY) {
            errors.add("傳說卡最多只能放 " + MAX_LEGENDARY + " 張");
        }
        return errors;
    }

    public List<Card> getAllCards()                    { return cardDAO.findAll(); }
    public List<Card> getCardsByFaction(String faction) { return cardDAO.findByFaction(faction); }
}
