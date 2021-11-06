package dev.benndorf.minitestframework;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class GameTestHelperDelegate {

    private final GameTestHelper delegate;

    public GameTestHelperDelegate(final GameTestHelper delegate) {
        this.delegate = delegate;
    }

    public ServerLevel getLevel() {
        return this.delegate.getLevel();
    }

    public BlockState getBlockState(final BlockPos pos) {
        return this.delegate.getBlockState(pos);
    }

    @Nullable
    public BlockEntity getBlockEntity(final BlockPos pos) {
        return this.delegate.getBlockEntity(pos);
    }

    public void killAllEntities() {
        this.delegate.killAllEntities();
    }

    public ItemEntity spawnItem(final Item item, final float x, final float y, final float z) {
        return this.delegate.spawnItem(item, x, y, z);
    }

    public <E extends Entity> E spawn(final EntityType<E> type, final BlockPos pos) {
        return this.delegate.spawn(type, pos);
    }

    public <E extends Entity> E spawn(final EntityType<E> type, final Vec3 pos) {
        return this.delegate.spawn(type, pos);
    }

    public <E extends Entity> E spawn(final EntityType<E> type, final int x, final int y, final int z) {
        return this.delegate.spawn(type, x, y, z);
    }

    public <E extends Entity> E spawn(final EntityType<E> type, final float x, final float y, final float z) {
        return this.delegate.spawn(type, x, y, z);
    }

    public <E extends Mob> E spawnWithNoFreeWill(final EntityType<E> type, final BlockPos pos) {
        return this.delegate.spawnWithNoFreeWill(type, pos);
    }

    public <E extends Mob> E spawnWithNoFreeWill(final EntityType<E> type, final int x, final int y, final int z) {
        return this.delegate.spawnWithNoFreeWill(type, x, y, z);
    }

    public <E extends Mob> E spawnWithNoFreeWill(final EntityType<E> type, final Vec3 pos) {
        return this.delegate.spawnWithNoFreeWill(type, pos);
    }

    public <E extends Mob> E spawnWithNoFreeWill(final EntityType<E> type, final float x, final float y, final float z) {
        return this.delegate.spawnWithNoFreeWill(type, x, y, z);
    }

    public GameTestSequence walkTo(final Mob entity, final BlockPos pos, final float f) {
        return this.delegate.walkTo(entity, pos, f);
    }

    public void pressButton(final int x, final int y, final int z) {
        this.delegate.pressButton(x, y, z);
    }

    public void pressButton(final BlockPos pos) {
        this.delegate.pressButton(pos);
    }

    public void useBlock(final BlockPos pos) {
        this.delegate.useBlock(pos);
    }

    public LivingEntity makeAboutToDrown(final LivingEntity entity) {
        return this.delegate.makeAboutToDrown(entity);
    }

    public Player makeMockPlayer() {
        return this.delegate.makeMockPlayer();
    }

    public void pullLever(final int x, final int y, final int z) {
        this.delegate.pullLever(x, y, z);
    }

    public void pullLever(final BlockPos pos) {
        this.delegate.pullLever(pos);
    }

    public void pulseRedstone(final BlockPos pos, final long delay) {
        this.delegate.pulseRedstone(pos, delay);
    }

    public void destroyBlock(final BlockPos pos) {
        this.delegate.destroyBlock(pos);
    }

    public void setBlock(final int x, final int y, final int z, final Block block) {
        this.delegate.setBlock(x, y, z, block);
    }

    public void setBlock(final int x, final int y, final int z, final BlockState state) {
        this.delegate.setBlock(x, y, z, state);
    }

    public void setBlock(final BlockPos pos, final Block block) {
        this.delegate.setBlock(pos, block);
    }

    public void setBlock(final BlockPos pos, final BlockState state) {
        this.delegate.setBlock(pos, state);
    }

    public void setNight() {
        this.delegate.setNight();
    }

    public void setDayTime(final int timeOfDay) {
        this.delegate.setDayTime(timeOfDay);
    }

    public void assertBlockPresent(final Block block, final int x, final int y, final int z) {
        this.delegate.assertBlockPresent(block, x, y, z);
    }

    public void assertBlockPresent(final Block block, final BlockPos pos) {
        this.delegate.assertBlockPresent(block, pos);
    }

    public void assertBlockNotPresent(final Block block, final int x, final int y, final int z) {
        this.delegate.assertBlockNotPresent(block, x, y, z);
    }

    public void assertBlockNotPresent(final Block block, final BlockPos pos) {
        this.delegate.assertBlockNotPresent(block, pos);
    }

    public void succeedWhenBlockPresent(final Block block, final int x, final int y, final int z) {
        this.delegate.succeedWhenBlockPresent(block, x, y, z);
    }

    public void succeedWhenBlockPresent(final Block block, final BlockPos pos) {
        this.delegate.succeedWhenBlockPresent(block, pos);
    }

    public void assertBlock(final BlockPos pos, final Predicate<Block> predicate, final String errorMessage) {
        this.delegate.assertBlock(pos, predicate, errorMessage);
    }

    public void assertBlock(final BlockPos pos, final Predicate<Block> predicate, final Supplier<String> errorMessageSupplier) {
        this.delegate.assertBlock(pos, predicate, errorMessageSupplier);
    }

    public <T extends Comparable<T>> void assertBlockProperty(final BlockPos pos, final Property<T> property, final T value) {
        this.delegate.assertBlockProperty(pos, property, value);
    }

    public <T extends Comparable<T>> void assertBlockProperty(final BlockPos pos, final Property<T> property, final Predicate<T> predicate, final String errorMessage) {
        this.delegate.assertBlockProperty(pos, property, predicate, errorMessage);
    }

    public void assertBlockState(final BlockPos pos, final Predicate<BlockState> predicate, final Supplier<String> errorMessageSupplier) {
        this.delegate.assertBlockState(pos, predicate, errorMessageSupplier);
    }

    public void assertEntityPresent(final EntityType<?> type) {
        this.delegate.assertEntityPresent(type);
    }

    public void assertEntityPresent(final EntityType<?> type, final int x, final int y, final int z) {
        this.delegate.assertEntityPresent(type, x, y, z);
    }

    public void assertEntityPresent(final EntityType<?> type, final BlockPos pos) {
        this.delegate.assertEntityPresent(type, pos);
    }

    public void assertEntityPresent(final EntityType<?> type, final BlockPos pos, final double radius) {
        this.delegate.assertEntityPresent(type, pos, radius);
    }

    public void assertEntityInstancePresent(final Entity entity, final int x, final int y, final int z) {
        this.delegate.assertEntityInstancePresent(entity, x, y, z);
    }

    public void assertEntityInstancePresent(final Entity entity, final BlockPos pos) {
        this.delegate.assertEntityInstancePresent(entity, pos);
    }

    public void assertItemEntityCountIs(final Item item, final BlockPos pos, final double radius, final int amount) {
        this.delegate.assertItemEntityCountIs(item, pos, radius, amount);
    }

    public void assertItemEntityPresent(final Item item, final BlockPos pos, final double radius) {
        this.delegate.assertItemEntityPresent(item, pos, radius);
    }

    public void assertEntityNotPresent(final EntityType<?> type) {
        this.delegate.assertEntityNotPresent(type);
    }

    public void assertEntityNotPresent(final EntityType<?> type, final int x, final int y, final int z) {
        this.delegate.assertEntityNotPresent(type, x, y, z);
    }

    public void assertEntityNotPresent(final EntityType<?> type, final BlockPos pos) {
        this.delegate.assertEntityNotPresent(type, pos);
    }

    public void assertEntityTouching(final EntityType<?> type, final double x, final double y, final double z) {
        this.delegate.assertEntityTouching(type, x, y, z);
    }

    public void assertEntityNotTouching(final EntityType<?> type, final double x, final double y, final double z) {
        this.delegate.assertEntityNotTouching(type, x, y, z);
    }

    public <E extends Entity, T> void assertEntityData(final BlockPos pos, final EntityType<E> type, final Function<? super E, T> entityDataGetter, @org.jetbrains.annotations.Nullable final T data) {
        this.delegate.assertEntityData(pos, type, entityDataGetter, data);
    }

    public void assertContainerEmpty(final BlockPos pos) {
        this.delegate.assertContainerEmpty(pos);
    }

    public void assertContainerContains(final BlockPos pos, final Item item) {
        this.delegate.assertContainerContains(pos, item);
    }

    public void assertSameBlockStates(final BoundingBox checkedBlockBox, final BlockPos correctStatePos) {
        this.delegate.assertSameBlockStates(checkedBlockBox, correctStatePos);
    }

    public void assertSameBlockState(final BlockPos checkedPos, final BlockPos correctStatePos) {
        this.delegate.assertSameBlockState(checkedPos, correctStatePos);
    }

    public void assertAtTickTimeContainerContains(final long l, final BlockPos blockPos, final Item item) {
        this.delegate.assertAtTickTimeContainerContains(l, blockPos, item);
    }

    public void assertAtTickTimeContainerEmpty(final long l, final BlockPos blockPos) {
        this.delegate.assertAtTickTimeContainerEmpty(l, blockPos);
    }

    public <E extends Entity, T> void succeedWhenEntityData(final BlockPos blockPos, final EntityType<E> entityType, final Function<E, T> function, final T object) {
        this.delegate.succeedWhenEntityData(blockPos, entityType, function, object);
    }

    public <E extends Entity> void assertEntityProperty(final E entity, final Predicate<E> predicate, final String string) {
        this.delegate.assertEntityProperty(entity, predicate, string);
    }

    public <E extends Entity, T> void assertEntityProperty(final E entity, final Function<E, T> function, final String string, final T object) {
        this.delegate.assertEntityProperty(entity, function, string, object);
    }

    public void succeedWhenEntityPresent(final EntityType<?> type, final int x, final int y, final int z) {
        this.delegate.succeedWhenEntityPresent(type, x, y, z);
    }

    public void succeedWhenEntityPresent(final EntityType<?> type, final BlockPos pos) {
        this.delegate.succeedWhenEntityPresent(type, pos);
    }

    public void succeedWhenEntityNotPresent(final EntityType<?> type, final int x, final int y, final int z) {
        this.delegate.succeedWhenEntityNotPresent(type, x, y, z);
    }

    public void succeedWhenEntityNotPresent(final EntityType<?> type, final BlockPos pos) {
        this.delegate.succeedWhenEntityNotPresent(type, pos);
    }

    public void succeed() {
        this.delegate.succeed();
    }

    public void succeedIf(final Runnable runnable) {
        this.delegate.succeedIf(runnable);
    }

    public void succeedWhen(final Runnable runnable) {
        this.delegate.succeedWhen(runnable);
    }

    public void succeedOnTickWhen(final int duration, final Runnable runnable) {
        this.delegate.succeedOnTickWhen(duration, runnable);
    }

    public void runAtTickTime(final long tick, final Runnable runnable) {
        this.delegate.runAtTickTime(tick, runnable);
    }

    public void runAfterDelay(final long ticks, final Runnable runnable) {
        this.delegate.runAfterDelay(ticks, runnable);
    }

    public void randomTick(final BlockPos pos) {
        this.delegate.randomTick(pos);
    }

    public void fail(final String message, final BlockPos pos) {
        this.delegate.fail(message, pos);
    }

    public void fail(final String message, final Entity entity) {
        this.delegate.fail(message, entity);
    }

    public void fail(final String message) {
        this.delegate.fail(message);
    }

    public void failIf(final Runnable runnable) {
        this.delegate.failIf(runnable);
    }

    public void failIfEver(final Runnable runnable) {
        this.delegate.failIfEver(runnable);
    }

    public GameTestSequence startSequence() {
        return this.delegate.startSequence();
    }

    public BlockPos absolutePos(final BlockPos pos) {
        return this.delegate.absolutePos(pos);
    }

    public BlockPos relativePos(final BlockPos pos) {
        return this.delegate.relativePos(pos);
    }

    public Vec3 absoluteVec(final Vec3 pos) {
        return this.delegate.absoluteVec(pos);
    }

    public long getTick() {
        return this.delegate.getTick();
    }

    public void forEveryBlockInStructure(final Consumer<BlockPos> consumer) {
        this.delegate.forEveryBlockInStructure(consumer);
    }

    public void onEachTick(final Runnable runnable) {
        this.delegate.onEachTick(runnable);
    }
}
