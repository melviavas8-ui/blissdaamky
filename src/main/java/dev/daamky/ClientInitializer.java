package dev.daamky;

import dev.daamky.core.event.EventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil.Type;

public class ClientInitializer implements ClientModInitializer {
    private static final String p13084e61 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    static ClientInitializer instance;
    private EventBus eventBus;
    private static KeyBinding clickGuiKey;
// https://t.me/pasterplace

    public void onInitializeClient() {
        StackDepthProbe.II1Il11l111II11IIl();

        instance = this;
        this.eventBus = new EventBus();
        RoundedRectangleRenderPipeline.II1Il11l111II11IIl();
        OutlineRenderPipeline.II1Il11l111II11IIl();
        ImageRenderPipeline.II1Il11l111II11IIl();
        RegionImageRenderPipeline.II1Il11l111II11IIl();
        Avatar.initialize();
        PlayerHeadRenderPipeline.II1Il11l111II11IIl();
        ShadowRenderPipeline.II1Il11l111II11IIl();
        Downsample.II1Il11l111II11IIl();
        GlassPanelRenderPipeline.II1Il11l111II11IIl();
        TextRenderPipeline.II1Il11l111II11IIl();
        WorldRenderPipelines.II1Il11l111II11IIl();
        JumpCirclePostPipeline.II1Il11l111II11IIl();
        WorldPostProcessPipelines.II1Il11l111II11IIl();
        HandsCapture.initialize();
        Medium.initializeFonts();
        Guest.II1Il11l111II11IIl();
        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.daamky.clickgui", Type.KEYSYM, 344, Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
            SavedBots.tickConnections();

            while (clickGuiKey.wasPressed()) {
                if (client.inGameHud != null && client.currentScreen == null) {
                    Search.getInstance().open();
                }
            }
        });
        this.eventBus.register(Search.getInstance());
        this.eventBus.register(DaamkyBotMap.getInstance());
        this.eventBus.register(BotExitOverlay.II1Il11l111II11IIl());
        this.eventBus.register(WingsAnimationRenderer.II1Il11l111II11IIl());
        this.eventBus.register(RotationPriorityManager.getInstance());
        this.eventBus.register(NeuroRotationController.getInstance());
        this.eventBus.register(KeybindManager.getInstance());
        this.eventBus.register(Key.getInstance());
        this.eventBus.register(Spooky.getInstance());
        this.eventBus.register(TeleportController.getInstance());
        this.eventBus.register(WaypointManager.getInstance());
        this.eventBus.register(TpsTracker.getInstance());
        this.eventBus.register(Priority.getInstance());
        this.eventBus.register(Priority3.getInstance());
        this.eventBus.register(CaptchaLinkOpener.II1Il11l111II11IIl());

        for (Object module : ModuleRegistry.getInstance().getModules()) {
            if (((ModuleStructure)module).isEnabled()) {
                this.eventBus.register(module);
            }
        }
// https://t.me/pasterplace

        ClientConfigManager.getInstance().loadConfig();
        GlobalsClient.getInstance().start();
        CloudConfigClient.getInstance().fetchCurrentUser();
        ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)client -> {
            ClientConfigManager.getInstance().saveConfig();
            SavedBots.shutdownSessions();
        });
    }
// https://t.me/pasterplace

    public static EventBus getEventBus() {
        return instance == null ? null : instance.eventBus;
    }
}

