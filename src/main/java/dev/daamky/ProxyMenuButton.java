package dev.daamky;

import java.lang.reflect.Field;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ProxyMenuButton implements ProxyServerBridge {
    private static final String paae076f9 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
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

