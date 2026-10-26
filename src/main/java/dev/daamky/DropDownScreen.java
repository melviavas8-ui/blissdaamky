package dev.daamky;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

/**
 * Альтернативный ClickGUI «Dropdown»: колонка на каждую категорию, модули раскрываются в настройки.
 * ЛКМ по модулю включает, ПКМ раскрывает настройки, СКМ задаёт бинд.
 */
public final class DropDownScreen extends Screen {
   private static final MsdfFont FONT = loadFont();
   private static final AvatarTexture CHECK_TEXTURE = Avatar.create("dropdown_check");
   private static final AvatarTexture ARROW_TEXTURE = Avatar.create("dropdown_arrow");
   private static final float PANEL_WIDTH = 120.0F;
   private static final float PANEL_GAP = 10.0F;
   private static final float PANEL_HEIGHT = 300.0F;
   private static final float HEADER_HEIGHT = 24.0F;
   private static final float MODULE_HEIGHT = 20.0F;
   private static final float MODULE_GAP = 3.5F;
   private static final float CHIP_HEIGHT = 9.0F;

   private final List<PanelView> panels = new ArrayList<>();
   private final SearchTextField searchField = new SearchTextField("Search", 24, text -> {});
   private final long openedAtMillis = System.currentTimeMillis();
   private long closingStartedAtMillis = -1L;
   private long previousFrameMillis = System.currentTimeMillis();
   private float frameDelta = 0.016F;
   private float screenWidth;
   private float screenHeight;
   private float layoutScale = 1.0F;
   private float mouseX;
   private float mouseY;
   private ModuleRow bindingModule;
   private IntSetting bindingSetting;

   public DropDownScreen() {
      super(Text.literal("Dropdown"));
      for (ModuleCategory category : ModuleCategory.values()) {
         if (category == ModuleCategory.THEMES
            || category == ModuleCategory.COSMETICS
            || category == ModuleCategory.BOTS
            || category == ModuleCategory.CLOUD_CONFIG
            || category == ModuleCategory.GLOBALS) {
            continue;
         }

         List<ModuleStructure> modules = ModuleRegistry.getInstance().getModulesByCategory(category);
         if (!modules.isEmpty()) {
            this.panels.add(new PanelView(category, modules));
         }
      }
   }

   private static MsdfFont loadFont() {
      try {
         return Medium.loadFont("montserrat");
      } catch (Throwable loadFailure) {
         return Medium.DEFAULT_FONT;
      }
   }

   // ---------- служебное ----------

   @Override
   public boolean shouldPause() {
      return false;
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Override
   public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
   }

   @Override
   public void renderInGameBackground(DrawContext drawContext) {
   }

   @Override
   public void close() {
      this.startClosing();
   }

   @Override
   public void removed() {
      ClientConfigManager.getInstance().saveConfig();
      super.removed();
   }

   private void startClosing() {
      if (this.closingStartedAtMillis < 0L) {
         this.closingStartedAtMillis = System.currentTimeMillis();
      }
   }

