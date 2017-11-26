package coollog.asyncjob;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/** A job that depends on another job that returned an AsyncJob. */
public class AsyncAsyncJob<T, R> extends AsyncJob<AsyncJob<R>> {

  private final Function<T, R> fn;

  /** A dependency to first join and then apply. */
  private CompletableFuture<AsyncJob<T>> dependencyFuture;

  AsyncAsyncJob(Function<T, R> fn) {
    this.fn = fn;
  }

  void run() {
    getFuture().join().run();
  }

  AsyncAsyncJob<T, R> applyToJob(SyncAsyncJob<AsyncJob<T>> other) throws Exception {
    if (dependencyFuture != null) {
      throw new Exception("Cannot apply to two wrapped jobs.");
    }
    dependsOn(other);
    dependencyFuture = other.getFuture();
    return this;
  }

  AsyncAsyncJob<T, R> applyToJob(AsyncAsyncJob<?, T> other) throws Exception {
    if (dependencyFuture != null) {
      throw new Exception("Cannot apply to two wrapped jobs.");
    }
    dependsOn(other);
    dependencyFuture = other.getFuture();
    return this;
  }

  protected Supplier<AsyncJob<R>> getSupplier() throws Exception {
    throw new Exception("Must call applyToJob on AsyncAsyncJob");
  }

  /** Wrap a dependency job that returns a future. */
  protected Supplier<AsyncJob<R>> wrapSupplier() throws Exception {
    if (dependencyFuture == null) {
      throw new Exception("Must call applyToJob on AsyncAsyncJob");
    }

    return () -> {
      AsyncJob<T> dependencyJob = dependencyFuture.join();
      AsyncJob<R> newJob = new SyncAsyncJob<>(() ->
          fn.apply((T) AsyncJob.getResultFor(dependencyJob)));
      return newJob.dependsOn(dependencyJob);
    };
  }
}
