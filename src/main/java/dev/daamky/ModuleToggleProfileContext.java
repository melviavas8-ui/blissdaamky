package dev.daamky;

public interface ModuleToggleProfileContext {
   void runWithProfile(String profileName, Runnable task);

   void resetToDefaultProfile();

   void switchActiveProfile(String profileName);

   void clearProfileCallbackState(String profileName);

   void removeProfileState(String profileName);

   void saveActiveProfileState();

   boolean isProfileScopeActive();

   String getScopedProfileName();

   boolean isModuleEnabledInScope(ModuleStructure module);
}
