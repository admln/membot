package org.example.utils;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.example.common.AnsiLog;
import org.example.common.Constant;
import org.example.common.ExecutingCommand;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 *
 * Referer https://github.com/alibaba/arthas/blob/master/boot/src/main/java/com/taobao/arthas/boot/ProcessUtils.java
 *
 */

public class ProcessUtils {
    private static String FOUND_JAVA_HOME = null;

    public static long select(boolean v, long telnetPortPid, String select) throws InputMismatchException {
        Map<Long, String> processMap = listProcessByJps(v);
        // Put the port that is already listening at the first
        if (telnetPortPid > 0 && processMap.containsKey(telnetPortPid)) {
            String telnetPortProcess = processMap.get(telnetPortPid);
            processMap.remove(telnetPortPid);
            Map<Long, String> newProcessMap = new LinkedHashMap<Long, String>();
            newProcessMap.put(telnetPortPid, telnetPortProcess);
            newProcessMap.putAll(processMap);
            processMap = newProcessMap;
        }

        if (processMap.isEmpty()) {
            AnsiLog.info("Can not find java process. Try to pass <pid> in command line.");
            return -1;
        }

        // select target process by the '--select' option when match only one process
        if (select != null && !select.trim().isEmpty()) {
            int matchedSelectCount = 0;
            Long matchedPid = null;
            for (Map.Entry<Long, String> entry : processMap.entrySet()) {
                if (entry.getValue().contains(select)) {
                    matchedSelectCount++;
                    matchedPid = entry.getKey();
                }
            }
            if (matchedSelectCount == 1) {
                return matchedPid;
            }
        }

        AnsiLog.info("Found existing java process, please choose one and hit RETURN.");
        // print list
        int count = 1;
        for (String process : processMap.values()) {
            if (count == 1) {
                System.out.println("* [" + count + "]: " + process);
            } else {
                System.out.println("  [" + count + "]: " + process);
            }
            count++;
        }

        // read choice
        String line = new Scanner(System.in).nextLine();
        if (line.trim().isEmpty()) {
            // get the first process id
            return processMap.keySet().iterator().next();
        }

        int choice = new Scanner(line).nextInt();

        if (choice <= 0 || choice > processMap.size()) {
            return -1;
        }

        Iterator<Long> idIter = processMap.keySet().iterator();
        for (int i = 1; i <= choice; ++i) {
            if (i == choice) {
                return idIter.next();
            }
            idIter.next();
        }

        return -1;
    }

    private static Map<Long, String> listProcessByJps(boolean v) {
        Map<Long, String> result = new LinkedHashMap<Long, String>();

        String jps = "jps";
        File jpsFile = findJps();
        if (jpsFile != null) {
            jps = jpsFile.getAbsolutePath();
        }

        AnsiLog.debug("Try use jps to lis java process, jps: " + jps);

        String[] command = null;
        if (v) {
            command = new String[] { jps, "-v", "-l" };
        } else {
            command = new String[] { jps, "-l" };
        }

        List<String> lines = ExecutingCommand.runNative(command);

        AnsiLog.debug("jps result: " + lines);

        long currentPid = Long.parseLong(PidUtils.currentPid());
        for (String line : lines) {
            String[] strings = line.trim().split("\\s+");
            if (strings.length < 1) {
                continue;
            }
            try {
                long pid = Long.parseLong(strings[0]);
                if (pid == currentPid) {
                    continue;
                }
                if (strings.length >= 2 && isJpsProcess(strings[1])) { // skip jps
                    continue;
                }

                result.put(pid, line);
            } catch (Throwable e) {
                // https://github.com/alibaba/arthas/issues/970
                // ignore
            }
        }

        return result;
    }

