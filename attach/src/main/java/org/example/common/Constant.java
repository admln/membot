package org.example.common;

import java.util.HashMap;
import java.util.Map;

public class Constant {

    public static Map<String, String> WEB_CONTAINER_MAP = new HashMap<String, String>() {
        {
            put("tomcat", "org.apache.catalina.startup.Bootstrap");
            put("weblogic", "weblogic.Server");
            put("jboss", "org.jboss.modules.Main");
            put("wildfly", "org.wildfly.bootable.launcher.WildFlyLauncher");
            put("websphere", "com.ibm.ws.bootstrap.WSLauncher");
            put("jetty", "org.eclipse.jetty.start.Main");
            put("resin", "com.caucho.server.resin.Resin");
        }
    };
}