   private static float clamp01(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   private static float easeOutCubic(float progress) {
      float inverse = 1.0F - clamp01(progress);
      return 1.0F - inverse * inverse * inverse;
   }

   private float approach(float current, float target, float speed) {
      return current + (target - current) * (1.0F - (float)Math.exp(-this.frameDelta * speed));
   }

   private static int alpha(float value) {
      return Math.max(0, Math.min(255, (int)value));
   }

   private static RgbaColor gray(int level, float alpha) {
      return RgbaColor.rgba(level, level, level, alpha(alpha));
   }

   private static float[] hsbToRgb(float hue, float saturation, float brightness) {
      float h = (hue - (float)Math.floor(hue)) * 6.0F;
      float f = h - (float)Math.floor(h);
      float p = brightness * (1.0F - saturation);
      float q = brightness * (1.0F - saturation * f);
      float t = brightness * (1.0F - saturation * (1.0F - f));
      switch ((int)h) {
         case 0:
            return new float[]{brightness, t, p};
         case 1:
            return new float[]{q, brightness, p};
         case 2:
            return new float[]{p, brightness, t};
         case 3:
            return new float[]{p, q, brightness};
         case 4:
            return new float[]{t, p, brightness};
         default:
            return new float[]{brightness, p, q};
      }
   }

   private static RgbaColor hsbColor(float hue, float saturation, float brightness, int alpha) {
      float[] rgb = hsbToRgb(hue, saturation, brightness);
      return RgbaColor.rgba((int)(rgb[0] * 255.0F), (int)(rgb[1] * 255.0F), (int)(rgb[2] * 255.0F), alpha);
   }

   private boolean matchesSearch(String name) {
      String query = this.searchField.getText().replace(" ", "").toLowerCase(Locale.ROOT);
      return query.isEmpty() || name.replace(" ", "").toLowerCase(Locale.ROOT).contains(query);
   }

   // ---------- рисование примитивов ----------

   private void fill(DrawContext drawContext, float x, float y, float width, float height, float radius, RgbaColor color) {
      RoundedGradientRectBuilder.at(x, y, width, height).cornerRadius(radius).color(color).render(drawContext);
   }

   private void stroke(DrawContext drawContext, float x, float y, float width, float height, float radius, float border, RgbaColor color) {
      RoundedRectBuilder.createAt(x, y, width, height).cornerRadius(radius).borderWidth(border).color(color).render(drawContext);
   }

   private void text(DrawContext drawContext, String value, float x, float y, float size, HorizontalTextAlignment alignment, RgbaColor color) {
      GuiTextBuilder.createAt(FONT, value, x, y, size)
         .alignment(alignment)
         .baselinePosition(TextBaselinePosition.CENTER)
         .color(color)
         .render(drawContext);
   }

   private static float textWidth(String value, float size) {
      return FONT.measureWidth(value, size);
   }

   // ---------- кадр ----------

   @Override
   public void render(DrawContext drawContext, int rawMouseX, int rawMouseY, float tickDelta) {
      MinecraftClient client = MinecraftClient.getInstance();
      int windowScaleFactor = client.getWindow().getScaleFactor();
      if (windowScaleFactor <= 0) {
         return;
      }

      long now = System.currentTimeMillis();
      this.frameDelta = Math.max(0.001F, Math.min(0.1F, (float)(now - this.previousFrameMillis) / 1000.0F));
      this.previousFrameMillis = now;
      float closing = this.closingStartedAtMillis < 0L ? 0.0F : clamp01((float)(now - this.closingStartedAtMillis) / 180.0F);
      if (closing >= 1.0F) {
         client.setScreen(null);
         return;
      }

      float animation = easeOutCubic((float)(now - this.openedAtMillis) / 320.0F) * (1.0F - closing);
      this.screenWidth = GuiEventRenderer.getScreenWidth();
      this.screenHeight = GuiEventRenderer.getScreenHeight();
      float totalWidth = this.panels.size() * (PANEL_WIDTH + PANEL_GAP) - PANEL_GAP;
      this.layoutScale = Math.max(0.5F, Math.min(1.6F, Math.min(this.screenWidth / (totalWidth + 20.0F), this.screenHeight / (PANEL_HEIGHT + 70.0F))));
      float halfScale = windowScaleFactor / 2.0F;
      this.mouseX = (rawMouseX * halfScale - this.screenWidth * 0.5F) / this.layoutScale + this.screenWidth * 0.5F;
      this.mouseY = (rawMouseY * halfScale - this.screenHeight * 0.5F) / this.layoutScale + this.screenHeight * 0.5F;

      drawContext.getMatrices().pushMatrix();
      drawContext.getMatrices().scale(2.0F / windowScaleFactor);
      GuiRenderStateBatcher.beginBatching(drawContext);

      try {
         this.fill(drawContext, 0.0F, 0.0F, this.screenWidth, this.screenHeight, 0.0F, RgbaColor.rgba(0, 0, 0, alpha(120.0F * animation)));
         float popScale = this.layoutScale * (0.92F + 0.08F * animation);
         drawContext.getMatrices().pushMatrix();
         drawContext.getMatrices().translate(this.screenWidth * 0.5F, this.screenHeight * 0.5F);
         drawContext.getMatrices().scale(popScale, popScale);
         drawContext.getMatrices().translate(-this.screenWidth * 0.5F, -this.screenHeight * 0.5F);
         float startX = (this.screenWidth - totalWidth) * 0.5F;
         float top = (this.screenHeight - PANEL_HEIGHT) * 0.5F - 8.0F;

         for (int index = 0; index < this.panels.size(); index++) {
            PanelView panel = this.panels.get(index);
            panel.x = startX + index * (PANEL_WIDTH + PANEL_GAP);
            panel.y = top;
            panel.render(drawContext, animation);
         }

         this.searchField.render(drawContext, this.screenWidth * 0.5F - 60.0F, top + PANEL_HEIGHT + 10.0F, 120.0F, 15.0F, now, animation);
         drawContext.getMatrices().popMatrix();
      } finally {
         GuiRenderStateBatcher.endBatching();
         drawContext.getMatrices().popMatrix();
      }
   }

   // ---------- ввод ----------

   @Override
   public boolean mouseClicked(Click click, boolean doubleClick) {
      float halfScale = MinecraftClient.getInstance().getWindow().getScaleFactor() / 2.0F;
      float designX = ((float)click.x() * halfScale - this.screenWidth * 0.5F) / this.layoutScale + this.screenWidth * 0.5F;
      float designY = ((float)click.y() * halfScale - this.screenHeight * 0.5F) / this.layoutScale + this.screenHeight * 0.5F;
      int button = click.button();
      float searchX = this.screenWidth * 0.5F - 60.0F;
      float searchY = (this.screenHeight - PANEL_HEIGHT) * 0.5F - 8.0F + PANEL_HEIGHT + 10.0F;
      boolean overSearch = designX >= searchX && designX <= searchX + 120.0F && designY >= searchY && designY <= searchY + 15.0F;
      this.searchField.setFocused(overSearch);
      if (overSearch) {
         return true;
      }

      for (PanelView panel : this.panels) {
         if (designX >= panel.x && designX <= panel.x + PANEL_WIDTH && designY >= panel.y && designY <= panel.y + PANEL_HEIGHT) {
            panel.click(designX, designY, button);
            return true;
         }
      }

      return super.mouseClicked(click, doubleClick);
   }

   @Override
   public boolean mouseReleased(Click click) {
      for (PanelView panel : this.panels) {
         panel.release();
      }

      return super.mouseReleased(click);
   }

   @Override
   public boolean mouseDragged(Click click, double deltaX, double deltaY) {
      return true;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      for (PanelView panel : this.panels) {
         if (this.mouseX >= panel.x && this.mouseX <= panel.x + PANEL_WIDTH && this.mouseY >= panel.y && this.mouseY <= panel.y + PANEL_HEIGHT) {
            panel.targetScroll += (float)verticalAmount * 20.0F;
            return true;
         }
      }

      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   @Override
   public boolean keyPressed(KeyInput keyInput) {
      int keyCode = keyInput.getKeycode();
      if (this.bindingSetting != null || this.bindingModule != null) {
         int value = keyCode == 256 || keyCode == 261 || keyCode == 259 ? 0 : keyCode;
         if (this.bindingSetting != null) {
            this.bindingSetting.setValue(Integer.valueOf(value));
         } else {
            this.bindingModule.module.setKeybind(value);
         }

         this.bindingSetting = null;
         this.bindingModule = null;
         return true;
      }

      if (this.searchField.isFocused()) {
         if (keyCode == 256) {
            this.searchField.setFocused(false);
            return true;
         }

         return this.searchField.keyPressed(keyInput);
      }

      if (keyCode == 256 || keyCode == 344) {
         this.startClosing();
         return true;
      }

      return super.keyPressed(keyInput);
   }

   @Override
   public boolean charTyped(CharInput charInput) {
      if (this.searchField.isFocused()) {
         return this.searchField.charTyped(charInput);
      }

      return super.charTyped(charInput);
   }

   // ---------- колонка категории ----------

   private final class PanelView {
      final ModuleCategory category;
      final List<ModuleRow> rows = new ArrayList<>();
      float x;
      float y;
      float scroll;
      float targetScroll;
      float contentHeight;
      boolean draggingScrollbar;
      float dragStartMouseY;
      float dragStartScroll;

      PanelView(ModuleCategory category, List<ModuleStructure> modules) {
         this.category = category;
         for (ModuleStructure module : modules) {
            this.rows.add(new ModuleRow(module));
         }
      }

      float viewportHeight() {
         return PANEL_HEIGHT - HEADER_HEIGHT - 6.0F;
      }

      float minScroll() {
         return Math.min(0.0F, -(this.contentHeight - this.viewportHeight()));
      }

      void render(DrawContext drawContext, float animation) {
         DropDownScreen screen = DropDownScreen.this;
         screen.fill(drawContext, this.x, this.y, PANEL_WIDTH, PANEL_HEIGHT, 7.0F, RgbaColor.rgba(15, 15, 15, alpha(215.0F * animation)));
         screen.stroke(drawContext, this.x, this.y, PANEL_WIDTH, PANEL_HEIGHT, 7.0F, 0.7F, RgbaColor.rgba(255, 255, 255, alpha(18.0F * animation)));
         String title = this.category.II1Il11l111II11IIl();
         screen.text(
            drawContext, title, this.x + PANEL_WIDTH * 0.5F, this.y + 12.0F, 7.5F, HorizontalTextAlignment.CENTER, RgbaColor.rgba(255, 255, 255, alpha(255.0F * animation))
         );
         GuiTextBuilder.createAt(Medium.ICON_FONT, this.category.l1I1IlllIlI(), this.x + 10.0F, this.y + 12.0F, 8.0F)
            .alignment(HorizontalTextAlignment.CENTER)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getAccentColorWithAlpha(alpha(255.0F * animation)))
            .render(drawContext);
         if (this.draggingScrollbar) {
            float travel = Math.max(1.0F, this.viewportHeight() - 12.0F);
            float ratio = (screen.mouseY - this.dragStartMouseY) / travel;
            this.targetScroll = this.dragStartScroll - ratio * (this.contentHeight - this.viewportHeight());
         }

         this.targetScroll = Math.max(this.minScroll(), Math.min(0.0F, this.targetScroll));
         this.scroll = screen.approach(this.scroll, this.targetScroll, 14.0F);
         drawContext.enableScissor(
            PixelRounding.II1Il11l111II11IIl(this.x),
            PixelRounding.II1Il11l111II11IIl(this.y + HEADER_HEIGHT),
            PixelRounding.l1I1IlllIlI(this.x + PANEL_WIDTH),
            PixelRounding.l1I1IlllIlI(this.y + PANEL_HEIGHT - 4.0F)
         );

         try {
            float offset = 0.0F;

            for (ModuleRow row : this.rows) {
               if (!screen.matchesSearch(row.module.getName())) {
                  continue;
               }

               row.x = this.x + 6.5F;
               row.y = this.y + HEADER_HEIGHT + offset + this.scroll;
               row.width = PANEL_WIDTH - 13.0F;
               row.updateHeight();
               if (row.y + row.height >= this.y + HEADER_HEIGHT - 2.0F && row.y <= this.y + PANEL_HEIGHT) {
                  row.render(drawContext, animation);
               }

               offset += row.height + MODULE_GAP;
            }

            this.contentHeight = offset;
         } finally {
            drawContext.disableScissor();
         }

         if (this.contentHeight > this.viewportHeight()) {
            float trackTop = this.y + HEADER_HEIGHT + 4.0F;
            float trackHeight = this.viewportHeight() - 8.0F;
            float thumbHeight = Math.max(14.0F, trackHeight * this.viewportHeight() / this.contentHeight);
            float progress = this.minScroll() == 0.0F ? 0.0F : this.scroll / this.minScroll();
            float thumbTop = trackTop + (trackHeight - thumbHeight) * progress;
            screen.fill(
               drawContext,
               this.x + PANEL_WIDTH - 3.2F,
               thumbTop,
               1.6F,
               thumbHeight,
               0.8F,
               Accent.getAccentColorWithAlpha(alpha(150.0F * animation))
            );
         }
      }

      void click(float clickX, float clickY, int button) {
         DropDownScreen screen = DropDownScreen.this;
         if (button == 0 && this.contentHeight > this.viewportHeight() && clickX >= this.x + PANEL_WIDTH - 7.0F && clickY >= this.y + HEADER_HEIGHT) {
            this.draggingScrollbar = true;
            this.dragStartMouseY = clickY;
            this.dragStartScroll = this.targetScroll;
            return;
         }

         if (clickY < this.y + HEADER_HEIGHT || clickY > this.y + PANEL_HEIGHT - 4.0F) {
            return;
         }

         for (ModuleRow row : this.rows) {
            if (screen.matchesSearch(row.module.getName()) && clickY >= row.y && clickY <= row.y + row.height) {
               row.click(clickX, clickY, button);
               return;
            }
         }
      }

      void release() {
         this.draggingScrollbar = false;
         for (ModuleRow row : this.rows) {
            row.release();
         }
      }
   }

   // ---------- строка модуля ----------

   private final class ModuleRow {
      final ModuleStructure module;
      final List<SettingRow> settingRows = new ArrayList<>();
      boolean open;
      float openProgress;
      float x;
      float y;
      float width;
      float height = MODULE_HEIGHT;

      ModuleRow(ModuleStructure module) {
         this.module = module;

         for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting booleanSetting) {
               this.settingRows.add(new BooleanRow(booleanSetting));
            } else if (setting instanceof FloatSetting floatSetting) {
               this.settingRows.add(new SliderRow(floatSetting));
            } else if (setting instanceof MultiOptionSetting optionSetting) {
               this.settingRows.add(new ModeRow(optionSetting));
            } else if (setting instanceof MultiToggleSetting toggleSetting) {
               this.settingRows.add(new MultiToggleRow(toggleSetting));
            } else if (setting instanceof ColorSetting colorSetting) {
               this.settingRows.add(new ColorRow(colorSetting));
            } else if (setting instanceof IntSetting intSetting) {
               this.settingRows.add(new BindRow(intSetting));
            }
         }
      }

