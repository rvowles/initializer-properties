package com.bluetrainsoftware.bathe.initializer;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * Created by Richard Vowles on 11/09/17.
 */
public class BatheTimeWatcherTest {
  @Test
  public void blankTest() {
    BatheTimeWatcher b = new BatheTimeWatcher();

    assertThat(b.isNotBlank("   ")).isFalse();
    assertThat(b.isNotBlank("")).isFalse();
    assertThat(b.isNotBlank(null)).isFalse();
    assertThat(b.isNotBlank(" b: ")).isTrue();
  }
}
