package com.pancake.enums;

/**
 * 使用枚举标示常量
 * Created by m on 2017/6/9.
 */
public enum StatEnum {
    SUCCESS(0, "成功"),
    ERROR(1, "失败")
    ;
    private int state;

    private String stateInfo;

    StatEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public static StatEnum stateOf(int index) {
        for (StatEnum state : values()) {
            if (state.getState() == index) {
                return state;
            }
        }
        return null;
    }
}
