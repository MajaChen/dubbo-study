package org.apache.dubbo.test.spi;

import org.apache.dubbo.common.URL;

public class SimpleExtImpl3 implements SimpleExt {
    @Override
    public String echo(URL url, String s) {
        return null;
    }
    
    @Override
    public void printA(URL url) {
        System.out.println("print-A: I'm SimpleExtImpl3");
    }
    
    @Override
    public void printB(URL url) {
        System.out.println("print-B: I'm SimpleExtImpl3");
    }
    
    @Override
    public void printC(URL url) {
        System.out.println("print-C: I'm SimpleExtImpl3");
    }
}
