package ru.ik.deploy;

import java.util.prefs.Preferences;
import java.util.HashMap;

public class AppPreferences {

  public final static String DEPLOY_PATH = "DeployPath";

  private static AppPreferences instance;

  private HashMap<String, Object> preferencesStorage;

  private AppPreferences() {}

  public static AppPreferences getInstance() {
    if (instance == null) {
      instance = new AppPreferences();
      instance.read();
    }
    return instance;
  }
              
  private void read() {                                        
    Preferences prefs = Preferences.userNodeForPackage(AppPreferences.class);
    preferencesStorage = new HashMap<>();
    preferencesStorage.put(DEPLOY_PATH, prefs.get(DEPLOY_PATH, null));
  }

  public void save() {
    Preferences prefs = Preferences.userNodeForPackage(AppPreferences.class);
    prefs.put(DEPLOY_PATH, (String)preferencesStorage.get(DEPLOY_PATH));
  }

  public String get(String preferenceName) {
    return (String)preferencesStorage.get(preferenceName);
  }

  public String put(String preferenceName, String preferenceValue) {
    return (String)preferencesStorage.put(preferenceName, preferenceValue);
  }

}
