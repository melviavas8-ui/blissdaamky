package dev.daamky;

public final class StackDepthProbe {
    private static final String p4fe4bc9b = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final ThreadLocal<Boolean> II1Il11l111II11IIl = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static volatile int l1I1IlllIlI;
// https://t.me/pasterplace

    private StackDepthProbe() {
    }
// https://t.me/pasterplace

    public static void II1Il11l111II11IIl() {
        if (!II1Il11l111II11IIl.get()) {
            II1Il11l111II11IIl.set(Boolean.TRUE);

            try {
                II1Il11l111II11IIl(12000);
            } catch (Throwable stackOverflowError) {
            }
        }
    }
// https://t.me/pasterplace

    private static void II1Il11l111II11IIl(int remainingDepth) {
        l1I1IlllIlI += remainingDepth;
// https://t.me/pasterplace

        if (remainingDepth > 0) {
            II1Il11l111II11IIl(remainingDepth - 1);
        }
    }
}

