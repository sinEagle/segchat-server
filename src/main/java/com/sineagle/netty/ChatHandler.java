package com.sineagle.netty;

import com.sineagle.enums.MsgActionEnum;
import com.sineagle.utils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;

/**
 * 处理消息的Handler
 * TextWebSocketFrame: 在netty中，是用于专门为websocket专门处理文本的对象，frame是消息的载体
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    // 用于记录和管理所有客户端的channel
    private static ChannelGroup users = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 获取客户端传输过来的消息
        String content = msg.text();

        // 1. 获取客户端发来的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        Integer action = dataContent.getAction();
        // 2. 判断消息类型，根据不同的类型来处理不同的业务
        if (action == MsgActionEnum.CONNECT.type) {
            //  2.1 当websocket第一次open的时候，初始化channel, 把用户的channel和userid关联起来

        } else if (action == MsgActionEnum.CHAT.type) {
            //  2.2 聊天类型的消息，把聊天记录保存到数据库，同时标记消息的签收状态

        } else if (action == MsgActionEnum.SIGNED.type) {
            //  2.3 签收消息的类型，针对具体的消息进行签收，修改数据库中的对应消息的签收状态【已签收】

        } else if (action == MsgActionEnum.KEEPALIVE.type) {
            //  2.4 心跳类型的消息

        }
    }

    /**
     * 当客户端连接服务器后（打开连接）
     * 获取客户端的channel, 并放到ChannelGroup中进行管理
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接成功！");
        users.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 当触发handlerRemoved，ChannelGroup会自动移除对应客户端的channel
        users.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生异常之后关闭连接（关闭channel）, 随后从ChannelGroup中移除
        ctx.channel().close();
        users.remove(ctx.channel());
    }
}
