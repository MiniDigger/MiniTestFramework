package dev.benndorf.minitestframework;

import com.mojang.brigadier.CommandDispatcher;
import dev.benndorf.minitestframework.ci.TestReporter;
import dev.benndorf.minitestframework.js.GraalUtil;
import dev.benndorf.minitestframework.ts.TSGenerator.TSType;
import dev.benndorf.minitestframework.ts.TSGeneratorRunner;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.command.VanillaCommandWrapper;
import org.bukkit.plugin.java.JavaPlugin;
import org.graalvm.polyglot.Engine;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.benndorf.minitestframework.ts.TSGenerator.*;

public final class MiniTestFramework extends JavaPlugin {

    private Engine engine;
    private String currentFileName = "";

    @TSHide
    @Override
    public void onLoad() {
        this.getDataFolder().mkdirs();

        this.initJS();
        this.initTS();

        this.registerCommand();
    }

    private void registerCommand() {
        final CommandDispatcher<CommandSourceStack> dispatcher = ((CraftServer) this.getServer()).getServer().vanillaCommandDispatcher.getDispatcher();
        MiniTestCommand.register(dispatcher, this);
    }

    private void initJS() {
        this.engine = GraalUtil.createEngine();
        this.getSLF4JLogger().info(this.findTests());
    }

    @TSHide
    public void initTS() {
        try {
            TSGeneratorRunner.run(getDataFolder().toPath().resolve("minitestframework"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @TSHide
    @Override
    public void onEnable() {
        if ("true".equals(System.getenv("CI"))) {
            try {
                this.runAllTests(VanillaCommandWrapper.getListener(Bukkit.getConsoleSender()), true);
            } catch (final Exception ex) {
                this.getSLF4JLogger().error("Error while running tests", ex);
            }
        }
    }

    @TSHide
    public void runAllTests(final CommandSourceStack source, final boolean stopServer) throws ParserConfigurationException {
        GameTestRunner.clearMarkers(source.getLevel());
        final Collection<TestFunction> testFunctions = GameTestRegistry.getAllTestFunctions();
        source.sendSuccess(() -> Component.literal("Running all " + testFunctions.size() + " tests..."), false);
        final BlockPos sourcePos = new BlockPos((int) source.getPosition().x(), (int) source.getPosition().y(), (int) source.getPosition().z());
        final BlockPos startPos = new BlockPos(sourcePos.getX(), source.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, sourcePos).getY(), sourcePos.getZ() + 3);
        final Collection<GameTestInfo> collection = GameTestRunner.runTests(testFunctions, startPos, StructureUtils.getRotationForRotationSteps(0), source.getLevel(), GameTestTicker.SINGLETON, 8);
        GlobalTestReporter.replaceWith(new TestReporter(new File( "test-results.xml"), new MultipleTestTracker(collection), stopServer));
    }

    @TSHide
    public String findTests() {
        GameTestRegistry.getAllTestClassNames().clear();
        GameTestRegistry.getAllTestFunctions().clear();

        try (final Stream<Path> list = Files.list(this.getDataFolder().toPath())) {
            list
                    .filter(p -> p.toString().endsWith(".js"))
                    .forEach(file -> {
                        try {
                            this.currentFileName = file.getFileName().toString().replace(".js", "");
                            GraalUtil.execute(engine, this, file);
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
        return String.format("Found tests %s in files %s", testNames, fileNames);
    }

    public void register(final String name, @TSType("(helper: GameTestHelper) => void") final Consumer<GameTestHelper> method) {
        this.register("defaultBatch", name, Rotation.NONE, 100, 0, true, method);
    }

    public void register(final String batchId, final String name, @TSType("(helper: GameTestHelper) => void") final Consumer<GameTestHelper> method) {
        this.register(batchId, name, Rotation.NONE, 100, 0, true, method);
    }

    public void register(final String batchId, final String name, final int tickLimit, final long duration, @TSType("(helper: GameTestHelper) => void") final Consumer<GameTestHelper> method) {
        this.register(batchId, name, Rotation.NONE, tickLimit, duration, true, method);
    }

    public void register(final String batchId, final String name, final Rotation rotation, final int tickLimit, final long duration, final boolean required, @TSType("(helper: GameTestHelper) => void") final Consumer<GameTestHelper> method) {
        this.register(batchId, name, rotation, tickLimit, duration, required, 1, 1, method);
    }

    public void register(final String batchId, String name, final Rotation rotation, final int tickLimit, final long duration, final boolean required, final int requiredSuccesses, final int maxAttempts, @TSType("(helper: GameTestHelper) => void") final Consumer<GameTestHelper> method) {
        name = this.currentFileName + "." + name;
        final TestFunction testFunction = new TestFunction(batchId, name, name, rotation, tickLimit, duration, required, requiredSuccesses, maxAttempts, method);
        GameTestRegistry.getAllTestFunctions().add(testFunction);
        GameTestRegistry.getAllTestClassNames().add(this.currentFileName);
    }
}
