const EntityType = Java.type('dev.benndorf.minitestframework.EntityTypeDelegate');

registry.register("test", (helper) => {
    helper.pressButton(3, 3, 3);
    helper.succeedWhenEntityPresent(EntityType.PIG, 1, 2, 3);
});

registry.register("test2", (helper) => {
    helper.pressButton(2, 3, 3);
    helper.succeedWhenEntityNotPresent(EntityType.PIG, 3, 2, 1);
})
