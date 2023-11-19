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
package org.apache.dubbo.rpc.proxy;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Constants;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.model.ConsumerModel;
import org.apache.dubbo.rpc.model.ServiceModel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * InvokerHandler
 */
public class InvokerInvocationHandler implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(InvokerInvocationHandler.class);

    private final Invoker<?> invoker;

    private final ServiceModel serviceModel;

    private final String protocolServiceKey;

    public InvokerInvocationHandler(Invoker<?> handler  ) {// 传入的invoker可能是一个ClusterInvoker对象，要追溯到RegistryProtocol.refer方法和Cluster.join方法
        this.invoker = handler;
        URL url = invoker.getUrl();
        this.protocolServiceKey = url.getProtocolServiceKey();
        this.serviceModel = url.getServiceModel();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {// invoke方法 -- 万物皆可invoke
        // 如果是Object定义的方法，直接用标准的反射
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);
        }
        // 特殊方法，特殊处理，非重点
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            if ("toString".equals(methodName)) {
                return invoker.toString();
            } else if ("$destroy".equals(methodName)) {
                invoker.destroy();
                return null;
            } else if ("hashCode".equals(methodName)) {
                return invoker.hashCode();
            }
        } else if (parameterTypes.length == 1 && "equals".equals(methodName)) {
            return invoker.equals(args[0]);
        }
        /*
        * 一般方法走远程调用，这是核心
        * 主要逻辑是封装RpcInvocation远程调用对象，然后基于InvocationUtil发起调用
        * */
        RpcInvocation rpcInvocation = new RpcInvocation(serviceModel, method.getName(), invoker.getInterface().getName(), protocolServiceKey, method.getParameterTypes(), args);

        if (serviceModel instanceof ConsumerModel) {
            rpcInvocation.put(Constants.CONSUMER_MODEL, serviceModel);
            rpcInvocation.put(Constants.METHOD_MODEL, ((ConsumerModel) serviceModel).getMethodModel(method));
        }
        // 正式发起远程调用，
        return InvocationUtil.invoke(invoker, rpcInvocation);
    }
}
