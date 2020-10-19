package com.keray;

/**
 * @author by keray
 * date:2020/9/19 9:47 上午
 */
public interface IPlugins {
    default boolean open() {
        return true;
    }
}
