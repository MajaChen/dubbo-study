package org.apache.dubbo.test.spi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;

@SPI("impl1")
public interface SimpleExt {
    
    String echo(URL url,String s);
    
    @Adaptive({"key4"})
    void printA(URL url);
    
    @Adaptive
    void printB(URL url);
    
    @Adaptive({"key3","key2","key1"})
    void printC(URL url);
}
