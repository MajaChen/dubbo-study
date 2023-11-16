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
package org.apache.dubbo.remoting.exchange.support.header;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.remoting.Transporters;
import org.apache.dubbo.remoting.exchange.ExchangeClient;
import org.apache.dubbo.remoting.exchange.ExchangeHandler;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.remoting.exchange.Exchanger;
import org.apache.dubbo.remoting.exchange.PortUnificationExchanger;
import org.apache.dubbo.remoting.transport.DecodeHandler;

import static org.apache.dubbo.remoting.Constants.IS_PU_SERVER_KEY;

/**
 * DefaultMessenger
 *
 *
 */
public class HeaderExchanger implements Exchanger {// Exchanger的定位是ExchangeClient的工厂

    public static final String NAME = "header";

    @Override
    public ExchangeClient connect(URL url, ExchangeHandler handler) throws RemotingException {
        /**
        * 创建Client对象，在此基础上创建HeaderExchangeClient
        * 对传递进来的ExchangeHandlerAdapter，先包了一层HeaderExchangeHandler，再包了一层DecodeHandler
         * 所以请求的处理顺序是DecodeHandler -> HeaderExchangeHandler -> handler
        * */
        return new HeaderExchangeClient(Transporters.connect(url, new DecodeHandler(new HeaderExchangeHandler(handler))), true);
    }

    @Override
    public ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {// 用于provider侧，创建ExchangeServer，绑定url和handler，核心逻辑在handler
        ExchangeServer server;
        boolean isPuServerKey = url.getParameter(IS_PU_SERVER_KEY, false);
        if(isPuServerKey) {
            server = new HeaderExchangeServer(PortUnificationExchanger.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler))));
        }else {
            /**
             * 再包了一层，内层是Transporter，外层是ExchangeServer
             * 对传递进来的ExchangeHandlerAdapter，先包了一层HeaderExchangeHandler，再包了一层DecodeHandler
             * 所以请求的处理顺序是DecodeHandler -> HeaderExchangeHandler -> handler
             */
            server = new HeaderExchangeServer(Transporters.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler))));
        }
        return server;
    }

}
