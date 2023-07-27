package org.example.common;

import java.util.HashMap;
import java.util.Map;

public class Constant {

    public static Map<String, String> WEB_CONTAINER_MAP = new HashMap<String, String>() {
        {
            put("tomcat", "org.apache.catalina.startup.Bootstrap");
        }
    };
}
