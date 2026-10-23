package dev.daamky;

import dev.daamky.core.event.EventBus;
import dev.daamky.core.event.impl.EventPacket;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.common.ClearDialogS2CPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.CustomReportDetailsS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerLinksS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.network.packet.s2c.common.ShowDialogS2CPacket;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.ServerLinks;
import net.minecraft.util.Identifier;

abstract class BotPacketListenerInvocationHandler implements BotPacketListenerProxy, InvocationHandler {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final ScheduledExecutorService RESOURCE_PACK_STATUS_EXECUTOR = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "bot-resourcepack");
        thread.setDaemon(true);
        return thread;
    });
    protected final MinecraftClient client;
    protected final ClientConnection connection;
    protected final BotSession disconnect;
    protected final BotSessionProfile sessionProfile;
    protected final Map<Identifier, byte[]> serverCookies;
    protected String serverBrand;
    protected Map<String, String> customReportDetails = Map.of();
    protected ServerLinks serverLinks = ServerLinks.EMPTY;
    protected Object proxyInstance;
// https://t.me/pasterplace

    BotPacketListenerInvocationHandler(MinecraftClient client, ClientConnection clientConnection, BotSession disconnect, BotSessionProfile l1lll11l1l, Map<Identifier, byte[]> entries) {
        this.client = client;
        this.connection = clientConnection;
        this.disconnect = disconnect;
        this.sessionProfile = l1lll11l1l;
        this.serverCookies = entries == null ? new HashMap<>() : entries;
    }
// https://t.me/pasterplace

    protected <T> T createProxy(Class<T> type, Class<?>... classArray) {
        Class[] proxyInterfaces = new Class[classArray.length + 3];
        proxyInterfaces[0] = type;
        proxyInterfaces[1] = DisconnectSessionView.class;
        proxyInterfaces[2] = BotPacketListenerProxy.class;
        System.arraycopy(classArray, 0, proxyInterfaces, 3, classArray.length);
        Object proxyInstance = Proxy.newProxyInstance(BotPacketListenerInvocationHandler.class.getClassLoader(), proxyInterfaces, this);
        this.proxyInstance = proxyInstance;
        return (T)proxyInstance;
    }
// https://t.me/pasterplace

    protected boolean requiresClientThreadDispatch() {
        return false;
    }
// https://t.me/pasterplace

    @Override
    public Object invoke(Object proxy, Method method, Object[] objectArray) throws Throwable {
        Class declaringClass = method.getDeclaringClass();
        Class[] parameterTypes = method.getParameterTypes();
        Object firstArgument = objectArray != null && objectArray.length > 0 ? objectArray[0] : null;
        if (declaringClass == Object.class) {
            return switch (method.getName()) {
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == firstArgument;
                default -> this.getClass().getSimpleName() + "[" + this.disconnect.getUsername() + "]";
            };
        } else {
            if (declaringClass == DisconnectSessionView.class) {
                return this.disconnect;
            }

            if (declaringClass == BotPacketListenerProxy.class) {
                if (method.getReturnType() == void.class) {
                    this.onOutgoingPacketSent((Packet<?>)firstArgument);
                    return null;
                } else {
                    return this.filterOutgoingPacket((Packet<?>)firstArgument);
                }
            } else {
                if (declaringClass == TickablePacketListener.class && parameterTypes.length == 0) {
                    this.tickConnection();
                    return null;
                }

                if (method.getReturnType() == NetworkSide.class && parameterTypes.length == 0) {
                    return NetworkSide.CLIENTBOUND;
                }

                if (parameterTypes.length == 1 && parameterTypes[0] == DisconnectionInfo.class && method.getReturnType() == void.class) {
                    this.onDisconnected((DisconnectionInfo)firstArgument);
                    return null;
                }

                if (parameterTypes.length == 1
                && method.getReturnType() == void.class
                && parameterTypes[0] != Packet.class
                && Packet.class.isAssignableFrom(parameterTypes[0])
                && firstArgument instanceof Packet name) {
                    if (this.requiresClientThreadDispatch() && !this.client.isOnThread() && !(name instanceof KeepAliveS2CPacket)) {
                        this.client.execute(() -> this.dispatchPacket(name));
                    } else {
                        this.dispatchPacket(name);
                    }
// https://t.me/pasterplace

                    return null;
                } else if (method.isDefault()) {
                    return InvocationHandler.invokeDefault(proxy, method, objectArray);
                } else {
                    return parameterTypes.length == 0 && method.getReturnType() == boolean.class
                    ? this.connection.isOpen()
                    : defaultValueForType(method.getReturnType());
                }
            }
        }
    }
// https://t.me/pasterplace

    protected void dispatchPacket(Packet<?> packet) {
        if (this.connection.isOpen() || packet instanceof DisconnectS2CPacket || packet instanceof LoginDisconnectS2CPacket) {
            try {
                if (this.handleCommonPacket(packet)) {
                    return;
                }

                this.handleLoginOrConfigurationPacket(packet);
            } catch (Throwable packetFailure) {
                BotCaptchaLog.II1Il11l111II11IIl("[" + this.disconnect.getUsername() + "] packet " + packet.getClass().getSimpleName() + " failed: " + packetFailure);
            }
        }
    }
