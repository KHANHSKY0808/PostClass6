package ca.mcgill.solitaire.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Biểu diễn một tập hợp các lá bài trên tay người chơi, 
 * hỗ trợ đánh giá và so sánh theo luật Poker.
 */
public class Hand implements Iterable<Card>, Comparable<Hand> {
    private static final int POKER_HAND_SIZE = 5;

    private final List<Card> aCards = new ArrayList<>();
    private final int aMaxCards;

    /**
     * Khởi tạo Hand với số lượng bài tối đa cho phép.
     * 
     * @param pMaxCards Số bài tối đa.
     * @pre pMaxCards > 0
     */
    public Hand(int pMaxCards) {
        assert pMaxCards > 0;
        aMaxCards = pMaxCards;
    }

    /**
     * Khởi tạo mặc định cho ván bài Poker 5 lá.
     */
    public Hand() {
        this(POKER_HAND_SIZE);
    }

    public void add(Card pCard) {
        assert pCard != null;
        assert !isFull();
        aCards.add(pCard);
    }

    public void remove(Card pCard) {
        assert pCard != null;
        aCards.remove(pCard);
    }

    public boolean contains(Card pCard) {
        assert pCard != null;
        return aCards.contains(pCard);
    }

    public boolean isEmpty() {
        return aCards.isEmpty();
    }

    public boolean isFull() {
        return aCards.size() >= aMaxCards;
    }

    public int size() {
        return aCards.size();
    }

    public void clear() {
        aCards.clear();
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(aCards);
    }

    @Override
    public Iterator<Card> iterator() {
        return aCards.iterator();
    }

    /**
     * Xác định loại tay bài Poker.
     * 
     * @return PokerHandType của bộ bài.
     * @throws IllegalStateException nếu bộ bài không có đúng 5 lá.
     */
    public PokerHandType getHandType() {
        if (aCards.size() != POKER_HAND_SIZE) {
            throw new IllegalStateException("Hand phai co chinh xac 5 la bai de danh gia Poker.");
        }

        Map<Rank, Integer> rankCounts = getRankFrequencies();
        List<Integer> frequencies = new ArrayList<>(rankCounts.values());
        frequencies.sort(Collections.reverseOrder());

        boolean flush = isFlush();
        boolean straight = isStraight(rankCounts);

        if (straight && flush) {
            return PokerHandType.STRAIGHT_FLUSH;
        }
        if (frequencies.get(0) == 4) {
            return PokerHandType.FOUR_OF_A_KIND;
        }
        if (frequencies.get(0) == 3 && frequencies.get(1) == 2) {
            return PokerHandType.FULL_HOUSE;
        }
        if (flush) {
            return PokerHandType.FLUSH;
        }
        if (straight) {
            return PokerHandType.STRAIGHT;
        }
        if (frequencies.get(0) == 3) {
            return PokerHandType.THREE_OF_A_KIND;
        }
        if (frequencies.get(0) == 2 && frequencies.get(1) == 2) {
            return PokerHandType.TWO_PAIR;
        }
        if (frequencies.get(0) == 2) {
            return PokerHandType.ONE_PAIR;
        }
        return PokerHandType.HIGH_CARD;
    }

    private Map<Rank, Integer> getRankFrequencies() {
        Map<Rank, Integer> counts = new HashMap<>();
        for (Card card : aCards) {
            counts.put(card.rank(), counts.getOrDefault(card.rank(), 0) + 1);
        }
        return counts;
    }

    /**
     * Kiểm tra xem 5 lá bài có tạo thành sảnh đồng chất (Straight Flush) hay không.
     * Thuật toán được thực hiện trên một bản sao của danh sách các lá bài đã được sắp xếp tăng dần theo rank.
     * Lưu ý: Ace chỉ được tính là lá bài nhỏ nhất (ordinal 0), do đó A-2-3-4-5 hợp lệ
     * nhưng 10-J-Q-K-A sẽ không được coi là Straight Flush (không hỗ trợ Ace-high).
     * 
     * @return true nếu bộ bài là Straight Flush (Ace-low), ngược lại false.
     */
    private boolean isStraightFlush() {
        assert aCards.size() == POKER_HAND_SIZE;
        List<Card> sortedCards = new ArrayList<>(aCards);
        sortedCards.sort(Comparator.comparing(Card::rank));
        Suit suit = sortedCards.get(0).suit();
        Rank rank = sortedCards.get(0).rank();
        Iterator<Card> iterator = sortedCards.iterator();
        iterator.next();
        while (iterator.hasNext()) {
            Card card = iterator.next();
            if (card.suit() != suit || card.rank().ordinal() - rank.ordinal() != 1) {
                return false;
            }
            rank = card.rank();
        }
        return true;
    }

