package dev.benndorf.minitestframework;

import net.minecraft.gametest.framework.GameTestHelper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class TSGeneratorTest {
    @Test
    public void test() throws IOException {
        final Path file = Path.of("D:\\IntellijProjects\\PaperGameTest\\run\\plugins\\MiniTestFramework\\node_modules\\helper.d.ts");
        final String sb =
                TSGenerator.generate("EntityType", EntityTypeDelegate.class) +
                TSGenerator.generate("helper", GameTestHelper.class) +
                TSGenerator.generate("registry", MiniTestFramework.class);
        Files.writeString(file, sb, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
