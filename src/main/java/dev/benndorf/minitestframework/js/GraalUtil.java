package dev.benndorf.minitestframework.js;

import dev.benndorf.minitestframework.MiniTestFramework;
import net.minecraft.gametest.framework.GameTestHelper;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class GraalUtil {

    public static Engine createEngine() {

        final Loader loader = new Loader(MiniTestFramework.class.getClassLoader());
        loader.addURL(locate(MiniTestFramework.class));
        Thread.currentThread().setContextClassLoader(loader);

        return Engine.newBuilder().option("engine.WarnInterpreterOnly", "false").build();
    }

    public static Context createContext(Engine engine, MiniTestFramework framework) {
        final Context context = Context.newBuilder("js")
                .engine(engine)
                .allowAllAccess(true)
                .allowExperimentalOptions(true)
                .option("js.nashorn-compat", "true")
                .option("js.commonjs-require", "true")
                .option("js.ecmascript-version", "2022")
                .option("js.commonjs-require-cwd", framework.getDataFolder().getAbsolutePath())
                .build();
        final Value bindings = context.getPolyglotBindings();
        bindings.putMember("registry", framework);
        bindings.putMember("helper", GameTestHelper.class);
        return context;
    }

    private static URL locate(final Class<?> clazz) {
        try {
            final URL resource = clazz.getProtectionDomain().getCodeSource().getLocation();
            if (resource != null) return resource;
        } catch (final SecurityException | NullPointerException error) {
            // do nothing
        }
        final URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
        if (resource != null) {
            final String link = resource.toString();
            final String suffix = clazz.getCanonicalName().replace('.', '/') + ".class";
            if (link.endsWith(suffix)) {
                String path = link.substring(0, link.length() - suffix.length());
                if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);
                try {
                    return new URL(path);
                } catch (final Exception error) {
                    // do nothing
                }
            }
        }
        return null;
    }

    public static void execute(Engine engine, MiniTestFramework framework, final Path file) throws IOException {
        createContext(engine, framework).eval(Source.newBuilder("js", file.toFile()).mimeType("application/javascript+module").build());
    }

    static class Loader extends URLClassLoader {

        public Loader(final ClassLoader parent) {
            super(new URL[0], parent);
        }

        @Override
        public void addURL(final URL location) {
            super.addURL(location);
        }
    }
}
