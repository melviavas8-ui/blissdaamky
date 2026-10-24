package dev.daamky;

public final class StackDepthProbe {
    private static final ThreadLocal<Boolean> II1Il11l111II11IIl = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static volatile int l1I1IlllIlI;

    private StackDepthProbe() {
    }

    public static void II1Il11l111II11IIl() {
        if (!II1Il11l111II11IIl.get()) {
            II1Il11l111II11IIl.set(Boolean.TRUE);

            try {
                II1Il11l111II11IIl(12000);
            } catch (Throwable stackOverflowError) {
            }
        }
    }

    private static void II1Il11l111II11IIl(int remainingDepth) {
        l1I1IlllIlI += remainingDepth;

        if (remainingDepth > 0) {
            II1Il11l111II11IIl(remainingDepth - 1);
        }
    }
}

