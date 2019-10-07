package com.imvector.logic;

/**
 * 用户使用的终端
 *
 * @author: vector.huang
 * @date: 2019/10/07 11:59
 */
public interface IMClientPlatform {

    /**
     * 终端标识，不可重复，否则断开连接
     *
     * @return 终端系列码 [0,15]
     */
    int getPlatformSeq();

}
