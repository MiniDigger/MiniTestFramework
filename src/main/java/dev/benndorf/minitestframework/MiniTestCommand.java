package dev.benndorf.minitestframework;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.command.VanillaCommandWrapper;

import javax.xml.parsers.ParserConfigurationException;

public class MiniTestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, MiniTestFramework miniTestFramework) {
        dispatcher.register(Commands.literal("minitest").executes((c) -> {
            c.getSource().sendSystemMessage(Component.literal("Plugin is enabled!"));
            return 1;
        }).then(Commands.literal("reload").executes((c) -> {
            String msg = miniTestFramework.findTests();
            c.getSource().sendSystemMessage(Component.literal("Reloaded! " + msg));
            return 1;
        })).then(Commands.literal("ci").executes((c) -> {
            try {
                miniTestFramework.runAllTests(VanillaCommandWrapper.getListener(Bukkit.getConsoleSender()), false);
                c.getSource().sendSystemMessage(Component.literal("Ran all tests!"));
                return 1;
            } catch (ParserConfigurationException e) {
                c.getSource().sendSystemMessage(Component.literal("Error while running tests").withStyle(ChatFormatting.RED));
                e.printStackTrace();
                return 1;
            }
        })).then(Commands.literal("ts").executes((c) -> {
            miniTestFramework.initTS();
            c.getSource().sendSystemMessage(Component.literal("Rewrote types!"));
            return 1;
        })));
    }
}
