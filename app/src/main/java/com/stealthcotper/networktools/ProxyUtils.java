package com.stealthcotper.networktools;

import android.content.Context;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ProxyUtils {
    public static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    public static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    public static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }

    public static void setProxySettings(String assign, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        setEnumField(wifiConf, assign, "proxySettings");
    }


    WifiConfiguration GetCurrentWifiConfiguration(WifiManager manager) {
        if (!manager.isWifiEnabled())
            return null;

        List<WifiConfiguration> configurationList = manager.getConfiguredNetworks();
        WifiConfiguration configuration = null;
        int cur = manager.getConnectionInfo().getNetworkId();
        for (int i = 0; i < configurationList.size(); ++i) {
            WifiConfiguration wifiConfiguration = configurationList.get(i);
            if (wifiConfiguration.networkId == cur)
                configuration = wifiConfiguration;
        }

        return configuration;
    }

    void setWifiProxySettings(Context context) {
        //get the current wifi configuration
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = GetCurrentWifiConfiguration(manager);
        if (null == config)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ProxyInfo info = ProxyInfo.buildDirectProxy("30.10.88.163", 8888);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                config.setHttpProxy(info);
            }
        }
//        try {
//            //get the link properties from the wifi configuration
//            Object linkProperties = getField(config, "linkProperties");
//            if (null == linkProperties)
//                return;
//
//            //get the setHttpProxy method for LinkProperties
//            Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
//            Class[] setHttpProxyParams = new Class[1];
//            setHttpProxyParams[0] = proxyPropertiesClass;
//            Class lpClass = Class.forName("android.net.LinkProperties");
//            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
//            setHttpProxy.setAccessible(true);
//
//            //get ProxyProperties constructor
//            Class[] proxyPropertiesCtorParamTypes = new Class[3];
//            proxyPropertiesCtorParamTypes[0] = String.class;
//            proxyPropertiesCtorParamTypes[1] = int.class;
//            proxyPropertiesCtorParamTypes[2] = String.class;
//
//            Constructor proxyPropertiesCtor = proxyPropertiesClass.getConstructor(proxyPropertiesCtorParamTypes);
//
//            //create the parameters for the constructor
//            Object[] proxyPropertiesCtorParams = new Object[3];
//            proxyPropertiesCtorParams[0] = "30.10.88.163";
//            proxyPropertiesCtorParams[1] = 8888;
//            proxyPropertiesCtorParams[2] = null;
//
//            //create a new object using the params
//            Object proxySettings = proxyPropertiesCtor.newInstance(proxyPropertiesCtorParams);
//
//            //pass the new object to setHttpProxy
//            Object[] params = new Object[1];
//            params[0] = proxySettings;
//            setHttpProxy.invoke(linkProperties, params);
//
//            setProxySettings("STATIC", config);
//
//            //save the settings
//            manager.updateNetwork(config);
//            manager.disconnect();
//            manager.reconnect();
//        } catch (Exception e) {
//        }
    }

    void unsetWifiProxySettings(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = GetCurrentWifiConfiguration(manager);
        if (null == config)
            return;

        try {
            //get the link properties from the wifi configuration
            Object linkProperties = getField(config, "linkProperties");
            if (null == linkProperties)
                return;

            //get the setHttpProxy method for LinkProperties
            Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class lpClass = Class.forName("android.net.LinkProperties");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);

            //pass null as the proxy
            Object[] params = new Object[1];
            params[0] = null;
            setHttpProxy.invoke(linkProperties, params);

            setProxySettings("NONE", config);

            //save the config
            manager.updateNetwork(config);
            manager.disconnect();
            manager.reconnect();
        } catch (Exception e) {
        }
    }
}
