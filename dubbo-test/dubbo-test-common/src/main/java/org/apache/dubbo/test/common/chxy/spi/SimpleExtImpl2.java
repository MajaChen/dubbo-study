package org.apache.dubbo.test.common.chxy.spi;

import org.apache.dubbo.common.URL;

public class SimpleExtImpl2 implements SimpleExt {
    @Override
    public String echo(URL url, String s) {
        return null;
    }
    
    @Override
    public void printA(URL url) {
        System.out.println("print-A: I'm SimpleExtImpl2");
    }
    
    @Override
    public void printB(URL url) {
        System.out.println("print-B: I'm SimpleExtImpl2");
    }
    
    @Override
    public void printC(URL url) {
        System.out.println("print-C: I'm SimpleExtImpl2");
    }
}
