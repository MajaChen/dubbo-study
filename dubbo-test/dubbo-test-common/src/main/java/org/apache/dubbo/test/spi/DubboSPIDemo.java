package org.apache.dubbo.test.spi;

import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.rpc.model.ApplicationModel;

public class DubboSPIDemo {
    public static void main(String[] args) {
        // 使用扩展类加载器加载指定扩展的实现
        ExtensionLoader<DataBaseSPI> dataBaseSpis = ApplicationModel.defaultModel().getDefaultModule().getExtensionLoader(DataBaseSPI.class);
        // 根据指定的名称加载扩展实例(与dubbo.spi.DataBaseSPI中一致)
        DataBaseSPI spi = dataBaseSpis.getExtension("mysql");
        spi.dataBaseOperation();
        
        DataBaseSPI spi2 = dataBaseSpis.getExtension("oracle");
        spi2.dataBaseOperation();
    }
}

