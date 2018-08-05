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
package org.apache.dubbo.config.spring;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.ModuleConfig;
import org.apache.dubbo.config.MonitorConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.spring.extension.SpringExtensionFactory;
import org.apache.dubbo.config.support.Parameter;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ReferenceFactoryBean
 *
 * @export
 */
public class ReferenceBean<T> extends ReferenceConfig<T> implements FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean {

    private static final long serialVersionUID = 213195494150089726L;

    private transient ApplicationContext applicationContext;

    public ReferenceBean() {
        super();
    }

    public ReferenceBean(Reference reference) {
        super(reference);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        SpringExtensionFactory.addApplicationContext(applicationContext);
    }

    // FactoryBean 的方法;
    @Override
    public Object getObject() throws Exception {
        return get();
    }

    @Override
    public Class<?> getObjectType() {
        return getInterfaceClass();
    }

    @Override
    @Parameter(excluded = true)
    public boolean isSingleton() {
        return true;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void afterPropertiesSet() throws Exception {
        // 如果consumer还没注册, 则设置 consumer 属性;
        if (getConsumer() == null) {
            //获取applicationContext这个IOC容器实例中的所有ConsumerConfig
            Map<String, ConsumerConfig> consumerConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ConsumerConfig.class, false, false);
            //如果IOC容器中存在这样的ConsumerConfig
            if (consumerConfigMap != null && consumerConfigMap.size() > 0) {
                ConsumerConfig consumerConfig = null;
                //遍历这些ConsumerConfig
                for (ConsumerConfig config : consumerConfigMap.values()) {
                    //如果用户没配置Consumer系统会生成一个默认Consumer，且它的isDefault返回true
                    //这里是说要么是Consumer是默认的要么是用户配置的Consumer并且没设置isDefault属性
                    if (config.isDefault() == null || config.isDefault().booleanValue()) {
                        //防止存在两个默认Consumer
                        if (consumerConfig != null) {
                            throw new IllegalStateException("Duplicate consumer configs: " + consumerConfig + " and " + config);
                        }
                        //获取默认Consumer
                        consumerConfig = config;
                    }
                }
                if (consumerConfig != null) {
                    //设置默认Consumer
                    setConsumer(consumerConfig);
                }
            }
        }

        //如果 reference 未绑定application且（reference未绑定consumer或reference绑定的consumer没绑定application
        if (getApplication() == null
                && (getConsumer() == null || getConsumer().getApplication() == null)) {
            //获取IOC中所有application的实例
            Map<String, ApplicationConfig> applicationConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ApplicationConfig.class, false, false);
            //如果IOC中存在application
            if (applicationConfigMap != null && applicationConfigMap.size() > 0) {
                ApplicationConfig applicationConfig = null;
                //遍历这些application
                for (ApplicationConfig config : applicationConfigMap.values()) {
                    //如果application是默认创建或者被指定成默认
                    if (config.isDefault() == null || config.isDefault().booleanValue()) {
                        if (applicationConfig != null) {
                            throw new IllegalStateException("Duplicate application configs: " + applicationConfig + " and " + config);
                        }
                        //获取application
                        applicationConfig = config;
                    }
                }
                if (applicationConfig != null) {
                    //关联到reference
                    setApplication(applicationConfig);
                }
            }
        }

        //如果 reference 未绑定module且（reference未绑定consumer或reference绑定的consumer没绑定module
        if (getModule() == null
                && (getConsumer() == null || getConsumer().getModule() == null)) {
            //获取IOC中所有module的实例
            Map<String, ModuleConfig> moduleConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ModuleConfig.class, false, false);
            if (moduleConfigMap != null && moduleConfigMap.size() > 0) {
                ModuleConfig moduleConfig = null;
                //遍历这些module
                for (ModuleConfig config : moduleConfigMap.values()) {
                    //如果module是默认创建或者被指定成默认
                    if (config.isDefault() == null || config.isDefault().booleanValue()) {
                        if (moduleConfig != null) {
                            throw new IllegalStateException("Duplicate module configs: " + moduleConfig + " and " + config);
                        }
                        //获取module
                        moduleConfig = config;
                    }
                }
                if (moduleConfig != null) {
                    //关联到reference
                    setModule(moduleConfig);
                }
            }
        }

        //如果reference未绑定注册中心（Register）且（reference未绑定consumer或referenc绑定的consumer没绑定注册中心（Register）
        if ((getRegistries() == null || getRegistries().isEmpty())
                && (getConsumer() == null || getConsumer().getRegistries() == null || getConsumer().getRegistries().isEmpty())
                && (getApplication() == null || getApplication().getRegistries() == null || getApplication().getRegistries().isEmpty())) {
            //获取IOC中所有的注册中心（Register）实例
            Map<String, RegistryConfig> registryConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RegistryConfig.class, false, false);
            if (registryConfigMap != null && registryConfigMap.size() > 0) {
                List<RegistryConfig> registryConfigs = new ArrayList<RegistryConfig>();
                //遍历这些registry
                for (RegistryConfig config : registryConfigMap.values()) {
                    //如果registry是默认创建或者被指定成默认
                    if (config.isDefault() == null || config.isDefault().booleanValue()) {
                        registryConfigs.add(config);
                    }
                }
                if (registryConfigs != null && !registryConfigs.isEmpty()) {
                    //关联到reference，此处可以看出一个consumer可以绑定多个registry（注册中心）
                    super.setRegistries(registryConfigs);
                }
            }
        }

        //如果reference未绑定监控中心（Monitor）且（reference未绑定consumer或reference绑定的consumer没绑定监控中心（Monitor）
        if (getMonitor() == null
                && (getConsumer() == null || getConsumer().getMonitor() == null)
                && (getApplication() == null || getApplication().getMonitor() == null)) {
            //获取IOC中所有的监控中心（Monitor）实例
            Map<String, MonitorConfig> monitorConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, MonitorConfig.class, false, false);
            if (monitorConfigMap != null && monitorConfigMap.size() > 0) {
                MonitorConfig monitorConfig = null;
                //遍历这些监控中心（Monitor）
                for (MonitorConfig config : monitorConfigMap.values()) {
                    //如果monitor是默认创建或者被指定成默认
                    if (config.isDefault() == null || config.isDefault().booleanValue()) {
                        if (monitorConfig != null) {
                            throw new IllegalStateException("Duplicate monitor configs: " + monitorConfig + " and " + config);
                        }
                        monitorConfig = config;
                    }
                }
                if (monitorConfig != null) {
                    //关联到reference,一个consumer绑定到一个监控中心（monitor）
                    setMonitor(monitorConfig);
                }
            }
        }

        /*
        上面部分 Reference确认生成Invoker所需要的组件是否已经准备好，都准备好后我们进入生成Invoker的部分;
         */

        // 如果 reference 配置了 init 为 true, 表明需要饥饿初始化, 则立刻初始化;
        // 否则等到有人注入或引用该实例时再初始化;
        Boolean b = isInit();
        if (b == null && getConsumer() != null) {
            b = getConsumer().isInit();
        }
        if (b != null && b.booleanValue()) {
            getObject();
        }
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
