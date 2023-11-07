package org.apache.dubbo.test.spi;

public class OracleDataBaseSPIImpl implements DataBaseSPI {
    
    @Override
    public void dataBaseOperation() {
        System.out.println("Dubbo SPI Operate Oracle database!!!");
    }
}
