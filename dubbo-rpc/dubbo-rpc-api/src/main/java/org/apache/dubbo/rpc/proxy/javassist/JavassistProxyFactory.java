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
package org.apache.dubbo.rpc.proxy.javassist;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.bytecode.Proxy;
import org.apache.dubbo.common.bytecode.Wrapper;
import org.apache.dubbo.common.logger.ErrorTypeAwareLogger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.proxy.AbstractProxyFactory;
import org.apache.dubbo.rpc.proxy.AbstractProxyInvoker;
import org.apache.dubbo.rpc.proxy.InvokerInvocationHandler;
import org.apache.dubbo.rpc.proxy.jdk.JdkProxyFactory;

import java.util.Arrays;

import static org.apache.dubbo.common.constants.LoggerCodeConstants.PROXY_FAILED;

/**
 * JavassistRpcProxyFactory
 */
public class JavassistProxyFactory extends AbstractProxyFactory {// 封装invoker生成consumer侧的proxy
    private static final ErrorTypeAwareLogger logger = LoggerFactory.getErrorTypeAwareLogger(JavassistProxyFactory.class);
    private final JdkProxyFactory jdkProxyFactory = new JdkProxyFactory();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {// 创建代理对象，代理interfaces（一个interface可能继承了其他interface，因此有多个），基于invoker来实现调用
        try {
            // 核心点是：生成代理类org.apache.dubbo.samples.api.GreetingsServiceDubboProxy0并创建其对象，该类实现了目标接口，有一个类型是InvokerInvocationHandler的handler，实现接口中方法的方式是：调用handler.invoke；所以核心是InvokerInvocationHandler;
            return (T) Proxy.getProxy(interfaces).newInstance(new InvokerInvocationHandler(invoker));// 在InvokerInvocationHandler中基于invoker发起远程调用
        } catch (Throwable fromJavassist) {
            // try fall back to JDK proxy factory
            try {
                T proxy = jdkProxyFactory.getProxy(invoker, interfaces);
                logger.error(PROXY_FAILED, "", "", "Failed to generate proxy by Javassist failed. Fallback to use JDK proxy success. " +
                    "Interfaces: " + Arrays.toString(interfaces), fromJavassist);
                return proxy;
            } catch (Throwable fromJdk) {
                logger.error(PROXY_FAILED, "", "", "Failed to generate proxy by Javassist failed. Fallback to use JDK proxy is also failed. " +
                    "Interfaces: " + Arrays.toString(interfaces) + " Javassist Error.", fromJavassist);
                logger.error(PROXY_FAILED, "", "", "Failed to generate proxy by Javassist failed. Fallback to use JDK proxy is also failed. " +
                    "Interfaces: " + Arrays.toString(interfaces) + " JDK Error.", fromJdk);
                throw fromJavassist;
            }
        }
    }

    @Override
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) {// 在provid创建invoker，proxy是接口实现对象，type是原接口，url是标识符
        try {
            // TODO Wrapper cannot handle this scenario correctly: the classname contains '$'
            final Wrapper wrapper = Wrapper.getWrapper(proxy.getClass().getName().indexOf('$') < 0 ? proxy.getClass() : type);// Wrapper是一个抽象方法，仅能通过getWrapper方法创建实例
            return new AbstractProxyInvoker<T>(proxy, type, url) {
                @Override
                protected Object doInvoke(T proxy, String methodName,
                                          Class<?>[] parameterTypes,
                                          Object[] arguments) throws Throwable {
                    return wrapper.invokeMethod(proxy, methodName, parameterTypes, arguments);
                }
            };
        } catch (Throwable fromJavassist) {
            // try fall back to JDK proxy factory
            try {
                Invoker<T> invoker = jdkProxyFactory.getInvoker(proxy, type, url);
                logger.error(PROXY_FAILED, "", "", "Failed to generate invoker by Javassist failed. Fallback to use JDK proxy success. " +
                    "Interfaces: " + type, fromJavassist);
                // log out error
                return invoker;
            } catch (Throwable fromJdk) {
                logger.error(PROXY_FAILED, "", "", "Failed to generate invoker by Javassist failed. Fallback to use JDK proxy is also failed. " +
                    "Interfaces: " + type + " Javassist Error.", fromJavassist);
                logger.error(PROXY_FAILED, "", "", "Failed to generate invoker by Javassist failed. Fallback to use JDK proxy is also failed. " +
                    "Interfaces: " + type + " JDK Error.", fromJdk);
                throw fromJavassist;
            }
        }
    }

}
