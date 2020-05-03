package com.malcolm.joules.utiils;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JoulesUtilTest {

    @Test
    public void shuffleAudioList() throws Exception {
        //NOTE TEST WITHOUT ANDROID API IMPLEMENTATION
        final int start = 0;
        final int end = 63;
        final int size = 64;

        assertTrue(JoulesUtil.shuffleAudioList(start,end) instanceof ArrayList);
        assertEquals(JoulesUtil.shuffleAudioList(start, end).size(), size);
    }
}