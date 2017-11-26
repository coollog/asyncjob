package coollog.asyncjob;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/** A job that depends on another job that returned a CompletableFuture. */
public class AsyncAsyncJob<T, R> extends AsyncJob<CompletableFuture<R>> {

  private final Function<T, R> fn;

  /** A dependency ot first join and then apply. */
  private SyncAsyncJob<CompletableFuture<T>> wrappedDependency;

  AsyncAsyncJob(Function<T, R> fn) {
    this.fn = fn;
  }

  void run() {
    getFuture().join().join();
  }

  void applyToJob(SyncAsyncJob<CompletableFuture<T>> other) throws Exception {
    if (wrappedDependency != null) {
      throw new Exception("Cannot apply to two wrapped jobs.");
    }
    dependsOn(other);
    wrappedDependency = other;
  }

  protected Supplier<CompletableFuture<R>> getSupplier() throws Exception {
    throw new Exception("Must call applyToJob on AsyncAsyncJob");
  }

  /** Wrap a dependency job that returns a future. */
  protected Supplier<CompletableFuture<R>> wrapSupplier() throws Exception {
    if (wrappedDependency == null) {
      throw new Exception("Must call applyToJob on AsyncAsyncJob");
    }

    return () -> {
      CompletableFuture<T> dependencyFuture = wrappedDependency.getFuture().join();
      return dependencyFuture.thenApplyAsync(fn);
    };
  }
}
