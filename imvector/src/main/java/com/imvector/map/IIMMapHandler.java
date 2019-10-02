package com.imvector.map;

import com.imvector.InitChannel;
import io.netty.channel.ChannelHandler;

/**
 * 获取过滤映射层处理器，必须不能为空，
 * 因为需要这一层映射(添加)到业务处理层
 * <p>
 * 注意：过滤映射层处理器处理完之后需要替换业务层处理器，此过程有具体实现完成
 *
 * @author: vector.huang
 * @date: 2019/10/02 01:11
 */
public interface IIMMapHandler {

    /**
     * 获取过滤映射层处理器，对进入的连接进行过滤和映射
     * 参考 {@link InitChannel} 的实现
     *
     * @return 过滤映射层处理器
     */
    ChannelHandler[] getMapHandler();


}
