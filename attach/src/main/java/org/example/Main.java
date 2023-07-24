package org.example;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.example.common.AnsiLog;
import org.example.utils.JavaVersionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Main {

    /**
     * 列出所有的JAVA PID
     * <p>
     * 可以通过进程名自动过滤，例如
     * TOMCAT的进程名称：org.apache.catalina.startup.Bootstrap
     * <p>
     * TODO：未处理多web容器的情况
     */
    public static String get_java_pid() {

        Map<String, String> web_container_map = new HashMap<>();
        web_container_map.put("tomcat", "org.apache.catalina.startup.Bootstrap");

        List<VirtualMachineDescriptor> vml = VirtualMachine.list();

        for (VirtualMachineDescriptor vmd: vml) {
            System.out.println(vmd.displayName());
            for (String key:
                 web_container_map.keySet()) {
                if(vmd.displayName().contains(web_container_map.get(key))) {
                    AnsiLog.info("Find a web container for " + key + " : " + vmd.id() + " - " + vmd.displayName());
                    return vmd.id();
                }
            }
        }

        AnsiLog.error("No web container find");
        System.exit(-1);
        return "-1";
    }

    public static void attach(String jvm_pid, String agent_jar_path, String filterClass) throws Exception {
        VirtualMachine virtualMachine = null;
        VirtualMachineDescriptor virtualMachineDescriptor = null;
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            String pid = descriptor.id();
            if (pid.equals(jvm_pid)) {
                virtualMachineDescriptor = descriptor;
                break;
            }
        }
        try {
            if (null == virtualMachineDescriptor) {
                virtualMachine = VirtualMachine.attach(jvm_pid);
            } else {
                virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
            }
            Properties targetSystemProperties = virtualMachine.getSystemProperties();
            String targetJavaVersion = JavaVersionUtils.javaVersionStr(targetSystemProperties);
            String currentJavaVersion = JavaVersionUtils.javaVersionStr();
            if (targetJavaVersion != null && currentJavaVersion != null) {
                if (!targetJavaVersion.equals(currentJavaVersion)) {
                    AnsiLog.warn("Current VM java version: {} do not match target VM java version: {}, attach may fail.", currentJavaVersion, targetJavaVersion);
                    AnsiLog.warn("Target VM JAVA_HOME is {}, copagent JAVA_HOME is {}, try to set the same JAVA_HOME.", targetSystemProperties.getProperty("java.home"), System.getProperty("java.home"));
                }
            }
            virtualMachine.loadAgent(agent_jar_path, filterClass);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (null != virtualMachine) {
                virtualMachine.detach();
            }
        }
    }

    public static void main(String[] args) throws Exception {

        String java_pid = get_java_pid();
        AnsiLog.info("start attach " + java_pid);

        //TODO 确定agent jar 的位置
        //"C:\\Program Files\\Java\\jdk1.8.0_102\\jre\\bin\\java.exe" "-Xbootclasspath/a:C:\Program Files\Java\jdk1.8.0_102\lib\tools.jar" -jar membot.jar
        String agent_jar_path = "C:\\Users\\fengkb\\IdeaProjects\\membot\\agent\\target\\agent.jar";
        String filterClassName = "[unknown]";
        AnsiLog.info("Try to attach process " + java_pid + ", please wait a moment ...");

        attach(java_pid, agent_jar_path, filterClassName);

        AnsiLog.info("Attach process {} finished .", java_pid);
        System.exit(0);
    }
}