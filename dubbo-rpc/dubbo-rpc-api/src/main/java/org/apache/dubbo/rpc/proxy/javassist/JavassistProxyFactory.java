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
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.proxy.AbstractProxyFactory;
import org.apache.dubbo.rpc.proxy.AbstractProxyInvoker;
import org.apache.dubbo.rpc.proxy.InvokerInvocationHandler;

/**
 * JavaassistRpcProxyFactory
 */
public class JavassistProxyFactory extends AbstractProxyFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {

        /*
        创建代理类;
         */
        return (T) Proxy.getProxy(interfaces).newInstance(new InvokerInvocationHandler(invoker));
    }

    @Override
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) {

        /*
        代理方法通过 getInvoker() 来获取invoke, 然后调用 invoke() 的时候会 调用 wrapper.invokeMethod() ;

        proxy: 被代理的service实现类;
        type: service实现类的Class对象;
         */

        // TODO Wrapper cannot handle this scenario correctly: the classname contains '$'
        // 获取service实现类的 wrapper 对象[Wrapper1.class];
        // Wrapper类不能正确处理带$符号的类名;
        final Wrapper wrapper = Wrapper.getWrapper(proxy.getClass().getName().indexOf('$') < 0 ? proxy.getClass() : type);

        // 返回 ProxyInvoker, doInvoke调用包装类的invokeMethod方法;
        return new AbstractProxyInvoker<T>(proxy, type, url) {
            @Override
            protected Object doInvoke(T proxy, String methodName,
                                      Class<?>[] parameterTypes,
                                      Object[] arguments) throws Throwable {

                /*
                wrapper动态代理的代码:
                在底部
                 */

                return wrapper.invokeMethod(proxy, methodName, parameterTypes, arguments);
            }
        };
    }

    /*
    Wrapper1.class源码

    package com.alibaba.dubbo.common.bytecode;

    import com.alibaba.dubbo.common.bytecode.ClassGenerator.DC;
    import com.github.archerda.dubbo.provider.HelloServiceImpl;
    import java.lang.reflect.InvocationTargetException;
    import java.util.Map;

    public class Wrapper1 extends Wrapper implements DC {
        public static String[] pns;
        public static Map pts;
        public static String[] mns;
        public static String[] dmns;
        public static Class[] mts0;

        public Wrapper1() {
        }

        public Object getPropertyValue(Object var1, String var2) {
            try {
                HelloServiceImpl var3 = (HelloServiceImpl)var1;
            } catch (Throwable var5) {
                throw new IllegalArgumentException(var5);
            }

            throw new NoSuchPropertyException("Not found property \"" + var2 + "\" filed or setter method in class com.github.archerda.dubbo.provider.HelloServiceImpl.");
        }

        public String[] getPropertyNames() {
            return pns;
        }

        public void setPropertyValue(Object var1, String var2, Object var3) {
            try {
                HelloServiceImpl var4 = (HelloServiceImpl)var1;
            } catch (Throwable var6) {
                throw new IllegalArgumentException(var6);
            }

            throw new NoSuchPropertyException("Not found property \"" + var2 + "\" filed or setter method in class com.github.archerda.dubbo.provider.HelloServiceImpl.");
        }

        public String[] getMethodNames() {
            return mns;
        }

        public boolean hasProperty(String var1) {
            return pts.containsKey(var1);
        }

        public Object invokeMethod(Object var1, String var2, Class[] var3, Object[] var4) throws InvocationTargetException {
            HelloServiceImpl var5;
            try {
                var5 = (HelloServiceImpl)var1;
            } catch (Throwable var8) {
                throw new IllegalArgumentException(var8);
            }

            try {
                if ("sayHello".equals(var2) && var3.length == 1) {
                    // 直接通过服务的实现对象调用具体方法，并不是通过反射，效率会高些;
                    return var5.sayHello((String)var4[0]);
                }
            } catch (Throwable var9) {
                throw new InvocationTargetException(var9);
            }

            throw new NoSuchMethodException("Not found method \"" + var2 + "\" in class com.github.archerda.dubbo.provider.HelloServiceImpl.");
        }

        public Class getPropertyType(String var1) {
            return (Class)pts.get(var1);
        }

        public String[] getDeclaredMethodNames() {
            return dmns;
        }
    }
     */

}
