package com.demo;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Orchestrator {

    private final Map<Class<?>, Object> container = new HashMap<>();
    private final List<Class<?>> scanned;

    public Orchestrator(String basePackage) throws Exception {
        this.scanned = scan(basePackage);
    }

    /** Instantiates and wires every class that has at least one @Inject field. */
    public void run() throws Exception {
        for (Class<?> c : scanned) {
            if (!injectableFields(c).isEmpty()) {
                getBean(c);
            }
        }
    }

    /** Returns the singleton for this type, creating and wiring it on first request. */
    public <T> T getBean(Class<T> type) throws Exception {
        Object existing = container.get(type);
        if (existing != null) return type.cast(existing);

        Class<?> impl = resolveImplementation(type);
        existing = container.get(impl);
        if (existing != null) return type.cast(existing);

        Constructor<?> ctor = impl.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object bean = ctor.newInstance();

        // Register BEFORE injecting fields: makes circular dependencies safe
        container.put(impl, bean);
        container.put(type, bean);

        for (Field field : injectableFields(impl)) {
            Object dependency = getBean(field.getType());
            inject(bean, field, dependency);
            System.out.println("[Orchestrator] " + field.getType().getSimpleName()
                    + " -> " + impl.getSimpleName() + "." + field.getName());
        }
        return type.cast(bean);
    }

    private void inject(Object host, Field field, Object dependency) throws Exception {
        String name = field.getName();
        String setter = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            Method m = host.getClass().getMethod(setter, field.getType());
            m.invoke(host, dependency);
        } catch (NoSuchMethodException e) {
            field.setAccessible(true);
            field.set(host, dependency);
        }
    }

    private List<Field> injectableFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.isAnnotationPresent(Inject.class)) fields.add(f);
            }
        }
        return fields;
    }

    private Class<?> resolveImplementation(Class<?> type) {
        if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) return type;

        List<Class<?>> candidates = scanned.stream()
                .filter(c -> type.isAssignableFrom(c))
                .filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()))
                .collect(Collectors.toList());

        if (candidates.isEmpty())
            throw new IllegalStateException("No implementation found for " + type.getName());
        if (candidates.size() > 1)
            throw new IllegalStateException("Ambiguous implementations for " + type.getName() + ": " + candidates);
        return candidates.get(0);
    }

    private List<Class<?>> scan(String basePackage) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String path = basePackage.replace('.', '/');
        List<Class<?>> result = new ArrayList<>();

        Enumeration<URL> roots = cl.getResources(path);
        while (roots.hasMoreElements()) {
            URL url = roots.nextElement();
            if (!"file".equals(url.getProtocol())) continue;

            Path root = Paths.get(url.toURI());
            List<Path> classFiles;
            try (Stream<Path> s = Files.walk(root)) {
                classFiles = s.filter(p -> p.toString().endsWith(".class")).collect(Collectors.toList());
            }

            for (Path p : classFiles) {
                String relative = root.relativize(p).toString().replace(File.separatorChar, '.');
                relative = relative.substring(0, relative.length() - ".class".length());
                if (relative.contains("$") || relative.equals("module-info")) continue;

                String className = basePackage.isEmpty() ? relative : basePackage + "." + relative;
                result.add(Class.forName(className, false, cl));
            }
        }
        return result;
    }
}
