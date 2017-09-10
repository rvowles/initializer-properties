package com.bluetrainsoftware.bathe.initializer

import org.junit.Test

/**
 * Created by Richard Vowles on 11/09/17.
 */
class BatheTimeWatcherSpec {
  @Test
  public void blankTest() {
    BatheTimeWatcher b = new BatheTimeWatcher()

    assert !b.isNotBlank("   ")
    assert !b.isNotBlank("")
    assert !b.isNotBlank(null)
    assert b.isNotBlank(" b ")
  }
}
