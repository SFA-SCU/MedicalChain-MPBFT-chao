package com.pancake.util;

import static org.junit.Assert.*;

public class SignatureUtilTest {

    @org.junit.Test
    public void loadPvtKey() {
    }

    @org.junit.Test
    public void loadPvtKeyStr() {
        System.out.println(SignatureUtil.loadPvtKeyStr("EC"));
    }

    @org.junit.Test
    public void loadPubKey() {
    }

    @org.junit.Test
    public void loadPubKeyStr() {
        System.out.println(SignatureUtil.loadPubKeyStr("EC"));
    }
}