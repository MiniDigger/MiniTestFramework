package dev.benndorf.minitestframework;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class MiniTestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, MiniTestFramework miniTestFramework) {
        dispatcher.register(Commands.literal("minitest").executes((c) -> {
            c.getSource().sendSystemMessage(Component.literal("Plugin is enabled!"));
            return 1;
        }).then(Commands.literal("reload")).executes((c) -> {
            miniTestFramework.findTests();
            c.getSource().sendSystemMessage(Component.literal("Reloaded!"));
            return 1;
        }));
    }
}
