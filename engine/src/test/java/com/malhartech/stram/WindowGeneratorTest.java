/**
 * Copyright (c) 2012-2012 Malhar, Inc. All rights reserved.
 */
package com.malhartech.stram;

import com.malhartech.dag.Component;
import com.malhartech.dag.ResetWindowTuple;
import com.malhartech.api.Sink;
import com.malhartech.dag.Tuple;
import com.malhartech.util.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import junit.framework.Assert;
import org.apache.hadoop.conf.Configuration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowGeneratorTest
{
  public static final Logger logger = LoggerFactory.getLogger(WindowGeneratorTest.class);
  @Test
  public void test2ndResetWindow() throws InterruptedException
  {
    logger.debug("Testing 2nd Reset Window");

    ManualScheduledExecutorService msse = new ManualScheduledExecutorService(1);
    WindowGenerator generator = new WindowGenerator(msse);

    final Configuration config = new Configuration();
    config.setLong(WindowGenerator.FIRST_WINDOW_MILLIS, 0L);
    config.setInt(WindowGenerator.WINDOW_WIDTH_MILLIS, 1);

    generator.setup(config);

    final AtomicInteger beginWindowCount = new AtomicInteger(0);
    final AtomicInteger endWindowCount = new AtomicInteger(0);
    final AtomicInteger resetWindowCount = new AtomicInteger(0);
    final AtomicBoolean loggingEnabled = new AtomicBoolean(true);

    generator.connect(Component.OUTPUT, new Sink() {
      @Override
      public void process(Object payload)
      {
        if (loggingEnabled.get()) {
          logger.debug(payload.toString());
        }

        switch (((Tuple)payload).getType()) {
          case BEGIN_WINDOW:
            beginWindowCount.incrementAndGet();
            break;

          case END_WINDOW:
            endWindowCount.incrementAndGet();
            break;

          case RESET_WINDOW:
            resetWindowCount.incrementAndGet();
            break;
        }
      }
    });

    generator.activate(null);

    msse.tick(1);
    msse.tick(1);
    loggingEnabled.set(false);
    for (int i = 0; i < WindowGenerator.MAX_WINDOW_ID - 2; i++) {
      msse.tick(1);
    }
    loggingEnabled.set(true);
    msse.tick(1);

    Thread.sleep(20);

    Assert.assertEquals("begin windows", WindowGenerator.MAX_WINDOW_ID + 1 + 1, beginWindowCount.get());
    Assert.assertEquals("end windows", WindowGenerator.MAX_WINDOW_ID + 1, endWindowCount.get());
    Assert.assertEquals("reset windows", 2, resetWindowCount.get());
  }
  /**
   * Test of resetWindow functionality of WindowGenerator.
   */
  @Test
  public void testResetWindow()
  {
    System.out.println("resetWindow");

    ManualScheduledExecutorService msse = new ManualScheduledExecutorService(1);
    msse.setCurrentTimeMillis(0xcafebabe * 1000L);
    WindowGenerator generator = new WindowGenerator(msse);

    final Configuration config = new Configuration();
    config.setLong(WindowGenerator.FIRST_WINDOW_MILLIS, msse.getCurrentTimeMillis());
    config.setInt(WindowGenerator.WINDOW_WIDTH_MILLIS, 0x1234abcd);

    generator.setup(config);
    generator.connect(Component.OUTPUT, new Sink()
    {
      boolean firsttime = true;

      @Override
      public void process(Object payload)
      {
        if (firsttime) {
          assert (payload instanceof ResetWindowTuple);
          assert (((ResetWindowTuple)payload).getWindowId() == 0xcafebabe00000000L);
          assert (((ResetWindowTuple)payload).getBaseSeconds() * 1000L == config.getLong(WindowGenerator.FIRST_WINDOW_MILLIS, 0));
          assert (((ResetWindowTuple)payload).getIntervalMillis() == config.getInt(WindowGenerator.WINDOW_WIDTH_MILLIS, 0));
          firsttime = false;
        }
        else {
          assert (payload instanceof Tuple);
          assert (((Tuple)payload).getWindowId() == 0xcafebabe00000000L);
        }
      }
    });

    generator.activate(null);
    msse.tick(1);
  }

  @Test
  public void testWindowGen() throws Exception
  {
    final AtomicLong currentWindow = new AtomicLong();
    final AtomicInteger beginWindowCount = new AtomicInteger();
    final AtomicInteger endWindowCount = new AtomicInteger();

    final AtomicLong windowXor = new AtomicLong();

    Sink s = new Sink()
    {
      @Override
      public void process(Object payload)
      {
        long windowId = ((Tuple)payload).getWindowId();

        switch (((Tuple)payload).getType()) {
          case BEGIN_WINDOW:
            currentWindow.set(windowId);
            beginWindowCount.incrementAndGet();
            windowXor.set(windowXor.get() ^ windowId);
            System.out.println("begin: " + Long.toHexString(windowId) + " (" + Long.toHexString(System.currentTimeMillis() / 1000) + ")");
            break;

          case END_WINDOW:
            endWindowCount.incrementAndGet();
            windowXor.set(windowXor.get() ^ windowId);
            System.out.println("end  : " + Long.toHexString(windowId) + " (" + Long.toHexString(System.currentTimeMillis() / 1000) + ")");
            break;

          case RESET_WINDOW:
            break;

          default:
            currentWindow.set(0);
            break;
        }
      }
    };

    Configuration config = new Configuration();

    ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1, "WindowGenerator");
    long firstWindowMillis = stpe.getCurrentTimeMillis();
    firstWindowMillis = firstWindowMillis - firstWindowMillis % 1000L;

    config.setLong(WindowGenerator.FIRST_WINDOW_MILLIS, firstWindowMillis);
    config.setInt(WindowGenerator.WINDOW_WIDTH_MILLIS, 200);
    // even if you do not set it, it defaults to the value we are trying to set here.
    // config.setLong(WindowGenerator.RESET_WINDOW_MILLIS, config.getLog(WindowGenerator.FIRST_WINDOW_MILLIS));

    WindowGenerator wg = new WindowGenerator(new ScheduledThreadPoolExecutor(1, "WindowGenerator"));
    wg.setup(config);
    wg.connect("GeneratorTester", s);

    wg.activate(null);
    Thread.sleep(200);
    wg.deactivate();
    long lastWindowMillis = System.currentTimeMillis();


    System.out.println("firstWindowMillis: " + firstWindowMillis + " lastWindowMillis: " + lastWindowMillis + " completed windows: " + endWindowCount.get());
    Assert.assertEquals("only last window open", currentWindow.get(), windowXor.get());

    long expectedCnt = (lastWindowMillis - firstWindowMillis) / config.getInt(WindowGenerator.WINDOW_WIDTH_MILLIS, 200);

    Assert.assertTrue("Minimum begin window count", expectedCnt + 1 <= beginWindowCount.get());
    Assert.assertEquals("end window count", beginWindowCount.get() - 1, endWindowCount.get());
  }
}
