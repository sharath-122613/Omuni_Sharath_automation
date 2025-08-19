package com.cloud.omuni_cloud.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Set;

public class ModuleAccessAgent {
    public static void premain(String args, Instrumentation inst) {
        // Open all JDK modules to unnamed modules
        ModuleLayer.boot().modules().forEach(module -> {
            if (module.isNamed()) {
                Set<String> packages = module.getPackages();
                if (packages != null) {
                    packages.forEach(pkg -> {
                        try {
                            // Use reflection to access the internal addOpens method
                            Method addOpens = Module.class.getDeclaredMethod("implAddOpens", String.class, Module.class);
                            addOpens.setAccessible(true);
                            addOpens.invoke(module, pkg, ModuleAccessAgent.class.getModule());
                        } catch (Exception e) {
                            // Ignore any errors
                        }
                    });
                }
            }
        });
    }
}
