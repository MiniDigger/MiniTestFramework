package dev.benndorf.minitestframework;

import com.mojang.brigadier.CommandDispatcher;
import dev.benndorf.minitestframework.TSGenerator.TSType;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.command.VanillaCommandWrapper;
import org.bukkit.plugin.java.JavaPlugin;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MiniTestFramework extends JavaPlugin {

    private Engine engine;
    private String currentFileName = "";

    @Override
    public void onLoad() {
        this.initJS();
        this.registerCommand();

        this.getDataFolder().mkdirs();
    }

    @Override
    public void onEnable() {
        if ("true".equals(System.getenv("CI"))) {
            try {
                this.runAllTests(VanillaCommandWrapper.getListener(Bukkit.getConsoleSender()));
            } catch (final Exception ex) {
                this.getSLF4JLogger().error("Error while running tests", ex);
            }
        }
    }

    private void runAllTests(final CommandSourceStack source) throws ParserConfigurationException {
        GameTestRunner.clearMarkers(source.getLevel());
        final Collection<TestFunction> testFunctions = GameTestRegistry.getAllTestFunctions();
        source.sendSuccess(() -> Component.literal("Running all " + testFunctions.size() + " tests..."), false);
        final BlockPos sourcePos = new BlockPos((int) source.getPosition().x(), (int) source.getPosition().y(), (int) source.getPosition().z());
        final BlockPos startPos = new BlockPos(sourcePos.getX(), source.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, sourcePos).getY(), sourcePos.getZ() + 3);
        final Collection<GameTestInfo> collection = GameTestRunner.runTests(testFunctions, startPos, StructureUtils.getRotationForRotationSteps(0), source.getLevel(), GameTestTicker.SINGLETON, 8);
        GlobalTestReporter.replaceWith(new TestReporter(new File( "test-results.xml"), new MultipleTestTracker(collection)));
    }

    private void registerCommand() {
        final CommandDispatcher<CommandSourceStack> dispatcher = ((CraftServer) this.getServer()).getServer().vanillaCommandDispatcher.getDispatcher();
        MiniTestCommand.register(dispatcher, this);
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

    public void findTests() {
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
                            this.getSLF4JLogger().error("Error while executing file {}", file.getFileName(), ex);
                        } finally {
                            this.currentFileName = null;
                        }
                    });
        } catch (final IOException ex) {
            this.getSLF4JLogger().error("Error while scanning for files in {}", this.getDataFolder(), ex);
        }

        final String fileNames = String.join(", ", GameTestRegistry.getAllTestClassNames());
        final String testNames = GameTestRegistry.getAllTestFunctions().stream().map(TestFunction::getTestName).collect(Collectors.joining(", "));
        this.getSLF4JLogger().info("Found tests [{}] in files [{}]", testNames, fileNames);
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
