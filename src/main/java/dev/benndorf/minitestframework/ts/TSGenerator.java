package dev.benndorf.minitestframework.ts;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TSGenerator {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface TSType {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface TSHide {

    }

    public static String generateClazz(final String alias, final Class<?> clazz) {
        final StringBuilder sb = new StringBuilder();

        if (alias != null) {
            sb.append("export const ").append(alias).append(": ").append(cleanName(clazz.getSimpleName())).append("\n");
        }
        sb.append("export class ").append(cleanName(clazz.getSimpleName())).append(" {").append("\n");

        Arrays.stream(clazz.getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .forEach(f -> generateField(f, sb));

        Arrays.stream(clazz.getDeclaredMethods())
                .sorted(Comparator.comparing(Method::getName).thenComparing(Method::getParameterCount))
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .forEach(m -> generateMethod(m, sb));

        sb.append("}").append("\n");
        return sb.toString();
    }

    private static void generateField(Field field, StringBuilder sb) {
        sb.append("  ");
        if (Modifier.isStatic(field.getModifiers())) {
            sb.append("public static ");
        }
        final String type = type(field.getType(), null);
        sb.append(cleanName(field.getName())).append(": ").append(type == null ? "any" : type).append(";").append("\n");
    }

    private static void generateMethod(Method method, StringBuilder sb) {
        if (method.getAnnotation(TSHide.class) != null) return;

        sb.append("  ").append(method.getName()).append("(");
        String prefix = "";
        for (final Parameter parameter : method.getParameters()) {
            sb.append(prefix);
            prefix = ",";
            sb.append(cleanName(parameter.getName()));
            final String type = type(parameter.getType(), parameter.getAnnotation(TSType.class));
            sb.append(type == null ? "" : ": " + type);
        }
        sb.append(")");
        final String type = type(method.getReturnType(), null);
        sb.append(type == null ? "" : ": " + type);
        sb.append(";\n");
    }

    public static final Map<Class<?>, String> mappings = new HashMap<>();

    static {
        mappings.put(String.class, "string");
        mappings.put(Long.class, "number");
        mappings.put(long.class, "number");
        mappings.put(Integer.class, "number");
        mappings.put(int.class, "number");
        mappings.put(Double.class, "number");
        mappings.put(double.class, "number");
        mappings.put(Float.class, "number");
        mappings.put(float.class, "number");
        mappings.put(Boolean.class, "boolean");
        mappings.put(boolean.class, "boolean");
        mappings.put(Void.class, "void");
        mappings.put(void.class, "void");
    }

    private static String type(final Class<?> clazz, final TSType typeInfo) {
        if (typeInfo != null && typeInfo.value() != null) {
            return typeInfo.value();
        }
        return mappings.getOrDefault(clazz, null);
    }

    private static String cleanName(final String name) {
        if (name.equals("function") || name.equals("object")) {
            return name + "P";
        }
        return name;
    }
}
