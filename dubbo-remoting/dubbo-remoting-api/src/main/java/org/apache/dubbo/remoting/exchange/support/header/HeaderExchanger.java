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
import org.apache.dubbo.remoting.transport.DecodeHandler;

/**
 * DefaultMessenger
 *
 *
 */
public class HeaderExchanger implements Exchanger {

    public static final String NAME = "header";

    @Override
    public ExchangeClient connect(URL url, ExchangeHandler handler) throws RemotingException {
        //先经过HeaderExchangeHandler包装
        //然后是DecodeHandler
        //然后是Transporters.connect
        //返回一个HeaderExchangerClient，这里封装了client，channel，启动心跳的定时器等

        // transporters.connect中也是根据SPI扩展获取Transport的具体实现，这里默认使用NettyTransporter.connect()，
        // 在NettyTransporter的connect方法中直接返回一个NettyClient(url, listener)
        return new HeaderExchangeClient(Transporters.connect(url, new DecodeHandler(new HeaderExchangeHandler(handler))), true);
    }

    @Override
    public ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
        // 调用 com.alibaba.dubbo.remoting.Transporters#bind(com.alibaba.dubbo.common.URL, com.alibaba.dubbo.remoting.ChannelHandler...)
        return new HeaderExchangeServer(Transporters.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler))));
    }

}
