/**
 * Copyright (c) 2012-2012 Malhar, Inc. All rights reserved.
 */
package com.malhartech.stream;

import com.malhartech.api.Operator;
import com.malhartech.annotation.ModuleAnnotation;
import com.malhartech.annotation.PortAnnotation;
import com.malhartech.annotation.PortAnnotation.PortType;
import com.malhartech.dag.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for message flow through DAG
 */
public class InlineStreamTest
{
  private static final Logger LOG = LoggerFactory.getLogger(InlineStreamTest.class);
  private Object prev;

  @Test
  @SuppressWarnings("SleepWhileInLoop")
  public void test() throws Exception
  {
    final int totalTupleCount = 5000;
    prev = null;

    final Module node1 = new PassThroughNode();
    node1.setup(new OperatorConfiguration("node1", null));

    final Module node2 = new PassThroughNode();
    node2.setup(new OperatorConfiguration("node2", null));

    InlineStream stream = new InlineStream();
    stream.setup(new StreamConfiguration());

    Sink sink = stream.connect(Component.INPUT, node1);
    node1.connect(Component.OUTPUT, sink);

    sink = node2.connect(Component.INPUT, stream);
    stream.connect("node2.input", sink);

    sink = new Sink()
    {
      /**
       *
       * @param t the value of t
       */
      @Override
      public void process(Object payload)
      {
        if (payload instanceof Tuple) {
          // we ignore the control tuple
        }
        else {
          if (prev == null) {
            prev = payload;
          }
          else {
            if (Integer.valueOf(payload.toString()) - Integer.valueOf(prev.toString()) != 1) {
              LOG.info("Got the tuples out of order!");
              LOG.info(prev + " followed by " + payload);
              synchronized (InlineStreamTest.this) {
                InlineStreamTest.this.notify();
              }
            }

            prev = payload;
          }

          if (Integer.valueOf(prev.toString()) == totalTupleCount - 1) {
            LOG.info("last tuple received.");
            synchronized (InlineStreamTest.this) {
              InlineStreamTest.this.notify();
            }
          }
        }
      }
    };
    node2.connect(Component.OUTPUT, sink);

    sink = node1.connect(Component.INPUT, new Sink()
    {
      // we just needed some random sink
      @Override
      public void process(Object payload)
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }
    });

    StreamContext streamContext = new StreamContext("node1->node2");

    stream.activate(streamContext);

    Map<String, Operator> activeNodes = new ConcurrentHashMap<String, Operator>();
    launchNodeThreads(Arrays.asList(node1, node2), activeNodes);

    for (int i = 0; i < totalTupleCount; i++) {
      sink.process(i);
    }

    synchronized (this) {
      this.wait(100);
    }

    Assert.assertTrue("last tuple", prev != null && totalTupleCount - Integer.valueOf(prev.toString()) == 1);
    Assert.assertEquals("active operators", 2, activeNodes.size());

    node2.deactivate();
    node1.deactivate();
    stream.deactivate();

    for (int i = 0; i < 10; i++) {
      Thread.sleep(20);
      if (activeNodes.isEmpty()) {
        break;
      }
    }

    node2.teardown();
    node1.teardown();
    stream.teardown();

    Assert.assertEquals("active operators", 0, activeNodes.size());
  }

  private void launchNodeThreads(Collection<? extends Module> nodes, final Map<String, Operator> activeNodes)
  {
    final AtomicInteger i = new AtomicInteger(0);
    for (final Module node: nodes) {
      // launch operators
      Runnable nodeRunnable = new Runnable()
      {
        @Override
        public void run()
        {
          OperatorContext ctx = new OperatorContext(String.valueOf(i.incrementAndGet()), Thread.currentThread());
          activeNodes.put(ctx.getId(), node);
          node.activate(ctx);
          activeNodes.remove(ctx.getId());
        }
      };

      Thread launchThread = new Thread(nodeRunnable);
      launchThread.start();
    }
  }

  /**
   * Operator implementation that simply passes on any tuple received
   */
  @ModuleAnnotation(ports = {
    @PortAnnotation(name = Component.INPUT, type = PortType.INPUT),
    @PortAnnotation(name = Component.OUTPUT, type = PortType.OUTPUT)
  })
  public static class PassThroughNode extends Module implements Sink
  {
    private boolean logMessages = false;

    public boolean isLogMessages()
    {
      return logMessages;
    }

    public void setLogMessages(boolean logMessages)
    {
      this.logMessages = logMessages;
    }

    @Override
    public void process(Object o)
    {
      emit(Component.OUTPUT, o);
    }
  }
}
