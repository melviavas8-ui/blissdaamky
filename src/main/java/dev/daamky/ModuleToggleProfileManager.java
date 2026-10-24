package dev.daamky;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ModuleToggleProfileManager {
    private static final String DEFAULT_PROFILE_NAME = "__main__";
    private static final ConcurrentHashMap<String, Set<String>> enabledModulesByProfile = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Set<String>> callbackEnabledModulesByProfile = new ConcurrentHashMap<>();
    private static final ThreadLocal<String> scopedProfileName = new ThreadLocal<>();
    private static volatile String activeProfileName = "__main__";
    private static volatile boolean initialized;
    private static final ModuleToggleProfileContext PROFILE_CONTEXT = new ModuleToggleProfileContext() {
        @Override
        public void runWithProfile(String profileName, Runnable runnable) {
            ModuleToggleProfileManager.runWithProfile(profileName, runnable);
        }

        @Override
        public void resetToDefaultProfile() {
            ModuleToggleProfileManager.resetToDefaultProfile();
        }

        @Override
        public void switchActiveProfile(String profileName) {
            ModuleToggleProfileManager.switchActiveProfile(profileName);
        }

        @Override
        public void clearProfileCallbackState(String profileName) {
            ModuleToggleProfileManager.clearProfileCallbackState(profileName);
        }

        @Override
        public void removeProfileState(String profileName) {
            ModuleToggleProfileManager.removeProfileState(profileName);
        }

        @Override
        public void saveActiveProfileState() {
            ModuleToggleProfileManager.saveActiveProfileState();
        }

        @Override
        public boolean isProfileScopeActive() {
            return ModuleToggleProfileManager.isProfileScopeActive();
        }

        @Override
        public String getScopedProfileName() {
            return ModuleToggleProfileManager.getScopedProfileName();
        }

        @Override
        public boolean isModuleEnabledInScope(ModuleStructure moduleStructure) {
            return ModuleToggleProfileManager.isModuleEnabledInScope(moduleStructure);
        }
    };

    private ModuleToggleProfileManager() {
    }

    public static ModuleToggleProfileContext getContext() {
        return PROFILE_CONTEXT;
    }

    public static void saveActiveProfileState() {
        if (ensureInitialized()) {
            enabledModulesByProfile.put(activeProfileName, snapshotCurrentEnabledModuleNames());
        }
    }

    public static void resetToDefaultProfile() {
        if (ensureInitialized()) {
            String previousProfileName = activeProfileName;
            saveActiveProfileState();
            if (!"__main__".equals(previousProfileName)) {
                callbackEnabledModulesByProfile.remove(previousProfileName);
            }

            activeProfileName = "__main__";
            applyProfile(activeProfileName);
        }
    }

    public static void switchActiveProfile(String profileName) {
        if (ensureInitialized() && profileName != null && !profileName.isBlank()) {
            saveActiveProfileState();
            activeProfileName = normalizeProfileName(profileName);
            callbackEnabledModulesByProfile.remove(activeProfileName);
            applyProfile(activeProfileName);
        }
    }

    public static void runWithProfile(String profileName, Runnable task) {
        if (ensureInitialized() && profileName != null && !profileName.isBlank()) {
            String normalizedProfileName = normalizeProfileName(profileName);
            String previousActiveProfileName = activeProfileName;
            Object previousScopedProfileName = scopedProfileName.get();
            List modules = ModuleRegistry.getInstance().getModules();
            int moduleCount = modules.size();
            boolean[] previousEnabledStates = new boolean[moduleCount];
            enabledModulesByProfile.put(previousActiveProfileName, collectEnabledModuleNames(modules));

            for (int moduleIndex = 0; moduleIndex < moduleCount; moduleIndex++) {
                previousEnabledStates[moduleIndex] = ((ModuleStructure)modules.get(moduleIndex)).getEnabledState();
            }

            activeProfileName = normalizedProfileName;

            try {
                invokeProfileEnableCallbacks(normalizedProfileName, modules);
                applyProfile(normalizedProfileName, modules);
                scopedProfileName.set(normalizedProfileName);
                task.run();
                Set enabledModuleNames = collectEnabledModuleNames(modules);
                enabledModulesByProfile.put(normalizedProfileName, enabledModuleNames);
                pruneProfileCallbackModules(normalizedProfileName, enabledModuleNames);
            } finally {
                restoreModuleEnabledStates(modules, previousEnabledStates);
                activeProfileName = previousActiveProfileName;
                if (previousScopedProfileName == null) {
                    scopedProfileName.remove();
                } else {
                    scopedProfileName.set((String)previousScopedProfileName);
                }
            }
        } else {
            task.run();
        }
    }

    public static void clearProfileCallbackState(String profileName) {
        if (profileName != null && !profileName.isBlank()) {
            callbackEnabledModulesByProfile.remove(normalizeProfileName(profileName));
        }
    }

    public static void removeProfileState(String profileName) {
        if (profileName != null && !profileName.isBlank()) {
            String normalizedProfileName = normalizeProfileName(profileName);
            enabledModulesByProfile.remove(normalizedProfileName);
            callbackEnabledModulesByProfile.remove(normalizedProfileName);
        }
    }

    public static String getScopedProfileName() {
        Object profileNameOverride = scopedProfileName.get();
        return (String)(profileNameOverride != null ? profileNameOverride : "__main__");
    }

    public static String getActiveProfileName() {
        Object profileNameOverride = scopedProfileName.get();
        return (String)(profileNameOverride != null ? profileNameOverride : activeProfileName);
    }

    public static String getDefaultProfileName() {
        return "__main__";
    }

    public static boolean isProfileAvailable(String profileName) {

        return false;
    }

    public static boolean isProfileScopeActive() {
        return scopedProfileName.get() != null;
    }

    public static boolean isModuleEnabledInScope(ModuleStructure moduleStructure) {
        if (!isProfileManagedModule(moduleStructure)) {
            return moduleStructure.getEnabledState();
        }

        Object profileNameOverride = scopedProfileName.get();
        return profileNameOverride == null ? moduleStructure.getEnabledState() : getOrCreateProfileModuleSet((String)profileNameOverride).contains(getProfileModuleKey(moduleStructure));
    }

    public static boolean isModuleEnabledInProfile(String profileName, ModuleStructure moduleStructure) {
        if (profileName != null && ensureInitialized()) {
            String normalizedProfileName = normalizeProfileName(profileName);
            return normalizedProfileName.equals(activeProfileName) ? moduleStructure.getEnabledState() : getOrCreateProfileModuleSet(normalizedProfileName).contains(getProfileModuleKey(moduleStructure));
        } else {
            return false;
        }
    }

    public static void setModuleEnabledInProfile(String profileName, ModuleStructure moduleStructure, boolean enabled) {
        if (profileName != null && ensureInitialized() && isProfileManagedModule(moduleStructure)) {
            String normalizedProfileName = normalizeProfileName(profileName);
            if (normalizedProfileName.equals(activeProfileName)) {
                moduleStructure.setEnabled(enabled);
            } else {
                Set<String> enabledModuleNames = getOrCreateProfileModuleSet(normalizedProfileName);
                if (enabled) {
                    enabledModuleNames.add(getProfileModuleKey(moduleStructure));
                } else {
                    enabledModuleNames.remove(getProfileModuleKey(moduleStructure));
                     Set<String> callbackEnabledModules = callbackEnabledModulesByProfile.get(normalizedProfileName);
                    if (callbackEnabledModules != null) {
                        callbackEnabledModules.remove(getProfileModuleKey(moduleStructure));
                    }
                }
            }
        }
    }

    public static Set<String> getEnabledModuleNames(String profileName) {
        return Set.copyOf(getOrCreateProfileModuleSet(normalizeProfileName(profileName)));
    }

    public static boolean isProfileManagedModule(ModuleStructure module) {
        ModuleCategory category = module.getCategory();
        return category != ModuleCategory.RENDER && category != ModuleCategory.THEMES;
    }

    private static String getProfileModuleKey(ModuleStructure module) {
        return module.getName().toLowerCase(Locale.ROOT);
    }

    private static void applyProfile(String profileName) {
        applyProfile(profileName, ModuleRegistry.getInstance().getModules(), true);
    }

    private static Set<String> snapshotCurrentEnabledModuleNames() {
        return collectEnabledModuleNames(ModuleRegistry.getInstance().getModules());
    }

    private static Set<String> collectEnabledModuleNames(List<ModuleStructure> modules) {
        HashSet enabledModuleNames = new HashSet(Math.max(16, (int)(modules.size() / 0.75F) + 1));

        for (Object moduleObject : modules) {
            if (isProfileManagedModule((ModuleStructure)moduleObject) && ((ModuleStructure)moduleObject).getEnabledState()) {
                enabledModuleNames.add(getProfileModuleKey((ModuleStructure)moduleObject));
            }
        }

        return enabledModuleNames;
    }

    private static void applyProfile(String profileName, List<ModuleStructure> modules) {
        applyProfile(profileName, modules, false);
    }

    private static void applyProfile(String profileName, List<ModuleStructure> modules, boolean invokeCallbacks) {
        Set enabledModuleNames = getOrCreateProfileModuleSet(profileName);

        for (Object moduleObject : modules) {
            if (isProfileManagedModule((ModuleStructure)moduleObject)) {
                boolean isEnabledInProfile = enabledModuleNames.contains(getProfileModuleKey((ModuleStructure)moduleObject));
                if (invokeCallbacks) {
                    ((ModuleStructure)moduleObject).setEnabled(isEnabledInProfile);
                } else {
                    ((ModuleStructure)moduleObject).setEnabledWithoutCallbacks(isEnabledInProfile);
                }
            }
        }
    }

    private static void invokeProfileEnableCallbacks(String profileName, List<ModuleStructure> modules) {
         Set<String> profileEnabledModules = getOrCreateProfileModuleSet(profileName);
         Set<String> callbackEnabledModules = callbackEnabledModulesByProfile.computeIfAbsent(profileName, profileKey -> ConcurrentHashMap.newKeySet());

         for (ModuleStructure module : modules) {
            if (isProfileManagedModule(module)) {
               String moduleName = getProfileModuleKey(module);
                if (profileEnabledModules.contains(moduleName) && !callbackEnabledModules.contains(moduleName)) {
                    callbackEnabledModules.add(moduleName);

                    try {
                        if (!module.getEnabledState()) {
                           module.setEnabledWithoutCallbacks(true);
                        }

                        module.invokeEnableCallback();
                        if (!module.getEnabledState()) {
                            callbackEnabledModules.remove(moduleName);
                        }
                    } catch (Throwable callbackFailure) {
                        callbackEnabledModules.remove(moduleName);
                        callbackFailure.printStackTrace();
                    }
                }
            }
        }
    }

    private static Set<String> getOrCreateProfileModuleSet(String profileName) {
        return enabledModulesByProfile.computeIfAbsent(profileName, profileKey -> ConcurrentHashMap.newKeySet());
    }

    private static void pruneProfileCallbackModules(String profileName, Set<String> enabledModuleNames) {
         Set<String> callbackEnabledModules = callbackEnabledModulesByProfile.get(profileName);
        if (callbackEnabledModules != null) {
            callbackEnabledModules.retainAll(enabledModuleNames);
            if (callbackEnabledModules.isEmpty()) {
                callbackEnabledModulesByProfile.remove(profileName);
            }
        }
    }

    private static void restoreModuleEnabledStates(List<ModuleStructure> modules, boolean[] previousEnabledStates) {
        for (int moduleIndex = 0; moduleIndex < previousEnabledStates.length; moduleIndex++) {
            ((ModuleStructure)modules.get(moduleIndex)).setEnabledWithoutCallbacks(previousEnabledStates[moduleIndex]);
        }
    }

    private static boolean ensureInitialized() {
        ModuleRegistry moduleRegistry = ModuleRegistry.getInstance();
        if (moduleRegistry != null && !moduleRegistry.getModules().isEmpty()) {
            if (!initialized) {
                enabledModulesByProfile.putIfAbsent("__main__", snapshotCurrentEnabledModuleNames());
                initialized = true;
            }

            return true;
        } else {
            return false;
        }
    }

    private static String normalizeProfileName(String profileName) {
        return profileName.toLowerCase(Locale.ROOT);
    }
}