// https://t.me/pasterplace

    protected abstract boolean handleLoginOrConfigurationPacket(Packet<?> packet);
// https://t.me/pasterplace

    protected void tickConnection() {
    }
// https://t.me/pasterplace

    private boolean handleCommonPacket(Packet<?> packet) {
        if (packet instanceof KeepAliveS2CPacket keepAlivePacket) {
            this.sendPacketIfOpen(new KeepAliveC2SPacket(keepAlivePacket.getId()));
        } else if (packet instanceof CommonPingS2CPacket pingPacket) {
            this.sendPacketIfOpen(new CommonPongC2SPacket(pingPacket.getParameter()));
        } else if (packet instanceof CustomPayloadS2CPacket customPayloadPacket) {
            CustomPayload customPayload = customPayloadPacket.payload();
            if (customPayload instanceof BrandCustomPayload) {
                BrandCustomPayload brandPayload = (BrandCustomPayload)customPayload;
                this.serverBrand = brandPayload.brand();
            }
        } else if (packet instanceof DisconnectS2CPacket disconnectPacket) {
            this.connection.disconnect(disconnectPacket.reason());
        } else if (packet instanceof LoginDisconnectS2CPacket loginDisconnectPacket) {
            this.connection.disconnect(loginDisconnectPacket.reason());
        } else if (packet instanceof CookieRequestS2CPacket cookieRequestPacket) {
            this.sendPacketIfOpen(new CookieResponseC2SPacket(cookieRequestPacket.key(), this.serverCookies.get(cookieRequestPacket.key())));
        } else if (packet instanceof StoreCookieS2CPacket storeCookiePacket) {
            this.serverCookies.put(storeCookiePacket.key(), storeCookiePacket.payload());
        } else if (packet instanceof CustomReportDetailsS2CPacket reportDetailsPacket) {
            this.customReportDetails = reportDetailsPacket.details();
        } else if (packet instanceof ServerLinksS2CPacket) {
            this.serverLinks = ServerLinks.EMPTY;
        } else if (packet instanceof ResourcePackSendS2CPacket resourcePackPacket) {
            this.sessionProfile.resourcePackHandler().handleResourcePackOffer(resourcePackPacket, this.connection, RESOURCE_PACK_STATUS_EXECUTOR);
        } else {
            if (packet instanceof ResourcePackRemoveS2CPacket || packet instanceof ClearDialogS2CPacket || packet instanceof ShowDialogS2CPacket) {
                return true;
            }

            if (!(packet instanceof ServerTransferS2CPacket serverTransferPacket)) {
                return false;
            }

            this.sessionProfile.transferIgnoredHandler().II1Il11l111II11IIl(serverTransferPacket, this.connection);
        }

        return true;
    }
// https://t.me/pasterplace

    protected void onDisconnected(DisconnectionInfo disconnectionInfo) {
        this.disconnect.handleDisconnection(disconnectionInfo == null ? "" : disconnectionInfo.reason().getString());
    }
// https://t.me/pasterplace

    public void sendPacketIfOpen(Packet<?> packet) {
        if (this.connection.isOpen()) {
            this.connection.send(packet);
        }
    }
// https://t.me/pasterplace

    @Override
    public Packet<?> filterOutgoingPacket(Packet<?> packet) {
        EventBus eventBus = ClientInitializer.getEventBus();
        if (eventBus == null) {
            return packet;
        } else if (!this.client.isOnThread()) {
            this.client.execute(() -> this.runWithModuleProfile(() -> eventBus.post(new EventPacket(packet, EventPacket.Direction.SEND))));
            return packet;
        } else {
            EventPacket eventPacket = new EventPacket(packet, EventPacket.Direction.SEND);
            this.runWithModuleProfile(() -> eventBus.post(eventPacket));
            return eventPacket.isCancelled() ? null : eventPacket.packet();
        }
    }
// https://t.me/pasterplace

    @Override
    public void onOutgoingPacketSent(Packet<?> packet) {
    }
// https://t.me/pasterplace

    protected void runWithModuleProfile(Runnable runnable) {
        ModuleToggleProfileManager.runWithProfile(this.disconnect.getUsername(), runnable);
    }
// https://t.me/pasterplace

    private static Object defaultValueForType(Class<?> type) {
        if (type == boolean.class) {
            return false;
        } else if (type == int.class) {
            return 0;
        } else if (type == long.class) {
            return 0L;
        } else if (type == float.class) {
            return 0.0F;
        } else if (type == double.class) {
            return 0.0;
        } else if (type == short.class) {
            return 0;
        } else if (type == byte.class) {
            return 0;
        } else {
            return type == char.class ? '\u0000' : null;
        }
    }
}

