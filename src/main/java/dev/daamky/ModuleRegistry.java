package dev.daamky;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleRegistry {
   private static final ModuleRegistry INSTANCE = new ModuleRegistry();
   private final List<ModuleStructure> modules = new ArrayList<>();

   private ModuleRegistry() {
      this.registerModule(new Aura());
      this.registerModule(new TriggerBot());
      this.registerModule(new AutoTotem());
      this.registerModule(new Velocity());
      this.registerModule(new HitBoxes());
      this.registerModule(new AntiBot());
      this.registerModule(new AutoPotion());
      this.registerModule(new AutoSwap());
      this.registerModule(new BowAimBot());
      this.registerModule(new CrystalAura());
      this.registerModule(new AutoExplosion());
      this.registerModule(new ElytraTarget());
      this.registerModule(new AimAssist());
      this.registerModule(new NoFriendDamage());
      this.registerModule(new AutoMace());
      this.registerModule(new CrystalOptimizer());
      this.registerModule(new BowSpammer());
      this.registerModule(new BackTrack());
      this.registerModule(new AutoAnchor());
      this.registerModule(new WebTrap());
      this.registerModule(new AutoSprint());
      this.registerModule(new NoSlow());
      this.registerModule(new Speed());
      this.registerModule(new Flight());
      this.registerModule(new Timer());
      this.registerModule(new GuiMove());
      this.registerModule(new NoWeb());
      this.registerModule(new ElytraHelper());
      this.registerModule(new ElytraMotion());
      this.registerModule(new ElytraBoost());
      this.registerModule(new ElytraBounce());
      this.registerModule(new DragonFly());
      this.registerModule(new ElytraDesync());
      this.registerModule(new NoClip());
      this.registerModule(new AirStuck());
      this.registerModule(new Spider());
      this.registerModule(new WindHop());
      this.registerModule(new ShulkerJump());
      this.registerModule(new TapeMouse());
      this.registerModule(new AutoFarm());
      this.registerModule(new PlayerHelper());
      this.registerModule(new WaterSpeed());
      this.registerModule(new Blink());
      this.registerModule(new ElytraFly());
      this.registerModule(new AutoEat());
      this.registerModule(new AutoFix());
      this.registerModule(new ClickPearl());
      this.registerModule(new AutoTool());
      this.registerModule(new ChestStealer());
      this.registerModule(new FastBreak());
      this.registerModule(new NoDelay());
      this.registerModule(new NoPush());
      this.registerModule(new AutoRespawn());
      this.registerModule(new AutoFish());
      this.registerModule(new AutoLeave());
      this.registerModule(new AutoArmor());
      this.registerModule(new InventoryCleaner());
      this.registerModule(new NoRotate());
      this.registerModule(new NoInteract());
      this.registerModule(new FreeCamera());
      this.registerModule(new Nuker());
      this.registerModule(new TargetPearl());
      this.registerModule(new TpKeyFinder());
      this.registerModule(new NoSlotChange());
      this.registerModule(new ClickThrough());
      this.registerModule(new ItemScroller());
      this.registerModule(new Optimizer());
      this.registerModule(new HudModule());
      this.registerModule(new NameTags());
      this.registerModule(new TargetEsp());
      this.registerModule(new FullBright());
      this.registerModule(new NoRender());
      this.registerModule(new CustomFog());
      this.registerModule(new Ambience());
      this.registerModule(new Beautifuly());
      this.registerModule(new ViewModel());
      this.registerModule(new SwingAnimation());
      this.registerModule(new Trails());
      this.registerModule(new Hands());
      this.registerModule(new AspectRatio());
      this.registerModule(new InvisibleOpacity());
      this.registerModule(new KillEffect());
      this.registerModule(new ESP());
      this.registerModule(new BlockESP());
      this.registerModule(new Tracers());
      this.registerModule(new JumpCircle());
      this.registerModule(new ChinaHat());
      this.registerModule(new Prediction());
      this.registerModule(new Trajectory());
      this.registerModule(new BlockOutline());
      this.registerModule(new Particles());
      this.registerModule(new LeaveTracker());
      this.registerModule(new LootAlert());
      this.registerModule(new PotionNotifier());
      this.registerModule(new ShulkerPreview());
      this.registerModule(new ServerHelper());
      this.registerModule(new AutoAccept());
      this.registerModule(new StructureFinder());
      this.registerModule(new AutoQuest());
      this.registerModule(new ClickFriend());
      this.registerModule(new NameProtect());
      this.registerModule(new ResourcePackSpoofer());
      this.registerModule(new KillMessage());
      this.registerModule(new Panic());
      this.registerModule(new ClientSounds());
      this.registerModule(new UseTracker());
      this.registerModule(new AutoJoin());
      this.registerModule(new AutoDuel());
      ModuleDescriptionLoader.II1Il11l111II11IIl(this.modules);
   }

   public static ModuleRegistry getInstance() {
      return INSTANCE;
   }

   public void registerModule(ModuleStructure moduleStructure) {
      this.modules.add(moduleStructure);
   }

   public List<ModuleStructure> getModules() {
      return Collections.unmodifiableList(this.modules);
   }

   public List<ModuleStructure> getModulesByCategory(ModuleCategory moduleCategory) {
      ArrayList arrayList = new ArrayList();

      for (Object module : this.modules) {
         if (((ModuleStructure)module).getCategory() == moduleCategory) {
            arrayList.add(module);
         }
      }

      return arrayList;
   }

   public <T extends ModuleStructure> T getModuleByClass(Class<T> moduleClass) {
      for (Object module : this.modules) {
          if (module.getClass() == moduleClass) {
            return (T)module;
         }
      }

      return null;
   }

   public ModuleStructure getModuleByName(String moduleName) {
      for (Object module : this.modules) {
         if (((ModuleStructure)module).getName().equalsIgnoreCase(moduleName)) {
            return (ModuleStructure)module;
         }
      }

      return null;
   }

   public void updateAnimations(float tickDelta) {
      for (Object module : this.modules) {
         ((ModuleStructure)module).updateAnimation(tickDelta);
      }
   }
}

