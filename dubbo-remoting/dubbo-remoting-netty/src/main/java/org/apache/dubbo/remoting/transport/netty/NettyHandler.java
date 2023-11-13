/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.remoting.transport.netty;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.ChannelHandler;

import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NettyHandler
 */
@Sharable
public class NettyHandler extends SimpleChannelHandler {// 本质上是一个handler，用于处理请求，问题是：他是在哪里创建的？如何将请求关联到它？

    private static final Logger logger = LoggerFactory.getLogger(NettyHandler.class);

    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>(); // <ip:port, channel>

    private final URL url;

    private final ChannelHandler handler;// 事件处理器

    /*
    * 创建溯源：
    * server端：创建Exporter暴露服务时，从DubboProtocol.openServer开始追溯，url是provider侧的服务标识符，如dubbo://...，handler是DubboProtocol.requestHandler
    *
    * */
    public NettyHandler(URL url, ChannelHandler handler) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.url = url;
        this.handler = handler;
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), url, handler);
        try {
            if (channel != null) {
                channels.put(NetUtils.toAddressString((InetSocketAddress) ctx.getChannel().getRemoteAddress()), channel);
            }
            handler.connected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }

        if (logger.isInfoEnabled()) {
            logger.info("The connection between " + channel.getRemoteAddress() + " and " + channel.getLocalAddress() + " is established");
        }
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), url, handler);
        try {
            channels.remove(NetUtils.toAddressString((InetSocketAddress) ctx.getChannel().getRemoteAddress()));
            handler.disconnected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }

        if (logger.isInfoEnabled()) {
            logger.info("The connection between " + channel.getRemoteAddress() + " and " + channel.getLocalAddress() + " is disconnected");
        }
    }

    /*
    * 从netty信道收到请求后的处理逻辑
    * 什么时候会调用这个方法？
    * 此方法定义在SimpleChannelHandler中，这个类属于netty，是netty框架开发给用户用于定义：收到请求之后如何处理请求
    *
    * 重点：NettyHandler是所有service共用的，无论client请求哪个service
    * */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {// 请求被反序列化后，会首先进入此方法处理
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), url, handler);// 获取或者创建NettyChannel对象
        try {
            handler.received(channel, e.getMessage());// 调用handler进行处理 - 忽略中间的垃圾代码，直接进入AllChannelHandler
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        super.writeRequested(ctx, e);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), url, handler);
        try {
            handler.sent(channel, e.getMessage());
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), url, handler);
        try {
            handler.caught(channel, e.getCause());
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }

}
