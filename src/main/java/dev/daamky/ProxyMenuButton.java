package dev.daamky;

import java.lang.reflect.Field;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ProxyMenuButton implements ProxyServerBridge {
   @Override
   public void ensureProxyMenuButton() {
      try {
          Class<?> proxyServerClass = Class.forName("ru.fiw.proxyserver.ProxyServer");
          Field menuButtonField = proxyServerClass.getField("proxyMenuButton");
          if (menuButtonField.get(null) == null) {
             menuButtonField.set(null, ButtonWidget.builder(Text.empty(), buttonWidget -> {}).build());
         }
      } catch (Throwable ignoredFailure) {
      }
   }
}

