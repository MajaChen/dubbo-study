package org.apache.dubbo.test.common.chxy.spi;

import org.apache.dubbo.common.extension.SPI;

@SPI  // 注解标记当前接口为扩展点
public interface DataBaseSPI {
    public void dataBaseOperation();
}
