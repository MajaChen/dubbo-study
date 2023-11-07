package org.apache.dubbo.test.spi;

import org.apache.dubbo.rpc.model.ApplicationModel;


public class AdaptiveDubboSPIDemo {
    public static <URL> void main(String[] args) {
        org.apache.dubbo.common.URL url=org.apache.dubbo.common.URL.valueOf("dubbo://0.0.0.0:6666/test?key1=impl1&key3=impl3&simple.ext=impl2");
        SimpleExt simpleExt= ApplicationModel.defaultModel().getDefaultModule().getExtensionLoader(SimpleExt.class).getAdaptiveExtension();
        simpleExt.printA(url);
        simpleExt.printB(url);
        simpleExt.printC(url);
        simpleExt.echo(url, "hello");
        SimpleExt impl1 = ApplicationModel.defaultModel().getDefaultModule().getExtensionLoader(SimpleExt.class).getExtension("impl1");
        impl1.printA(url);
    }
}
