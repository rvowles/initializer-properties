package com.bluetrainsoftware.bathe.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * If startWatching is called, it will keep watching the files gathered and reload them if they have changed.
 * If DIE is set, then it will then die.
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class BaseConfigurationFileWatcher {
  private static final Logger log = LoggerFactory.getLogger(BatheTimeWatcher.class);
  public static final String BATHE_PROPERTY_LOADER_DIE_ON_CONFIG_CHANGE = "bathe.property-loader.dieOnConfigChange";
  public static final String BATHE_PROPERTY_LOADER_TIMEOUT = "bathe.property-loader.timeout";
  protected static Set<File> watchedFiles = new HashSet<>();
  protected static Map<File, Long> lastModified = new HashMap<>();
  protected static boolean requiresReloading = false;
  protected static int watchTimeout = 15; // seconds

  public void startWatching() {
    if (watchedFiles.size() > 0 && watchTimeout > 0) {
      new Thread(() -> {
        while (true) {
          try {
            Thread.sleep(watchTimeout * 1000);
          } catch (InterruptedException e) {
            return;
          }

          loadWatchedFiles();

          if (requiresReloading && System.getProperty(BATHE_PROPERTY_LOADER_DIE_ON_CONFIG_CHANGE) != null) {
            String dieVal = System.getProperty(BATHE_PROPERTY_LOADER_DIE_ON_CONFIG_CHANGE, "0");
            int exitVal = 0;
            try {
              exitVal = Integer.parseInt(dieVal);
            } catch (Exception e) {} // swallow, don't care

            exit(exitVal);
          }
        }
      }).start();
    } else {
      log.error("There are no files to watch or the timeout is 0, not watching configuration files.");
    }
  }

  // ensure you have system exit hooks to allow incoming traffic to finish if necessary
  // allows us to test
  protected void exit(int exitVal) {
    System.exit(exitVal);
  }

  protected boolean isYaml(File f) {
    String name = f.getName().toLowerCase();
    return name.endsWith(".yml") || name.endsWith(".yaml");
  }

  protected boolean isProperties(File f) {
    String name = f.getName().toLowerCase();
    return name.endsWith(".properties");
  }

  protected void loadWatchedFiles() {
    requiresReloading = false;

    Set<File> newWatchedFiles = watchedFiles.stream().map(propertyFile -> {
      File newFile = new File(propertyFile.getAbsolutePath());

      Long previousLastModifiedTime = lastModified.get(propertyFile);

      if (newFile.exists() && (previousLastModifiedTime == null || newFile.lastModified() != previousLastModifiedTime)) {
        log.info("Loading configuration `{}` into system properties", newFile.getAbsolutePath());

        Map<String, Object> propertySource = loadPropertyFile(newFile);

        lastModified.remove(propertyFile);

        if (propertySource == null) {
          return null;
        } else {
          lastModified.put(newFile, newFile.lastModified());
        }

        // merge them in
        propertySource.forEach((key, value) -> System.setProperty(key, value.toString()));

        requiresReloading = true;

        return newFile;
      }

      return propertyFile;
    })
      .filter(Objects::nonNull) // clear out failed ones
      .collect(Collectors.toSet());

    watchedFiles = newWatchedFiles;

    checkForTimerOverride();
  }

  protected Map<String, Object> loadPropertyFile(File propertyFile) {
    return null;
  }

  protected void checkForTimerOverride() {
    String timeout = System.getProperty(BATHE_PROPERTY_LOADER_TIMEOUT);
    if (timeout != null) {
      try {
        watchTimeout = Integer.parseInt(timeout);
      } catch (Exception ex) {
        // ignore failures
      }
    }
  }
}
