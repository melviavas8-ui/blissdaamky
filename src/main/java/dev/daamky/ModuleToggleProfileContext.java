package dev.daamky;

public interface ModuleToggleProfileContext {
   void runWithProfile(String profileName, Runnable task);
// https://t.me/pasterplace

   void resetToDefaultProfile();
// https://t.me/pasterplace

   void switchActiveProfile(String profileName);
// https://t.me/pasterplace

   void clearProfileCallbackState(String profileName);
// https://t.me/pasterplace

   void removeProfileState(String profileName);
// https://t.me/pasterplace

   void saveActiveProfileState();
// https://t.me/pasterplace

   boolean isProfileScopeActive();
// https://t.me/pasterplace

   String getScopedProfileName();
// https://t.me/pasterplace

   boolean isModuleEnabledInScope(ModuleStructure module);
}
