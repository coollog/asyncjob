package coollog.asyncjob2.api;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/** Tests for {@link SingleAsyncTask}. */
public class SingleAsyncTaskTest {

  private static class LatchedAsyncTask extends SingleAsyncTask<Void> {

    private int position;

    private final AtomicInteger counter;

    private final CountDownLatch latch = new CountDownLatch(1);

    private LatchedAsyncTask(AtomicInteger counter, List<AsyncTask<?>> dependencies) {
      super(dependencies);
      this.counter = counter;
    }

    /** Allows the task to finish running. */
    private void unlatch() {
      latch.countDown();
    }

    @Override
    public void execute() {
      try {
        latch.await();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
        throw new CompletionException(ex);
      }
      position = counter.getAndIncrement();
    }
  }

  @Test
  public void testRun_correctOrder() {
    AtomicInteger counter = new AtomicInteger(1);

    LatchedAsyncTask task1 = new LatchedAsyncTask(counter, Collections.emptyList());
    LatchedAsyncTask task2 = new LatchedAsyncTask(counter, Collections.emptyList());

    LatchedAsyncTask task3 = new LatchedAsyncTask(counter, Arrays.asList(task1));

    LatchedAsyncTask task4 = new LatchedAsyncTask(counter, Arrays.asList(task3, task2));

    LatchedAsyncTask task5 = new LatchedAsyncTask(counter, Arrays.asList(task2));

    LatchedAsyncTask task6 = new LatchedAsyncTask(counter, Arrays.asList(task1, task2, task3, task4, task5));

    task6.getFuture();

    task6.unlatch();
    task5.unlatch();
    task4.unlatch();
    task3.unlatch();
    task2.unlatch();
    task1.unlatch();

    task6.getFuture().join();

    Assert.assertTrue(task3.position > task1.position);
    Assert.assertTrue(task4.position > task2.position);
    Assert.assertTrue(task4.position > task3.position);
    Assert.assertTrue(task5.position > task2.position);
    Assert.assertTrue(task6.position > task1.position);
    Assert.assertTrue(task6.position > task2.position);
    Assert.assertTrue(task6.position > task3.position);
    Assert.assertTrue(task6.position > task4.position);
    Assert.assertTrue(task6.position > task5.position);
  }
}
