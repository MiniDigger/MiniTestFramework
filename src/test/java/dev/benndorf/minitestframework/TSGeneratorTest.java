package dev.benndorf.minitestframework;

import dev.benndorf.minitestframework.ts.TSGeneratorRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

class TSGeneratorTest {
    @Test
    public void test() throws IOException {
        TSGeneratorRunner.run(Path.of("run/plugins/MiniTestFramework/minitestframework"));
    }
}
