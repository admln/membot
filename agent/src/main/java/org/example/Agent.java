package org.example;

import org.example.common.Constant;
import org.example.utils.ClassUtils;
import org.example.utils.LogUtils;
import org.example.utils.PathUtils;
import org.example.utils.SearchUtils;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Agent {
    public static File agent_work_directory = null;

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        catchThief(agentArgs, instrumentation);
        instrumentation.addTransformer(new DefineTransformer(), true);
    }


    static class DefineTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            return classfileBuffer;
        }
    }

    private static synchronized void catchThief(String name, Instrumentation ins) {
        LogUtils.logit("Agent.jar is success attached");
        LogUtils.logit("Current Agent.jar Directory : " + PathUtils.getCurrentDirectory());
        LogUtils.logit("Prepared Dump class name    : " + name);

        List<Class<?>> resultClasses = new ArrayList<>();

        // 获得所有已加载的类及类名
        Class<?>[] loadedClasses = ins.getAllLoadedClasses();
        LogUtils.logit("Found All Loaded Classes    : " + loadedClasses.length);
        List<String> loadedClassesNames = new ArrayList<>();
        for (Class<?> cls : loadedClasses) {
            loadedClassesNames.add(cls.getName());
        }

        // 单独保存已加载的类名及 hashcode、classloader、classloader hashcode 信息
        agent_work_directory = new File(PathUtils.getCurrentDirectory());
        File allLoadedClassFile = new File(new File(agent_work_directory, "logs"), "allLoadedClasses.txt");
        LogUtils.logit("Prepared Store All Loaded Classes Name ...");
        PathUtils.appendTextToFile(allLoadedClassFile, "[*] Format: [classname | class-hashcode | classloader | classloader-hashcode]\n");
        ClassUtils.storeAllLoadedClassesName(allLoadedClassFile, loadedClasses);
        LogUtils.logit("All Loaded Classes Names Store in : " + allLoadedClassFile.getAbsolutePath());

        if (name.equals("[unknown]")) { // 默认没有指定具体过滤类名的流程
            for (Class<?> clazz : loadedClasses) {

                //前置-筛选可疑包名
                for (String packageName : Constant.SUS_PACKAGES) {
                    if (clazz.getName().contains(packageName)) {
                        resultClasses.add(clazz);
                        LogUtils.logit("[!] find suspicious class by package name: [" + clazz.getName() + "]  class hashcode: [" + Integer.toHexString(clazz.hashCode()) + "]  ClassLoader: [" + clazz.getClassLoader().getClass().getName() + "]  ClassLoader hashcode: [" + Integer.toHexString(clazz.getClassLoader().hashCode()) + "]\n");
                        break;
                    }
                }

                //前置-筛选可疑注解
                if (ClassUtils.isUseAnnotations(clazz, Constant.SUS_ANNOTATIONS)) {
                    resultClasses.add(clazz);
                    LogUtils.logit("[!] find suspicious class by annotation: [" + clazz.getName() + "]  class hashcode: [" + Integer.toHexString(clazz.hashCode()) + "]  ClassLoader: [" + clazz.getClassLoader().getClass().getName() + "]  ClassLoader hashcode: [" + Integer.toHexString(clazz.getClassLoader().hashCode()) + "]\n");
                    continue;
                }

                //前置-筛选可疑父类
                if (clazz.getSuperclass() != null && Constant.SUS_SUPER_CLASSNAMES.contains(clazz.getSuperclass().getName())) {
                    if(loadedClassesNames.contains(clazz.getName()) && clazz.getClassLoader() != null){
                        resultClasses.add(clazz);
                        LogUtils.logit("[!] find suspicious class by super class name: [" + clazz.getName() + "]  class hashcode: [" + Integer.toHexString(clazz.hashCode()) + "]  ClassLoader: [" + clazz.getClassLoader().getClass().getName() + "]  ClassLoader hashcode: [" + Integer.toHexString(clazz.getClassLoader().hashCode()) + "]\n");
                    }else{
                        LogUtils.logit("cannot find " + clazz.getName() + " classes in instrumentation");
                    }
                    continue;
                }

                //前置-筛选可疑接口
                java.util.List<String> interfaces = new ArrayList<>();
                for (Class<?> cls : clazz.getInterfaces()) {
                    interfaces.add(cls.getName());
                }
                for (String sus_interface_str: Constant.SUS_INTERFACES) {
                    for (String interface_str: interfaces) {
                        if (interface_str.contains(sus_interface_str)) {
                            resultClasses.add(clazz);
                            LogUtils.logit("[!] find suspicious class by interface name: [" + clazz.getName() + "]  class hashcode: [" + Integer.toHexString(clazz.hashCode()) + "]  ClassLoader: [" + clazz.getClassLoader().getClass().getName() + "]  ClassLoader hashcode: [" + Integer.toHexString(clazz.getClassLoader().hashCode()) + "]\n");
                            break;
                        }
                    }
                }
            }
        } else { //如果只是要dump特定的类
            if (loadedClassesNames.contains(name)) {
                Class<?> clazz = ClassUtils.dumpClass(ins, name, false, null);
                resultClasses.add(clazz);
            } else if (name.contains("*")) {
                Set<Class<?>> findClasses = SearchUtils.searchClass(ins, name, true, null);
                while (findClasses.iterator().hasNext()) {
                    Class<?> clazz = findClasses.iterator().next();
                    resultClasses.add(clazz);
                    ClassUtils.dumpClass(ins, clazz.getName(), false, null);
                }
            } else {
                LogUtils.logit("class name [" + name + "] not found in loaded classes");
            }
        }

        List<Class<?>> riskClasses = new ArrayList<>();
        // 后置-检测风险代码内容
        for (Class<?> clazz : resultClasses) {
            // TODO:这里优化，把获取clazz明文的方式变成类似jad直接内存中反编译，然后将dumpClass方法后移，会省时
            ClassUtils.dumpClass(ins, clazz.getName(), false, Integer.toHexString(clazz.getClassLoader().hashCode()));
            File dumpPath = PathUtils.getStorePath(clazz, false);
            String content = PathUtils.getFileContent(dumpPath);
            for (String keyword : Constant.RISK_KEYWORD) {
                if (content.contains(keyword)) {
                    riskClasses.add(clazz);
                    LogUtils.logit("[!] find risk class by keyword: [" + clazz.getName() + "]  class hashcode: [" + Integer.toHexString(clazz.hashCode()) + "]  ClassLoader: [" + clazz.getClassLoader().getClass().getName() + "]  ClassLoader hashcode: [" + Integer.toHexString(clazz.getClassLoader().hashCode()) + "]\n");
                    break;
                }
            }
        }

        // 结果输出
        StringBuilder results = new StringBuilder("All Risk Class    : " + riskClasses.size() + "\n\n");
        int order = 1;
        for (Class<?> clazz : riskClasses) {
            File dumpPath = PathUtils.getStorePath(clazz, false);

            // 拼接日志字符串
            String tmp = "";
            try{
                tmp = String.format("order       : %d\nname        : %s\nlocation    : %s\nhashcode    : %s\nclassloader : %s\nextends     : %s\n\n", order, clazz.getName(), dumpPath.getAbsolutePath(), Integer.toHexString(clazz.hashCode()), clazz.getClassLoader().getClass().getName(), clazz.getSuperclass().getName());
            }catch (NullPointerException e){
                tmp = String.format("order       : %d\nname        : %s\nlocation    : %s\nhashcode    : %s\nclassloader : %s\nextends     : [NullPointerException]\n\n", order, clazz.getName(), dumpPath.getAbsolutePath(), Integer.toHexString(clazz.hashCode()), clazz.getClassLoader().getClass().getName());
            }
            results.append(tmp);
            order ++;
        }
        LogUtils.logit(results.toString());
        LogUtils.result(results.toString());
    }
}