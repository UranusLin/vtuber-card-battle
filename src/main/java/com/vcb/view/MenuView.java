package com.vcb.view;

import com.vcb.dao.*;
import com.vcb.model.*;
import com.vcb.model.enums.*;
import com.vcb.service.*;
import java.util.*;

/**
 * 主選單 — 遊戲入口（登入/註冊/開始對戰/圖鑑/戰績）
 */
public class MenuView {
    private final Scanner scanner;
    private final PlayerDAO playerDAO  = new PlayerDAO();
    private final MatchDAO matchDAO    = new MatchDAO();
    private final DeckService deckService = new DeckService();
    private final DeckDAO deckDAO      = new DeckDAO();
    private Player currentPlayer;

    public MenuView(Scanner scanner) {
        this.scanner = scanner;
    }

    /** 啟動遊戲，顯示登入畫面直到登入成功 */
    public void start() {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║       🃏 VTuber Card Battle 🃏            ║");
        System.out.println("║           配信宇宙錦標賽                  ║");
        System.out.println("╚════════════════════════════════════════════╝");

        while (currentPlayer == null) {
            System.out.println("\n[1] 登入  [2] 註冊  [0] 離開");
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> login();
                case "2" -> register();
                case "0" -> { System.out.println("👋 再見！"); return; }
            }
        }
        mainMenu();
    }

    /** 登入：帳號 + 密碼（密碼以 hashCode 比對） */
    private void login() {
        System.out.print("帳號: ");
        String username = scanner.nextLine().trim();
        System.out.print("密碼: ");
        String password = scanner.nextLine().trim();
        // 使用 hashCode 做簡單雜湊（僅供教學示範）
        currentPlayer = playerDAO.login(username, String.valueOf(password.hashCode()));
        if (currentPlayer == null) {
            System.out.println("❌ 帳號或密碼錯誤！");
        } else {
            System.out.println("✅ 歡迎回來，" + currentPlayer.getUsername() + "！");
        }
    }

    /** 註冊新帳號（帳號長度 3-30 字元，由資料庫 CHECK 約束驗證） */
    private void register() {
        System.out.print("帳號（3-30字元）: ");
        String username = scanner.nextLine().trim();
        System.out.print("密碼: ");
        String password = scanner.nextLine().trim();
        int id = playerDAO.register(username, String.valueOf(password.hashCode()));
        if (id > 0) {
            System.out.println("✅ 註冊成功！請重新登入。");
        }
    }

    /** 主選單迴圈 */
    private void mainMenu() {
        while (true) {
            System.out.println("\n═══ 主選單 ═══");
            System.out.println("[1] ⚔️ 開始對戰");
            System.out.println("[2] 📖 卡牌圖鑑");
            System.out.println("[3] 📊 我的戰績");
            System.out.println("[0] 🚪 登出");
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> startBattle();
                case "2" -> showCardCollection();
                case "3" -> showStats();
                case "0" -> {
                    System.out.println("👋 登出成功！");
                    currentPlayer = null;
                    return;
                }
            }
        }
    }

    /** 快速對戰：選擇陣營後自動組一副隨機牌組 */
    private void startBattle() {
        System.out.println("\n選擇你的陣營：");
        Faction[] factions = {Faction.DEEP_SPACE, Faction.RESCUTE, Faction.CELESTIAL,
                               Faction.TIMAEUS, Faction.PARADOX};
        for (int i = 0; i < factions.length; i++) {
            System.out.printf("  [%d] %s %s%n", i + 1, factions[i].getIcon(), factions[i].getDisplayName());
        }
        System.out.print("> ");
        int choice = readInt() - 1;
        if (choice < 0 || choice >= factions.length) return;

        Faction chosen = factions[choice];
        currentPlayer.setFaction(chosen);

        // 從選定陣營 + 中立牌池隨機挑 20 張組牌
        List<Card> pool = deckService.getCardsByFaction(chosen.name());
        pool.addAll(deckService.getCardsByFaction("NEUTRAL"));

        Deck deck = new Deck("快速對戰", chosen.name());
        Random rand = new Random();
        int count = 0;
        List<Card> available = new ArrayList<>(pool);  // 複製一份避免修改原始 pool
        while (count < 20 && !available.isEmpty()) {
            Card card = available.get(rand.nextInt(available.size()));
            deck.addCard(card);
            count++;
            if (count % 2 == 0) available.remove(card);  // 確保每張最多 2 份
        }

        BattleView battleView = new BattleView(scanner);
        battleView.startBattle(currentPlayer, deck);
    }

    /** 顯示卡牌圖鑑（可依陣營篩選） */
    private void showCardCollection() {
        System.out.println("\n選擇陣營（0=全部）：");
        System.out.println("  [0] 全部  [1] 極深空  [2] 瀕臨絕種團  [3] 瑟拉斯蒂歐  [4] 諦覓司  [5] 五律悖反  [6] 中立");
        System.out.print("> ");
        int choice = readInt();

        List<Card> cards;
        if (choice == 0) {
            cards = deckService.getAllCards();
        } else {
            String[] codes = {"", "DEEP_SPACE", "RESCUTE", "CELESTIAL", "TIMAEUS", "PARADOX", "NEUTRAL"};
            if (choice < 1 || choice > 6) return;
            cards = deckService.getCardsByFaction(codes[choice]);
        }

        // 表格標題
        System.out.printf("%n%-4s %-12s %-10s %-8s %-4s %-4s %-4s %-6s %s%n",
                "ID", "名稱", "陣營", "類型", "費用", "ATK", "HP", "稀有度", "能力");
        System.out.println("─".repeat(80));
        for (Card c : cards) {
            int atk = 0, hp = 0;
            if (c instanceof VTuberCard v)  { atk = v.getAttack();      hp = v.getHealth(); }
            else if (c instanceof SupportCard s) { atk = s.getAttackBonus(); hp = s.getHealthBonus(); }
            System.out.printf("%-4d %-12s %-10s %-8s %-4d %-4d %-4d %-6s %s%n",
                    c.getId(), c.getName(), c.getFaction().getDisplayName(),
                    c.getCardType().getDisplayName(), c.getManaCost(),
                    atk, hp, c.getRarity().getDisplay(),
                    c.getEffectDesc() != null ? c.getEffectDesc() : "");
        }
        System.out.println("共 " + cards.size() + " 張卡牌");
    }

    /** 顯示最近 10 場對戰記錄 */
    private void showStats() {
        System.out.println("\n📊 " + currentPlayer.getUsername() + " 的戰績");
        List<Map<String, Object>> records = matchDAO.findRecent(currentPlayer.getId(), 10);
        if (records.isEmpty()) {
            System.out.println("  還沒有對戰記錄，快去打一場吧！");
            return;
        }
        System.out.printf("%-12s %-6s %-10s %-10s %-5s %-5s%n",
                "對手", "結果", "我方陣營", "對方陣營", "回合", "剩餘HP");
        System.out.println("─".repeat(55));
        for (Map<String, Object> r : records) {
            String result = "WIN".equals(r.get("result")) ? "🏆 勝" : "💀 敗";
            System.out.printf("%-12s %-6s %-10s %-10s %-5s %-5s%n",
                    r.get("opponent"), result, r.get("faction"), r.get("oppFaction"),
                    r.get("turns"), r.get("hpLeft"));
        }
    }

    /** 安全讀取整數（NumberFormatException 時回傳 -1） */
    private int readInt() {
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}