      boolean hasVisibleSettings() {
         for (SettingRow row : this.settingRows) {
            if (row.isVisible()) {
               return true;
            }
         }

         return false;
      }

      void updateHeight() {
         DropDownScreen screen = DropDownScreen.this;
         this.openProgress = screen.approach(this.openProgress, this.open ? 1.0F : 0.0F, 16.0F);
         if (this.openProgress < 0.002F && !this.open) {
            this.openProgress = 0.0F;
         }

         float settingsHeight = 0.0F;
         if (this.openProgress > 0.0F) {
            for (SettingRow row : this.settingRows) {
               if (row.isVisible()) {
                  settingsHeight += row.getHeight();
               }
            }
         }

         this.height = MODULE_HEIGHT + settingsHeight * this.openProgress;
      }

      void render(DrawContext drawContext, float animation) {
         DropDownScreen screen = DropDownScreen.this;
         float enabled = this.module.getAnimationProgress();
         boolean waitingForBind = screen.bindingModule == this;
         RoundedGradientRectBuilder.at(this.x + 0.7F, this.y + 1.0F, this.width - 1.4F, this.height - 2.0F)
            .cornerRadius(5.0F)
            .horizontalGradient(
               Accent.getAccentColorWithAlpha(alpha(70.0F * enabled * animation)), Accent.getAccentColorWithAlpha(0)
            )
            .render(drawContext);
         RoundedGradientRectBuilder.at(this.x + 0.7F, this.y + 1.0F, this.width - 1.4F, this.height - 2.0F)
            .cornerRadius(5.0F)
            .horizontalGradient(gray(153, 16.0F * (1.0F - enabled) * animation), gray(153, 0.0F))
            .render(drawContext);
         int level = 153 + (int)(102.0F * enabled);
         if (waitingForBind) {
            int bind = this.module.getKeybind();
            String label = bind == 0 ? "Bind..." : KeybindManager.getDisplayName(bind);
            screen.text(
               drawContext, label, this.x + this.width * 0.5F, this.y + 10.0F, 5.6F, HorizontalTextAlignment.CENTER, gray(153, 255.0F * animation)
            );
         } else {
            screen.text(
               drawContext,
               FONT.truncateToWidth(this.module.getName(), 6.2F, this.width - 30.0F),
               this.x + this.width / 12.0F,
               this.y + 10.0F,
               6.2F,
               HorizontalTextAlignment.LEFT,
               gray(level, 255.0F * animation)
            );
         }

         if (this.hasVisibleSettings() && !waitingForBind) {
            TextureQuadBuilder.at(ARROW_TEXTURE, this.x + this.width - 15.0F, this.y + 4.0F, 11.0F, 12.0F)
               .tint(gray(153, 255.0F))
               .opacity((0.35F + 0.45F * this.openProgress) * animation)
               .render(drawContext);
         }

         if (this.openProgress > 0.01F) {
            drawContext.enableScissor(
               PixelRounding.II1Il11l111II11IIl(this.x),
               PixelRounding.II1Il11l111II11IIl(this.y + MODULE_HEIGHT),
               PixelRounding.l1I1IlllIlI(this.x + this.width),
               PixelRounding.l1I1IlllIlI(this.y + this.height)
            );

            try {
               float settingY = this.y + MODULE_HEIGHT;

               for (SettingRow row : this.settingRows) {
                  if (!row.isVisible()) {
                     continue;
                  }

                  row.x = this.x;
                  row.y = settingY;
                  row.width = this.width;
                  row.render(drawContext, animation * this.openProgress);
                  settingY += row.getHeight();
               }
            } finally {
               drawContext.disableScissor();
            }
         }
      }

