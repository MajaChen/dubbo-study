package org.apache.dubbo.test.common.chxy.spi;

public class MysqlDataBaseSPIImpl implements DataBaseSPI {
    
    @Override
    public void dataBaseOperation() {
        System.out.println("Dubbo SPI Operate Mysql database!!!");
    }
}
