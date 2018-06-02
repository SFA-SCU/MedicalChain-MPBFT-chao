package com.pancake.entity.enumeration;

/**
 * Created by chao on 2018/6/2.
 */
public enum TxType {
    INSERT("insert"), UPDATE("update"), DELETE("delete");
    private final String name;

    TxType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
