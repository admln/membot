package org.example.common;

import java.util.ArrayList;
import java.util.List;

public class Constant {

    /**
     * 风险包名
     */
    public static List<String> SUS_PACKAGES = new ArrayList<String>() {
        {
            add("net.rebeyond.");
            add("com.metasploit.");
        }
    };

    /**
     * 风险父类名
     */
    public static List<String> SUS_SUPER_CLASSNAMES = new ArrayList<String>() {
        {
            add("javax.servlet.http.HttpServlet");
            add("javax.servlet.GenericServlet");
            add("org.apache.jasper.runtime.HttpJspBase");
        }
    };

    /**
     * 风险接口名
     */
    public static List<String> SUS_INTERFACES = new ArrayList<String>() {
        {
            add("javax.servlet.Filter");
            add("javax.servlet.Servlet");
            add("javax.servlet.ServletRequestListener");
            add("org.apache.jasper.runtime.JspSourceDependent");
        }
    };

    /**
     * 风险注解
      */
    public static List<String> SUS_ANNOTATIONS = new ArrayList<String>() {
        {
            add("org.springframework.stereotype.Controller");
            add("org.springframework.web.bind.annotation.RestController");
            add("org.springframework.web.bind.annotation.RequestMapping");
            add("org.springframework.web.bind.annotation.GetMapping");
            add("org.springframework.web.bind.annotation.PostMapping");
            add("org.springframework.web.bind.annotation.PatchMapping");
            add("org.springframework.web.bind.annotation.PutMapping");
            add("org.springframework.web.bind.annotation.Mapping");
        }
    };

    /**
     * 风险关键字
     */
    public static List<String> RISK_KEYWORD = new ArrayList<String>() {
        {
            add("javax.crypto.");
            add("ProcessBuilder");
            add("getRuntime");
            add("shell");
        }
    };

}
