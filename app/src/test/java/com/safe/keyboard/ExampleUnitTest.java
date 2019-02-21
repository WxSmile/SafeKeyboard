package com.safe.keyboard;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void stringToChar() {
        String str = "，";
        char[] chars = str.toCharArray();
        for (char aChar : chars) {
            System.out.println("chars = " + (int)aChar);

        }
    }

    @Test
    public void charToString() {
        int i = 65292;
        char c = (char) i;
        System.out.println("c = " + c);
    }
}