    private boolean isFlush() {
        Suit firstSuit = aCards.get(0).suit();
        for (Card card : aCards) {
            if (card.suit() != firstSuit) {
                return false;
            }
        }
        return true;
    }

    private boolean isStraight(Map<Rank, Integer> pRankCounts) {
        if (pRankCounts.size() != POKER_HAND_SIZE) {
            return false;
        }

        // Trường hợp Ace-low straight: A-2-3-4-5
        if (isAceLowStraight(pRankCounts)) {
            return true;
        }

        // Dãy 5 lá liên tiếp thông thường (Ace tính là 14)
        List<Integer> values = new ArrayList<>();
        for (Rank rank : pRankCounts.keySet()) {
            values.add(getPokerRankValue(rank, false));
        }
        Collections.sort(values);

        return values.get(4) - values.get(0) == 4;
    }

    private boolean isAceLowStraight(Map<Rank, Integer> pRankCounts) {
        return pRankCounts.containsKey(Rank.ACE) &&
               pRankCounts.containsKey(Rank.TWO) &&
               pRankCounts.containsKey(Rank.THREE) &&
               pRankCounts.containsKey(Rank.FOUR) &&
               pRankCounts.containsKey(Rank.FIVE);
    }

    /**
     * Quy đổi Rank sang giá trị điểm Poker.
     * Mặc định Ace = 14 (cao nhất), trừ khi xét sảnh nhỏ Ace-low (Ace = 1).
     */
    private static int getPokerRankValue(Rank pRank, boolean pAceAsLow) {
        if (pRank == Rank.ACE) {
            return pAceAsLow ? 1 : 14;
        }
        // Trong enum Rank: ACE=0, TWO=1, THREE=2, ..., KING=12
        return pRank.ordinal() + 1;
    }

    /**
     * Sắp xếp các lá bài để phân định thắng thua khi cùng loại Hand (Tie-breaker).
     * Ưu tiên nhóm bài xuất hiện nhiều hơn trước, sau đó xét giá trị rank giảm dần.
     */
    private List<Card> getTieBreakerCards() {
        Map<Rank, Integer> counts = getRankFrequencies();
        boolean aceLow = isAceLowStraight(counts);

        List<Card> sorted = new ArrayList<>(aCards);
        sorted.sort(new Comparator<Card>() {
            @Override
            public int compare(Card c1, Card c2) {
                int freq1 = counts.get(c1.rank());
                int freq2 = counts.get(c2.rank());
                if (freq1 != freq2) {
                    return Integer.compare(freq2, freq1); // Tần suất lớn hơn xếp trước
                }
                int val1 = getPokerRankValue(c1.rank(), aceLow);
                int val2 = getPokerRankValue(c2.rank(), aceLow);
                return Integer.compare(val2, val1); // Giá trị bài lớn hơn xếp trước
            }
        });
        return sorted;
    }

    @Override
    public int compareTo(Hand pOther) {
        assert pOther != null;
        if (this.size() != POKER_HAND_SIZE || pOther.size() != POKER_HAND_SIZE) {
            throw new IllegalStateException("Ca 2 bo bai phai co du 5 la de so sanh.");
        }

        // 1. So sánh theo cấp bậc bộ bài (Straight Flush > Four of a kind > ...)
        int typeDiff = this.getHandType().compareTo(pOther.getHandType());
        if (typeDiff != 0) {
            return typeDiff;
        }

        // 2. Nếu hòa loại: So sánh từng lá theo thứ tự ưu tiên tie-breaker
        List<Card> thisCards = this.getTieBreakerCards();
        List<Card> otherCards = pOther.getTieBreakerCards();

        boolean thisAceLow = isAceLowStraight(this.getRankFrequencies());
        boolean otherAceLow = isAceLowStraight(pOther.getRankFrequencies());

        for (int i = 0; i < POKER_HAND_SIZE; i++) {
            int val1 = getPokerRankValue(thisCards.get(i).rank(), thisAceLow);
            int val2 = getPokerRankValue(otherCards.get(i).rank(), otherAceLow);
            if (val1 != val2) {
                return Integer.compare(val1, val2);
            }
        }

        return 0; // Hòa tuyệt đối
    }

    @Override
    public String toString() {
        return aCards.toString() + " (" + (aCards.size() == POKER_HAND_SIZE ? getHandType() : "Incomplete") + ")";
    }
}