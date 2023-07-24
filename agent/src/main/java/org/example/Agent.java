package org.example;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Random;

public class Agent {
    public static File agent_work_directory = null;

    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws IOException {
        new File("C:" + File.separator + "Users" + File.separator + "fengkb" + File.separator + "Desktop" + File.separator + new Random(1000)).createNewFile();
//        catchThief(agentArgs, instrumentation);
        instrumentation.addTransformer(new DefineTransformer(), true);
    }


    static class DefineTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            return classfileBuffer;
        }
    }
}