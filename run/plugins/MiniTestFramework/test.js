import {registry, EntityType, Blocks} from "./minitestframework/index.mjs";

registry.register("test", (helper) => {
    helper.pressButton(3, 3, 3);
    helper.succeedWhenEntityPresent(EntityType.PIG, 1, 2, 3);
});

registry.register("test2", (helper) => {
    helper.pressButton(2, 3, 3);
    helper.succeedWhenEntityNotPresent(EntityType.PIG, 3, 2, 1);
});

registry.register("test3", (helper) => {
    helper.pressButton(1, 2, 3)
    helper.succeedWhenBlockPresent(Blocks.DIAMOND_BLOCK, 2, 2, 1);
});