      void click(float clickX, float clickY, int button) {
         DropDownScreen screen = DropDownScreen.this;
         if (clickY <= this.y + MODULE_HEIGHT) {
            if (button == 0) {
               this.module.toggle();
            } else if (button == 1) {
               if (this.hasVisibleSettings()) {
                  this.open = !this.open;
               }
            } else if (button == 2) {
               screen.bindingModule = this;
               screen.bindingSetting = null;
            }

            return;
         }

         if (this.open) {
            for (SettingRow row : this.settingRows) {
               if (row.isVisible() && clickY >= row.y && clickY <= row.y + row.getHeight()) {
                  row.click(clickX, clickY, button);
                  return;
               }
            }
         }
      }

      void release() {
         for (SettingRow row : this.settingRows) {
            row.release();
         }
      }
   }

   // ---------- строки настроек ----------

   private abstract class SettingRow {
      float x;
      float y;
      float width;

      abstract Setting<?> getSetting();

      abstract float getHeight();

      abstract void render(DrawContext drawContext, float animation);

      abstract void click(float clickX, float clickY, int button);

      void release() {
      }

      boolean isVisible() {
         return this.getSetting().isVisible();
      }

      float[] layoutChips(List<String> labels, float topOffset) {
         float offset = 0.0F;
         float lineY = topOffset;
         float[] layout = new float[labels.size() * 3];

         for (int index = 0; index < labels.size(); index++) {
            float chipWidth = textWidth(labels.get(index), 4.8F) + 6.0F;
            if (offset + chipWidth > this.width - 14.0F && offset > 0.0F) {
               offset = 0.0F;
               lineY += CHIP_HEIGHT + 2.5F;
            }

            layout[index * 3] = 7.0F + offset;
            layout[index * 3 + 1] = lineY;
            layout[index * 3 + 2] = chipWidth;
            offset += chipWidth + 2.0F;
         }

         return layout;
      }

      float chipsHeight(List<String> labels) {
         if (labels.isEmpty()) {
            return 0.0F;
         }

         float[] layout = this.layoutChips(labels, 0.0F);
         return layout[(labels.size() - 1) * 3 + 1] + CHIP_HEIGHT;
      }

      void drawChip(DrawContext drawContext, String label, float chipX, float chipY, float chipWidth, boolean selected, float animation) {
         DropDownScreen screen = DropDownScreen.this;
         if (selected) {
            screen.fill(drawContext, chipX, chipY, chipWidth, CHIP_HEIGHT, 2.0F, Accent.getAccentColorWithAlpha(alpha(95.0F * animation)));
         } else {
            screen.fill(drawContext, chipX, chipY, chipWidth, CHIP_HEIGHT, 2.0F, RgbaColor.rgba(40, 40, 40, alpha(150.0F * animation)));
         }

         screen.text(
            drawContext,
            label,
            chipX + chipWidth * 0.5F,
            chipY + CHIP_HEIGHT * 0.5F,
            4.8F,
            HorizontalTextAlignment.CENTER,
            gray(selected ? 255 : 153, 255.0F * animation)
         );
      }
   }

   private final class BooleanRow extends SettingRow {
      private final BooleanSetting setting;

      BooleanRow(BooleanSetting setting) {
         this.setting = setting;
      }

      @Override
      Setting<?> getSetting() {
         return this.setting;
      }

      @Override
      float getHeight() {
         return 16.0F;
      }

      @Override
      void render(DrawContext drawContext, float animation) {
         DropDownScreen screen = DropDownScreen.this;
         this.setting.updateToggleProgress(screen.frameDelta);
         float progress = this.setting.getToggleProgress();
         screen.text(
            drawContext,
            FONT.truncateToWidth(this.setting.getName(), 5.4F, this.width - 32.0F),
            this.x + 7.0F,
            this.y + 8.0F,
            5.4F,
            HorizontalTextAlignment.LEFT,
            gray(153, 255.0F * animation)
         );
         float boxX = this.x + this.width - 18.0F;
         float boxY = this.y + 3.0F;
         screen.fill(drawContext, boxX, boxY, 10.0F, 10.0F, 3.0F, RgbaColor.rgba(25, 25, 25, alpha(170.0F * animation)));
         if (progress > 0.01F) {
            screen.fill(drawContext, boxX, boxY, 10.0F, 10.0F, 3.0F, Accent.getAccentColorWithAlpha(alpha(70.0F * progress * animation)));
            TextureQuadBuilder.at(CHECK_TEXTURE, boxX + 1.0F, boxY + 1.0F, 8.0F, 8.0F)
               .tint(gray(230, 255.0F))
               .opacity(progress * animation)
               .render(drawContext);
         }
      }

      @Override
      void click(float clickX, float clickY, int button) {
         if (button == 0) {
            this.setting.toggle();
         }
      }
   }

   private final class SliderRow extends SettingRow {
      private final FloatSetting setting;
      private float animatedProgress = -1.0F;
      private boolean dragging;

      SliderRow(FloatSetting setting) {
         this.setting = setting;
      }

      @Override
      Setting<?> getSetting() {
         return this.setting;
      }

      @Override
      float getHeight() {
         return 20.0F;
      }

      private float progress() {
         float range = this.setting.maximum() - this.setting.minimum();
         return range <= 0.0F ? 0.0F : clamp01((this.setting.getValue() - this.setting.minimum()) / range);
      }

      private void applyMouse(float mouseX) {
         float raw = this.setting.minimum() + clamp01((mouseX - this.x - 6.0F) / (this.width - 12.0F)) * (this.setting.maximum() - this.setting.minimum());
         float step = this.setting.step();
         float snapped = step > 0.0F ? this.setting.minimum() + Math.round((raw - this.setting.minimum()) / step) * step : raw;
         snapped = Math.max(this.setting.minimum(), Math.min(this.setting.maximum(), snapped));
         this.setting.setValue(Float.valueOf(Math.round(snapped * 1000.0F) / 1000.0F));
      }

      @Override
      void render(DrawContext drawContext, float animation) {
         DropDownScreen screen = DropDownScreen.this;
         if (this.dragging) {
            this.applyMouse(screen.mouseX);
         }

         float target = this.progress();
         this.animatedProgress = this.animatedProgress < 0.0F ? target : screen.approach(this.animatedProgress, target, 22.0F);
         String value = this.setting.formatValue();
         screen.text(
            drawContext,
            FONT.truncateToWidth(this.setting.getName(), 5.2F, this.width - textWidth(value, 5.2F) - 20.0F),
            this.x + 6.0F,
            this.y + 6.5F,
            5.2F,
            HorizontalTextAlignment.LEFT,
            gray(153, 255.0F * animation)
         );
         screen.text(
            drawContext, value, this.x + this.width - 6.0F, this.y + 6.5F, 5.2F, HorizontalTextAlignment.RIGHT, gray(153, 255.0F * animation)
         );
         float trackX = this.x + 6.0F;
         float trackWidth = this.width - 12.0F;
         float trackY = this.y + 13.0F;
         screen.fill(drawContext, trackX, trackY, trackWidth, 2.0F, 1.0F, RgbaColor.rgba(55, 55, 55, alpha(110.0F * animation)));
         float filled = Math.max(1.0F, trackWidth * this.animatedProgress);
         screen.fill(drawContext, trackX, trackY, filled, 2.0F, 1.0F, Accent.getAccentColorWithAlpha(alpha(210.0F * animation)));
         screen.fill(drawContext, trackX + filled - 2.5F, trackY - 1.5F, 5.0F, 5.0F, 2.5F, gray(220, 255.0F * animation));
      }

      @Override
      void click(float clickX, float clickY, int button) {
         if (button == 0 && clickY >= this.y + 9.0F) {
            this.dragging = true;
            this.applyMouse(clickX);
         }
      }

      @Override
      void release() {
         this.dragging = false;
      }
   }

   private final class ModeRow extends SettingRow {
      private final MultiOptionSetting setting;

      ModeRow(MultiOptionSetting setting) {
         this.setting = setting;
      }

      @Override
      Setting<?> getSetting() {
         return this.setting;
      }

      @Override
      float getHeight() {
         return 13.0F + this.chipsHeight(this.setting.getOptions()) + 3.0F;
      }

      @Override
      void render(DrawContext drawContext, float animation) {
         DropDownScreen screen = DropDownScreen.this;
         screen.text(
            drawContext, this.setting.getName(), this.x + 7.0F, this.y + 6.5F, 5.2F, HorizontalTextAlignment.LEFT, gray(153, 255.0F * animation)
         );
         List<String> options = this.setting.getOptions();
         float[] layout = this.layoutChips(options, 13.0F);

         for (int index = 0; index < options.size(); index++) {
            this.drawChip(
               drawContext,
               options.get(index),
               this.x + layout[index * 3],
               this.y + layout[index * 3 + 1],
               layout[index * 3 + 2],
               this.setting.isSelected(options.get(index)),
               animation
            );
         }
      }

      @Override
      void click(float clickX, float clickY, int button) {
         if (button != 0) {
            return;
         }

         List<String> options = this.setting.getOptions();
         float[] layout = this.layoutChips(options, 13.0F);

         for (int index = 0; index < options.size(); index++) {
            float chipX = this.x + layout[index * 3];
            float chipY = this.y + layout[index * 3 + 1];
            if (clickX >= chipX && clickX <= chipX + layout[index * 3 + 2] && clickY >= chipY && clickY <= chipY + CHIP_HEIGHT) {
               this.setting.setSelectedIndex(index);
               return;
            }
         }
      }
   }

   private final class MultiToggleRow extends SettingRow {
      private final MultiToggleSetting setting;

      MultiToggleRow(MultiToggleSetting setting) {
         this.setting = setting;
      }

      @Override
      Setting<?> getSetting() {
         return this.setting;
      }

      private List<String> labels() {
         List<String> labels = new ArrayList<>();
         for (BooleanSetting option : this.setting.getOptions()) {
            labels.add(option.getName());
         }

         return labels;
      }

      @Override
      float getHeight() {
         return 16.0F + this.chipsHeight(this.labels()) + 3.0F;
      }

      @Override
      void render(DrawContext drawContext, float animation) {
         DropDownScreen screen = DropDownScreen.this;
         this.setting.updateAnimation(screen.frameDelta);
         float progress = this.setting.getToggleProgress();
         screen.text(
            drawContext,
            FONT.truncateToWidth(this.setting.getName(), 5.4F, this.width - 32.0F),
            this.x + 7.0F,
            this.y + 8.0F,
            5.4F,
            HorizontalTextAlignment.LEFT,
            gray(153, 255.0F * animation)
         );
         float boxX = this.x + this.width - 18.0F;
         float boxY = this.y + 3.0F;
         screen.fill(drawContext, boxX, boxY, 10.0F, 10.0F, 3.0F, RgbaColor.rgba(25, 25, 25, alpha(170.0F * animation)));
         if (progress > 0.01F) {
            screen.fill(drawContext, boxX, boxY, 10.0F, 10.0F, 3.0F, Accent.getAccentColorWithAlpha(alpha(70.0F * progress * animation)));
            TextureQuadBuilder.at(CHECK_TEXTURE, boxX + 1.0F, boxY + 1.0F, 8.0F, 8.0F)
               .tint(gray(230, 255.0F))
               .opacity(progress * animation)
               .render(drawContext);
         }

         List<String> labels = this.labels();
         float[] layout = this.layoutChips(labels, 16.0F);
         List<BooleanSetting> options = this.setting.getOptions();

         for (int index = 0; index < labels.size(); index++) {
            this.drawChip(
               drawContext,
               labels.get(index),
               this.x + layout[index * 3],
               this.y + layout[index * 3 + 1],
               layout[index * 3 + 2],
               options.get(index).getValue(),
               animation
            );
         }
      }

      @Override
      void click(float clickX, float clickY, int button) {
         if (button != 0) {
            return;
         }

         if (clickY <= this.y + 16.0F) {
            this.setting.toggle();
            return;
         }

         List<String> labels = this.labels();
         float[] layout = this.layoutChips(labels, 16.0F);
         List<BooleanSetting> options = this.setting.getOptions();

         for (int index = 0; index < labels.size(); index++) {
            float chipX = this.x + layout[index * 3];
            float chipY = this.y + layout[index * 3 + 1];
            if (clickX >= chipX && clickX <= chipX + layout[index * 3 + 2] && clickY >= chipY && clickY <= chipY + CHIP_HEIGHT) {
               options.get(index).toggle();
               return;
            }
         }
      }
   }

   private final class ColorRow extends SettingRow {
      private static final float PICKER_HEIGHT = 58.0F;
      private final ColorSetting setting;
      private int dragMode;

      ColorRow(ColorSetting setting) {
         this.setting = setting;
      }

      @Override
      Setting<?> getSetting() {
         return this.setting;
      }

      @Override
      float getHeight() {
         return 18.0F + (PICKER_HEIGHT + 6.0F) * this.setting.getPickerOpenProgress();
      }

      private float squareX() {
         return this.x + 7.0F;
      }

      private float squareY() {
         return this.y + 22.0F;
      }

      private float squareWidth() {
         return this.width - 30.0F;
      }

      private float hueX() {
         return this.x + this.width - 18.0F;
      }

      private void applyDrag(float mouseX, float mouseY) {
         if (this.dragMode == 1) {
            float saturation = clamp01((mouseX - this.squareX()) / this.squareWidth());
            float brightness = 1.0F - clamp01((mouseY - this.squareY()) / PICKER_HEIGHT);
            this.setting.setHsb(this.setting.getHue(), saturation, brightness);
         } else if (this.dragMode == 2) {
            float hue = clamp01((mouseY - this.squareY()) / PICKER_HEIGHT);
            this.setting.setHsb(hue, this.setting.getSaturation(), this.setting.getBrightness());
         }
      }

      @Override
      void render(DrawContext drawContext, float animation) {
         DropDownScreen screen = DropDownScreen.this;
         this.setting.updatePickerAnimation(screen.frameDelta);
         if (this.dragMode != 0) {
            this.applyDrag(screen.mouseX, screen.mouseY);
         }

         screen.text(
            drawContext,
            FONT.truncateToWidth(this.setting.getName(), 5.4F, this.width - 34.0F),
            this.x + 7.0F,
            this.y + 9.0F,
            5.4F,
            HorizontalTextAlignment.LEFT,
            gray(153, 255.0F * animation)
         );
         screen.fill(drawContext, this.x + this.width - 24.0F, this.y + 4.5F, 16.0F, 9.0F, 3.5F, this.setting.getRgbaColor().withAlpha(alpha(255.0F * animation)));
         float open = this.setting.getPickerOpenProgress();
         if (open <= 0.01F) {
            return;
         }

         float hue = this.setting.getHue();
         float squareX = this.squareX();
         float squareY = this.squareY();
         float squareWidth = this.squareWidth();
         int fade = alpha(255.0F * animation * open);
         RoundedGradientRectBuilder.at(squareX, squareY, squareWidth, PICKER_HEIGHT)
            .cornerRadius(4.0F)
            .cornerColors(
               RgbaColor.rgba(255, 255, 255, fade),
               hsbColor(hue, 1.0F, 1.0F, fade),
               RgbaColor.rgba(0, 0, 0, fade),
               RgbaColor.rgba(0, 0, 0, fade)
            )
            .render(drawContext);
         float knobX = squareX + this.setting.getSaturation() * squareWidth;
         float knobY = squareY + (1.0F - this.setting.getBrightness()) * PICKER_HEIGHT;
         screen.fill(drawContext, knobX - 3.5F, knobY - 3.5F, 7.0F, 7.0F, 3.5F, RgbaColor.rgba(0, 0, 0, fade));
         screen.fill(drawContext, knobX - 2.5F, knobY - 2.5F, 5.0F, 5.0F, 2.5F, RgbaColor.rgba(255, 255, 255, fade));
         float segment = PICKER_HEIGHT / 6.0F;

         for (int index = 0; index < 6; index++) {
            RoundedGradientRectBuilder.at(this.hueX(), squareY + index * segment, 8.0F, segment + 0.5F)
               .cornerRadius(0.0F)
               .verticalGradient(hsbColor(index / 6.0F, 1.0F, 1.0F, fade), hsbColor((index + 1) / 6.0F, 1.0F, 1.0F, fade))
               .render(drawContext);
         }

         float hueKnobY = squareY + hue * PICKER_HEIGHT;
         screen.fill(drawContext, this.hueX() - 1.5F, hueKnobY - 2.0F, 11.0F, 4.0F, 2.0F, RgbaColor.rgba(255, 255, 255, fade));
      }

      @Override
      void click(float clickX, float clickY, int button) {
         if (button != 0) {
            return;
         }

         if (clickY <= this.y + 18.0F) {
            this.setting.togglePickerOpen();
            return;
         }

         if (this.setting.getPickerOpenProgress() < 0.5F) {
            return;
         }

         if (clickX >= this.hueX() - 3.0F && clickX <= this.hueX() + 11.0F) {
            this.dragMode = 2;
         } else if (clickX >= this.squareX() && clickX <= this.squareX() + this.squareWidth()) {
            this.dragMode = 1;
         }

         this.applyDrag(clickX, clickY);
      }

      @Override
      void release() {
         this.dragMode = 0;
      }
   }

   private final class BindRow extends SettingRow {
      private final IntSetting setting;

      BindRow(IntSetting setting) {
         this.setting = setting;
      }

      @Override
      Setting<?> getSetting() {
         return this.setting;
      }

      @Override
      float getHeight() {
         return 16.0F;
      }

      @Override
      void render(DrawContext drawContext, float animation) {
         DropDownScreen screen = DropDownScreen.this;
         boolean waiting = screen.bindingSetting == this.setting;
         int value = this.setting.getValue();
         String label = waiting ? "..." : (value == 0 ? "NONE" : KeybindManager.getDisplayName(value));
         float labelWidth = textWidth(label, 4.8F) + 8.0F;
         screen.text(
            drawContext,
            FONT.truncateToWidth(this.setting.getName(), 5.4F, this.width - labelWidth - 20.0F),
            this.x + 7.0F,
            this.y + 8.0F,
            5.4F,
            HorizontalTextAlignment.LEFT,
            gray(153, 255.0F * animation)
         );
         float chipX = this.x + this.width - labelWidth - 8.0F;
         screen.fill(
            drawContext,
            chipX,
            this.y + 3.5F,
            labelWidth,
            9.0F,
            2.5F,
            waiting ? Accent.getAccentColorWithAlpha(alpha(110.0F * animation)) : RgbaColor.rgba(40, 40, 40, alpha(170.0F * animation))
         );
         screen.text(
            drawContext, label, chipX + labelWidth * 0.5F, this.y + 8.0F, 4.8F, HorizontalTextAlignment.CENTER, gray(waiting ? 255 : 170, 255.0F * animation)
         );
      }

      @Override
      void click(float clickX, float clickY, int button) {
         if (button == 0) {
            DropDownScreen.this.bindingSetting = this.setting;
            DropDownScreen.this.bindingModule = null;
         }
      }
   }
}
