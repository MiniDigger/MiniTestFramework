package dev.benndorf.minitestframework;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

import dev.benndorf.minitestframework.TSGenerator.TSType;

public final class MiniTestFramework extends JavaPlugin {

    private Engine engine;
    private String currentFileName = "";

    @Override
    public void onLoad() {
        this.initJS();
        this.initGameTest();

        this.getDataFolder().mkdirs();
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        if (command.getName().equalsIgnoreCase("minitest")) {
            if (args.length == 0) {
                sender.sendMessage("Plugin is enabled!");
                return false;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                this.findTests();
                sender.sendMessage("Reloaded!");
            }
            return true;
        }
        return false;
    }

    private void initGameTest() {
        SharedConstants.IS_RUNNING_IN_IDE = true;

        final CommandDispatcher<CommandSourceStack> dispatcher = ((CraftServer) this.getServer()).getServer().vanillaCommandDispatcher.getDispatcher();
        TestCommand.register(dispatcher);
    }

    private void initJS() {
        class Loader extends URLClassLoader {

            public Loader(final ClassLoader parent) {
                super(new URL[0], parent);
            }

            @Override
            public void addURL(final URL location) {
                super.addURL(location);
            }
        }
        final Loader loader = new Loader(this.getClassLoader());
        loader.addURL(this.locate(this.getClass()));
        Thread.currentThread().setContextClassLoader(loader);

        this.engine = Engine.newBuilder().option("engine.WarnInterpreterOnly", "false").build();

        this.findTests();
    }

    private URL locate(final Class<?> clazz) {
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

    private void findTests() {
        GameTestRegistry.getAllTestClassNames().clear();
        GameTestRegistry.getAllTestFunctions().clear();

        try (final Stream<Path> list = Files.list(this.getDataFolder().toPath())) {
            list
                    .filter(p -> p.toString().endsWith(".js"))
                    .forEach(file -> {
                        try {
                            this.currentFileName = file.getFileName().toString().replace(".js", "");
                            this.execute(file);
                        } catch (final IOException ex) {
                            this.getLog4JLogger().error("Error while executing file {}", file.getFileName(), ex);
                        } finally {
                            this.currentFileName = null;
                        }
                    });
        } catch (final IOException ex) {
            this.getLog4JLogger().error("Error while scanning for files in {}", this.getDataFolder(), ex);
        }

        final String fileNames = String.join(", ", GameTestRegistry.getAllTestClassNames());
        final String testNames = GameTestRegistry.getAllTestFunctions().stream().map(TestFunction::getTestName).collect(Collectors.joining(", "));
        this.getLog4JLogger().info("Found tests [{}] in files [{}]", testNames, fileNames);
    }

    private Context createContext() {
        final Context context = Context.newBuilder("js")
                .engine(this.engine)
                .allowAllAccess(true)
                .allowExperimentalOptions(true)
                .option("js.nashorn-compat", "true")
                .option("js.commonjs-require", "true")
                .option("js.ecmascript-version", "2022")
                .option("js.commonjs-require-cwd", this.getDataFolder().getAbsolutePath())
                .build();
        final Value bindings = context.getBindings("js");
        bindings.putMember("registry", this);
        bindings.putMember("helper", GameTestHelper.class);
        bindings.putMember("Items", Items.class);
        bindings.putMember("EntityType", EntityTypeDelegate.class);
        bindings.putMember("BlockPos", BlockPos.class);
        return context;
    }

    private void execute(final Path file) throws IOException {
        this.createContext().eval(Source.newBuilder("js", file.toFile()).mimeType("application/javascript+module").build());
    }

    public void register(final String name, @TSType("(helper: GameTestHelper) => void") final Consumer<GameTestHelperDelegate> method) {
        this.register("defaultBatch", name, Rotation.NONE, 100, 0, true, method);
    }

    public void register(final String batchId, final String name, @TSType("(helper: GameTestHelper) => void") final Consumer<GameTestHelperDelegate> method) {
        this.register(batchId, name, Rotation.NONE, 100, 0, true, method);
    }

    public void register(final String batchId, final String name, final int tickLimit, final long duration, @TSType("(helper: GameTestHelper) => void") final Consumer<GameTestHelperDelegate> method) {
        this.register(batchId, name, Rotation.NONE, tickLimit, duration, true, method);
    }

    public void register(final String batchId, final String name, final Rotation rotation, final int tickLimit, final long duration, final boolean required, @TSType("(helper: GameTestHelper) => void") final Consumer<GameTestHelperDelegate> method) {
        this.register(batchId, name, rotation, tickLimit, duration, required, 1, 1, method);
    }

    public void register(final String batchId, String name, final Rotation rotation, final int tickLimit, final long duration, final boolean required, final int requiredSuccesses, final int maxAttempts, @TSType("(helper: GameTestHelper) => void") final Consumer<GameTestHelperDelegate> method) {
        final Consumer<GameTestHelper> wrapped = (helper) -> method.accept(new GameTestHelperDelegate(helper));
        name = this.currentFileName + "." + name;
        final TestFunction testFunction = new TestFunction(batchId, name, name, rotation, tickLimit, duration, required, requiredSuccesses, maxAttempts, wrapped);
        GameTestRegistry.getAllTestFunctions().add(testFunction);
        GameTestRegistry.getAllTestClassNames().add(this.currentFileName);
    }
}
