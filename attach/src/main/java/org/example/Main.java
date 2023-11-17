package org.example;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.example.common.AnsiLog;
import org.example.utils.JavaVersionUtils;
import org.example.utils.PathUtils;
import org.example.utils.ProcessUtils;

import java.io.File;
import java.util.*;

public class Main {

    public static File agent_work_directory = new File(PathUtils.getCurrentDirectory(), ".membot");

    public static void main(String[] args) throws Exception {

        long pid = -1;
        String jvm_pid;
        String filterClassName = "[unknown]";
        String attach_jar_path = null;
        String current_jar_path = PathUtils.getCurrentJarPath();
        AnsiLog.info("current_jarPth: " + current_jar_path);
        AnsiLog.info("Java version: "+ JavaVersionUtils.javaVersionStr());

        List<String> argsList = Arrays.asList(args);

        boolean is_boot_start = argsList.contains("bootstart_flag");
        boolean is_greater_than_jre9 = argsList.contains("greater_than_jdk9_flag");

        if(is_boot_start || is_greater_than_jre9 ){ // 第二次启动

            jvm_pid = ProcessUtils.get_java_pid();
            AnsiLog.info("select attach jvm pid : " + jvm_pid);

            // create membot agent work directory
            if(! agent_work_directory.exists()){
                if(! PathUtils.createDirectory(agent_work_directory)){
                    AnsiLog.warn("Create directory {} failed, use {}", agent_work_directory.getAbsolutePath(), PathUtils.getTempDirectory().getAbsolutePath());
                    agent_work_directory = PathUtils.getTempDirectory();
                }
            }
            // update attach jar path
            attach_jar_path = new File(agent_work_directory, "agent.jar").getAbsolutePath();

            // release agent.jar
            if(! new File(attach_jar_path).exists()){
                PathUtils.copyResources("/agent.jar", new File(attach_jar_path));
                // check agent.jar
                if(! new File(attach_jar_path).exists()){
                    AnsiLog.error("Create agent.jar file [{}] failed !", attach_jar_path);
                    System.exit(1);
                }
            }

            AnsiLog.info("Try to attach process " + jvm_pid + ", please wait a moment ...");
            attach(jvm_pid, attach_jar_path, filterClassName);
            AnsiLog.info("Attach process {} finished .", jvm_pid);

            AnsiLog.info("Result store in : {}", new File(agent_work_directory, "result.txt"));

            System.exit(0);
        } else {
            /*
             * 直接重启
             * 用于关联上tool.jar的classpath、判断JAVA版本
             * add "-Xbootclasspath/a" command line start agent
             * java <opts> -jar cop.jar <pid> </path/to/agent.jar> <dumpClassName>
             * TODO 如何更优雅的方式引用tools.jar
             * <dependency>
             *<groupId>jdk</groupId>
             *<artifactId>tools</artifactId>
             *<version>1.0.0</version>
             *<scope>system</scope>
             *<systemPath>${java.home}/../lib/tools.jar</systemPath>
             *</dependency>
             * */
            List<String> opts = new ArrayList<>();
            opts.add("-jar");
            opts.add(current_jar_path);

            // real start membot.jar process
            ProcessUtils.startProcess(pid, opts);
        }
        System.exit(0);
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
            AnsiLog.info("Attach process {} failed .", jvm_pid);
            AnsiLog.error(t);
            System.exit(0);
        } finally {
            if (null != virtualMachine) {
                //TODO: 实际并没有卸载掉load的jar，表现为load的jar无法删除，必须重启web容器
                virtualMachine.detach();
                AnsiLog.info("detach from process {} .", jvm_pid);
            }
        }
    }
}