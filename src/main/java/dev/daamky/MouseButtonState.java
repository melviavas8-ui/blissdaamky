package dev.daamky;

public final class MouseButtonState {
    private static volatile boolean II1Il11l111II11IIl;
    private static volatile boolean l1I1IlllIlI;

    private MouseButtonState() {
    }

    public static void recordButtonState(int button, int action) {
        if (button == 0) {
            II1Il11l111II11IIl = action != 0;
        } else if (button == 1) {
            l1I1IlllIlI = action != 0;
        }
    }

    public static boolean isPrimaryButtonDown() {
        return II1Il11l111II11IIl;
    }

    public static boolean isSecondaryButtonDown() {
        return l1I1IlllIlI;
    }
}

