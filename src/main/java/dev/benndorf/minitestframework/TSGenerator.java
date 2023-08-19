package dev.benndorf.minitestframework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class TSGenerator {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface TSType {
        String value();
    }

    public static String generate(final String alias, final Class<?> clazz) {
        final StringBuilder sb = new StringBuilder();
        sb.append("export const ").append(alias).append(": ").append(cleanName(clazz.getSimpleName())).append("\n");
        sb.append("export class ").append(cleanName(clazz.getSimpleName())).append(" {").append("\n");
        for (final Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isPublic(field.getModifiers())) continue;
            if (Modifier.isStatic(field.getModifiers())) {
                sb.append("  public static ").append(cleanName(field.getName())).append(": any;").append("\n");
            } else {
                sb.append("  ").append(cleanName(field.getName())).append(": any;").append("\n");
            }
        }
        for (final Method method : clazz.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) continue;
            sb.append("  ").append(method.getName()).append("(");
            String prefix = "";
            for (final Parameter parameter : method.getParameters()) {
                sb.append(prefix);
                prefix = ",";
                sb.append(cleanName(parameter.getName()));
                final String type = type(parameter.getType(), parameter.getAnnotation(TSType.class));
                sb.append(type == null ? "" : ": " + type);
            }
            sb.append(");").append("\n");
        }
        sb.append("}").append("\n");
        return sb.toString();
    }

    private static final Map<Class<?>, String> mappings = new HashMap<>();

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
