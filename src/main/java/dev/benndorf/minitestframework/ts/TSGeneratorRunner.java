package dev.benndorf.minitestframework.ts;

import dev.benndorf.minitestframework.MiniTestFramework;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class TSGeneratorRunner {

    public static void run(final Path folder) throws IOException {
        final Path definitionFile = folder.resolve("index.d.ts");

        List<Class<?>> simple = List.of(EntityType.class, Items.class, Item.class, Blocks.class, Block.class, BlockPos.class);
        for (Class<?> aClass : simple) {
            TSGenerator.mappings.put(aClass, aClass.getSimpleName());
        }

        StringBuilder sb = new StringBuilder();
        for (Class<?> aClass : simple) {
            sb.append(TSGenerator.generateClazz(null, aClass));
        }
        sb.append(TSGenerator.generateClazz("helper", GameTestHelper.class));
        sb.append(TSGenerator.generateClazz("registry", MiniTestFramework.class));

        Files.writeString(definitionFile, sb, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        final Path moduleFile = folder.resolve("index.mjs");
        sb = new StringBuilder();
        for (Class<?> aClass : simple) {
            sb.append("export const ").append(aClass.getSimpleName()).append(" = Java.type('").append(aClass.getName()).append("');\n");
        }
        sb.append("export const registry = Polyglot.import('registry');\n");
        sb.append("export const helper = Polyglot.import('helper');\n");

        Files.writeString(moduleFile, sb, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }
}
