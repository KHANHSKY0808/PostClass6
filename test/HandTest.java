/*******************************************************************************
 * Solitaire
 * 
 * Copyright (C) 2025 by Martin P. Robillard
 * 
 * See: https://github.com/prmr/Solitaire
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package ca.mcgill.solitaire.cards;

import static ca.mcgill.solitaire.testutils.Cards.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

public class HandTest {

    private static Hand hand(Card... pCards) {
        Hand hand = new Hand();
        for (Card card : pCards) {
            hand.add(card);
        }
        return hand;
    }

    private static boolean invokeIsStraightFlush(Hand pHand) {
        try {
            Method method = Hand.class.getDeclaredMethod("isStraightFlush");
            method.setAccessible(true);
            return (boolean) method.invoke(pHand);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            fail();
            return false;
        }
    }

    @Test
    void testIsStraightFlush_True() {
        Hand hand = hand(C2C, C3C, C4C, C5C, C6C);
        assertTrue(invokeIsStraightFlush(hand));
    }

    @Test
    void testIsStraightFlush_WrongSuit() {
        Hand hand = hand(C2C, C3C, C4C, C5C, C6D);
        assertFalse(invokeIsStraightFlush(hand));
    }

    @Test
    void testIsStraightFlush_WrongRank() {
        Hand hand = hand(C2C, C3C, C4C, C5C, C7C);
        assertFalse(invokeIsStraightFlush(hand));
    }

    // =========================================================================
    // Các test case bổ sung thêm (Optional)
    // =========================================================================

    /**
     * Bổ sung thêm: Kiểm tra trường hợp các lá bài được đưa vào theo thứ tự
     * lộn xộn (không tăng dần), xác nhận hàm tự tạo bản sao và sắp xếp đúng.
     */
    @Test
    void testIsStraightFlush_UnsortedInput() {
        Hand hand = hand(C6C, C2C, C4C, C3C, C5C);
        assertTrue(invokeIsStraightFlush(hand));
    }

    /**
     * Bổ sung thêm: Kiểm tra trường hợp sảnh thấp có Ace (A-2-3-4-5 cùng chất)
     * hợp lệ vì Ace có ordinal = 0.
     */
    @Test
    void testIsStraightFlush_AceLow_True() {
        Hand hand = hand(CAC, C2C, C3C, C4C, C5C);
        assertTrue(invokeIsStraightFlush(hand));
    }

    /**
     * Bổ sung thêm: Kiểm tra sảnh cao 10-J-Q-K-A cùng chất trả về false,
     * xác nhận rằng hàm chỉ tính Ace là lá thấp (không hỗ trợ Ace-high).
     */
    @Test
    void testIsStraightFlush_AceHigh_False() {
        Hand hand = hand(CTC, CJC, CQC, CKC, CAC);
        assertFalse(invokeIsStraightFlush(hand));
    }
}