    private static File findJps() {
        // Try to find jps under java.home and System env JAVA_HOME
        String javaHome = System.getProperty("java.home");
        String[] paths = { "bin/jps", "bin/jps.exe", "../bin/jps", "../bin/jps.exe" };

        List<File> jpsList = new ArrayList<File>();
        for (String path : paths) {
            File jpsFile = new File(javaHome, path);
            if (jpsFile.exists()) {
                AnsiLog.debug("Found jps: " + jpsFile.getAbsolutePath());
                jpsList.add(jpsFile);
            }
        }

        if (jpsList.isEmpty()) {
            AnsiLog.debug("Can not find jps under :" + javaHome);
            String javaHomeEnv = System.getenv("JAVA_HOME");
            AnsiLog.debug("Try to find jps under env JAVA_HOME :" + javaHomeEnv);
            for (String path : paths) {
                File jpsFile = new File(javaHomeEnv, path);
                if (jpsFile.exists()) {
                    AnsiLog.debug("Found jps: " + jpsFile.getAbsolutePath());
                    jpsList.add(jpsFile);
                }
            }
        }

        if (jpsList.isEmpty()) {
            AnsiLog.debug("Can not find jps under current java home: " + javaHome);
            return null;
        }

        // find the shortest path, jre path longer than jdk path
        if (jpsList.size() > 1) {
            Collections.sort(jpsList, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    try {
                        return file1.getCanonicalPath().length() - file2.getCanonicalPath().length();
                    } catch (IOException e) {
                        // ignore
                    }
                    return -1;
                }
            });
        }
        return jpsList.get(0);
    }

    private static boolean isJpsProcess(String mainClassName) {
        return "sun.tools.jps.Jps".equals(mainClassName) || "jdk.jcmd/sun.tools.jps.Jps".equals(mainClassName);
    }

    /**
     * <pre>
     * 1. Try to find java home from System Property java.home
     * 2. If jdk > 8, FOUND_JAVA_HOME set to java.home
     * 3. If jdk <= 8, try to find tools.jar under java.home
     * 4. If tools.jar do not exists under java.home, try to find System env JAVA_HOME
     * 5. If jdk <= 8 and tools.jar do not exists under JAVA_HOME, throw IllegalArgumentException
     * </pre>
     *
     * @return
     */
    public static String findJavaHome() {
        if (FOUND_JAVA_HOME != null) {
            return FOUND_JAVA_HOME;
        }

        String javaHome = System.getProperty("java.home");

        if (JavaVersionUtils.isLessThanJava9()) {
            File toolsJar = new File(javaHome, "lib/tools.jar");
            if (!toolsJar.exists()) {
                toolsJar = new File(javaHome, "../lib/tools.jar");
            }
            if (!toolsJar.exists()) {
                // maybe jre
                toolsJar = new File(javaHome, "../../lib/tools.jar");
            }

            if (toolsJar.exists()) {
                FOUND_JAVA_HOME = javaHome;
                return FOUND_JAVA_HOME;
            }

            if (!toolsJar.exists()) {
                AnsiLog.debug("Can not find tools.jar under java.home: " + javaHome);
                String javaHomeEnv = System.getenv("JAVA_HOME");
                if (javaHomeEnv != null && !javaHomeEnv.isEmpty()) {
                    AnsiLog.debug("Try to find tools.jar in System Env JAVA_HOME: " + javaHomeEnv);
                    // $JAVA_HOME/lib/tools.jar
                    toolsJar = new File(javaHomeEnv, "lib/tools.jar");
                    if (!toolsJar.exists()) {
                        // maybe jre
                        toolsJar = new File(javaHomeEnv, "../lib/tools.jar");
                    }
                }

                if (toolsJar.exists()) {
                    AnsiLog.info("Found java home from System Env JAVA_HOME: " + javaHomeEnv);
                    FOUND_JAVA_HOME = javaHomeEnv;
                    return FOUND_JAVA_HOME;
                }

                throw new IllegalArgumentException("Can not find tools.jar under java home: " + javaHome
                        + ", please try to start arthas-boot with full path java. Such as /opt/jdk/bin/java -jar arthas-boot.jar");
            }
        } else {
            FOUND_JAVA_HOME = javaHome;
        }
        return FOUND_JAVA_HOME;
    }

    private static File findJava() {
        String javaHome = findJavaHome();
        String[] paths = { "bin/java", "bin/java.exe", "../bin/java", "../bin/java.exe" };

        List<File> javaList = new ArrayList<File>();
        for (String path : paths) {
            File javaFile = new File(javaHome, path);
            if (javaFile.exists()) {
                AnsiLog.debug("Found java: " + javaFile.getAbsolutePath());
                javaList.add(javaFile);
            }
        }

        if (javaList.isEmpty()) {
            AnsiLog.debug("Can not find java/java.exe under current java home: " + javaHome);
            return null;
        }

        // find the shortest path, jre path longer than jdk path
        if (javaList.size() > 1) {
            Collections.sort(javaList, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    try {
                        return file1.getCanonicalPath().length() - file2.getCanonicalPath().length();
                    } catch (IOException e) {
                        // ignore
                    }
                    return -1;
                }
            });
        }
        return javaList.get(0);
    }

    private static File findToolsJar() {
        if (JavaVersionUtils.isGreaterThanJava8()) {
            return null;
        }

        String javaHome = findJavaHome();
        File toolsJar = new File(javaHome, "lib/tools.jar");
        if (!toolsJar.exists()) {
            toolsJar = new File(javaHome, "../lib/tools.jar");
        }
        if (!toolsJar.exists()) {
            // maybe jre
            toolsJar = new File(javaHome, "../../lib/tools.jar");
        }

        if (!toolsJar.exists()) {
            throw new IllegalArgumentException("Can not find tools.jar under java home: " + javaHome);
        }

        AnsiLog.debug("Found tools.jar: " + toolsJar.getAbsolutePath());
        return toolsJar;
    }

    public static void startProcess(long targetPid, List<String> attachArgs) {
        // find java/java.exe, then try to find tools.jar
        String javaHome = findJavaHome();

        // find java/java.exe
        File javaPath = findJava();
        if (javaPath == null) {
            throw new IllegalArgumentException(
                    "Can not find java/java.exe executable file under java home: " + javaHome);
        }

        File toolsJar = findToolsJar();

        if (JavaVersionUtils.isLessThanJava9()) {
            if (toolsJar == null || !toolsJar.exists()) {
                throw new IllegalArgumentException("Can not find tools.jar under java home: " + javaHome);
            }
        }

        List<String> command = new ArrayList<String>();
        command.add(javaPath.getAbsolutePath());

        if (toolsJar != null && toolsJar.exists()) {
            // solve tools.jar com.sun.tools.attach.VirtualMachine Class not found exception
            command.add("-Xbootclasspath/a:" + toolsJar.getAbsolutePath());
        }

        command.addAll(attachArgs);

        if (!JavaVersionUtils.isLessThanJava9())
            command.add("greater_than_jdk9_flag");
        if (toolsJar != null && toolsJar.exists())
            command.add("bootstart_flag");

        ProcessBuilder pb = new ProcessBuilder(command);

        try {
            final Process proc = pb.start();
            Thread redirectStdout = new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream inputStream = proc.getInputStream();
                    try {
                        IOUtils.copy(inputStream, System.out);
                    } catch (IOException e) {
                        IOUtils.close(inputStream);
                    }

                }
            });

            Thread redirectStderr = new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream inputStream = proc.getErrorStream();
                    try {
                        IOUtils.copy(inputStream, System.err);
                    } catch (IOException e) {
                        IOUtils.close(inputStream);
                    }

                }
            });
            redirectStdout.start();
            redirectStderr.start();
            redirectStdout.join();
            redirectStderr.join();

            int exitValue = proc.exitValue();
            if (exitValue != 0) {
                AnsiLog.error("attach fail, targetPid: " + targetPid);
                System.exit(1);
            }
        } catch (Throwable e) {
            // ignore
        }
    }


    /**
     * 列出所有的JAVA PID
     * TODO：未处理多web容器的情况，让选择？默认两个都注入？
     */
    public static String get_java_pid() {

        List<VirtualMachineDescriptor> vml = VirtualMachine.list();

        for (VirtualMachineDescriptor vmd: vml) {
            System.out.println(vmd.displayName());
            for (String key:
                    Constant.WEB_CONTAINER_MAP.keySet()) {
                if(vmd.displayName().contains(Constant.WEB_CONTAINER_MAP.get(key))) {
                    AnsiLog.info("Find a web container for " + key + " : " + vmd.id() + " - " + vmd.displayName());
                    return vmd.id();
                }
            }
        }

        AnsiLog.error("No web container find");
        System.exit(-1);
        return "-1";
    }

}