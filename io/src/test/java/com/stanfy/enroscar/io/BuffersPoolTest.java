package com.stanfy.enroscar.io;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link BuffersPool}. Absolutely useless.
 */
public class BuffersPoolTest {

  /** Buffers pool instance. */
  private BuffersPool buffersPool;
  /** Size of the biggest buffer available in pool after initialization. */
  private int maxAvailableSize;

  /** Random. */
  private final Random r = new Random();

  @Before
  public void createPool() {
    final int maxSize = 1024;
    maxAvailableSize = maxSize;
    buffersPool = new BuffersPool(new int[][] {
        {1, maxSize}, {2, maxSize / 2}
    });

  }

  @Test
  public void shouldReportCorrectStatsAfterCreation() {
    assertThat(buffersPool.getAllocatedBuffersCount()).isEqualTo(3);
    assertThat(buffersPool.getUsedBuffersCount()).isZero();
  }

  @Test
  public void shouldBeAbleToAllocateNewBuffer() {
    int prevCount = buffersPool.getAllocatedBuffersCount();
    byte[] buffer = buffersPool.get(maxAvailableSize + 1);
    assertThat(buffer).isNotNull();
    assertThat(buffersPool.getAllocatedBuffersCount()).isEqualTo(prevCount + 1);
  }

  @Test
  public void shouldUseAvailableBuffers() {
    int expectedBuffersCount = buffersPool.getAllocatedBuffersCount();

    byte[] buffer1 = buffersPool.get(maxAvailableSize / 4);
    assertThat(buffer1).isNotNull();
    assertThat(buffersPool.getAllocatedBuffersCount()).isEqualTo(expectedBuffersCount);
    assertThat(buffersPool.getUsedBuffersCount()).isEqualTo(1);

    byte[] buffer2 = buffersPool.get(maxAvailableSize / 2);
    assertThat(buffer2).isNotNull();
    assertThat(buffersPool.getAllocatedBuffersCount()).isEqualTo(expectedBuffersCount);
    assertThat(buffersPool.getUsedBuffersCount()).isEqualTo(2);

    byte[] buffer3 = buffersPool.get(maxAvailableSize);
    assertThat(buffer3).isNotNull();
    assertThat(buffersPool.getAllocatedBuffersCount()).isEqualTo(expectedBuffersCount);
    assertThat(buffersPool.getUsedBuffersCount()).isEqualTo(3);

    byte[] buffer4 = buffersPool.get(2);
    expectedBuffersCount++;
    assertThat(buffer4).isNotNull();
    assertThat(buffersPool.getAllocatedBuffersCount()).isEqualTo(expectedBuffersCount);
    assertThat(buffersPool.getUsedBuffersCount()).isEqualTo(4);

    buffersPool.release(buffer1);
    buffersPool.release(buffer2);
    buffersPool.release(buffer3);
    buffersPool.release(buffer4);
    assertThat(buffersPool.getAllocatedBuffersCount()).isEqualTo(expectedBuffersCount);
    assertThat(buffersPool.getUsedBuffersCount()).isZero();

    assertThat(buffersPool.get(maxAvailableSize / 3)).isNotNull();
    assertThat(buffersPool.getAllocatedBuffersCount()).isEqualTo(expectedBuffersCount);
    assertThat(buffersPool.getUsedBuffersCount()).isEqualTo(1);
  }

  @Test
  public void flushShouldClearsRetainedBuffers() {
    byte[] buffer = buffersPool.get(maxAvailableSize);
    int allocationsCount = buffersPool.getAllocatedBuffersCount();
    assertThat(allocationsCount).isGreaterThan(0);
    assertThat(buffersPool.getBuffersMapSize()).isGreaterThan(0);

    buffersPool.flush();
    assertThat(buffersPool.getAllocatedBuffersCount()).isEqualTo(allocationsCount);
    assertThat(buffersPool.getUsedBuffersCount()).isEqualTo(1);
    assertThat(buffersPool.getBuffersMapSize()).isZero();

    buffersPool.release(buffer);
    assertThat(buffersPool.getAllocatedBuffersCount()).isEqualTo(allocationsCount);
    assertThat(buffersPool.getUsedBuffersCount()).isZero();
  }

  @Test
  public void bufferizeShouldWrapInputStream() throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream("test".getBytes());
    buffersPool = spy(buffersPool);

    InputStream bufferedInput = buffersPool.bufferize(input);
    assertThat(bufferedInput).isNotSameAs(input);
    assertThat(bufferedInput.markSupported()).isTrue();

    bufferedInput.close();
    verify(buffersPool).get(anyInt());
    verify(buffersPool).release(any(byte[].class));
  }

  @Test
  public void bufferizeShouldWrapOutputStream() throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    buffersPool = spy(buffersPool);

    OutputStream bufferedOutput = buffersPool.bufferize(output);
    assertThat(bufferedOutput).isNotSameAs(output);

    bufferedOutput.close();
    verify(buffersPool).get(anyInt());
    verify(buffersPool).release(any(byte[].class));
  }


  @Test
  public void threadsTest() throws Exception {
    final int usersCount = 10;

    final ArrayList<UserThread> threads = new ArrayList<UserThread>(usersCount);
    for (int i = 0; i < usersCount; i++) {
      final UserThread t = new UserThread();
      threads.add(t);
      t.start();
    }

    for (final UserThread t : threads) {
      t.join();
      assertThat(t.error).isNull();
    }

    assertThat(buffersPool.getUsedBuffersCount()).isZero();
  }

  /** User thread. */
  private class UserThread extends Thread {

    /** Caught error. */
    private Throwable error;

    @Override
    public void run() {
      final int count = 100;
      byte[] buffer = null;

      try {

        for (int i = 0; i < count; i++) {
          if ((i & 1) == 0) {
            buffer = buffersPool.get(r.nextInt(maxAvailableSize * 3));
          } else {
            buffersPool.release(buffer);
          }
        }

      } catch (Throwable e) {
        error = e;
      }
    }
  }

}
