package spi.test;


import com.alibaba.dubbo.common.extension.ExtensionLoader;

import com.alibaba.dubbo.common.URL;

public class AdaptiveWheelMaker implements WheelMaker {
    @Override
    public Wheel makeWheel(URL url) {
        if(url==null){
            return null;

        }

        String wheelMakerName = url.getParameter("Wheel.maker");
        if(wheelMakerName==null){
            return null;
        }

        WheelMaker wheelMaker = ExtensionLoader.getExtensionLoader(WheelMaker.class).getAdaptiveExtension();
        return wheelMaker.makeWheel(url);
    }
}
