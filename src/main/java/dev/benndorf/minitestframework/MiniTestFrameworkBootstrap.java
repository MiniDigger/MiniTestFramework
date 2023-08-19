package dev.benndorf.minitestframework;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.TestClassNameArgument;
import net.minecraft.gametest.framework.TestFunctionArgument;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("UnstableApiUsage")
public class MiniTestFrameworkBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        // set to true so that the test command is registered and stuff
        SharedConstants.IS_RUNNING_IN_IDE = true;

        // bootstrap isn't early enough, argument types are still registered before this method is called, so we need to do it manually
        for (Method method : ArgumentTypeInfos.class.getDeclaredMethods()) {
            if (method.getName().equals("register")) {
                try {
                    method.setAccessible(true);
                    method.invoke(null, BuiltInRegistries.COMMAND_ARGUMENT_TYPE, "test_argument", TestFunctionArgument.class, SingletonArgumentInfo.contextFree(TestFunctionArgument::testFunctionArgument));
                    method.invoke(null, BuiltInRegistries.COMMAND_ARGUMENT_TYPE, "test_class", TestClassNameArgument.class, SingletonArgumentInfo.contextFree(TestClassNameArgument::testClassName));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Couldn't register gametest arguemnt types", e);
                }
            }
        }
    }
}
