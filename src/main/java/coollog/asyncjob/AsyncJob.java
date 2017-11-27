package coollog.asyncjob;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

abstract class AsyncJob<T> {
  /** A set of completed jobs. */
  private static final Set<AsyncJob<?>> completed = ConcurrentHashMap.newKeySet();

  private final Set<AsyncJob<?>> dependencies = new HashSet<>();

  /** Only valid if `dependencies.size() == 1`. */
  private AsyncJob<?> onlyDependency;

  private CompletableFuture<T> future;

  /** Save the result once obtained. */
  private T result;

  final T getResult() {
//    if (!completed.contains(this)) {
//      throw new Exception("Trying to get result of uncompleted job. " +
//          "Make sure to set dependencies correctly.");
//    }

    return result;
  }

  // TODO: Staticly define dependencies to make sure getting dependency results
  //       is checked at compile time.
  final AsyncJob<T> dependsOn(AsyncJob<?> other) {
    onlyDependency = other;
    dependencies.add(other);
    return this;
  }

  final <U> AsyncJob<T> dependsOn(List<AsyncJob<U>> others) {
    dependencies.addAll(others);
    return this;
  }

  void run() {
    getFuture().join();
  }

  final CompletableFuture<T> getFuture() {
    if (future == null) {
      switch (dependencies.size()) {
        case 0:
          future = CompletableFuture.supplyAsync(wrapMySupplier());
          break;
        case 1:
          future = onlyDependency.getFuture().thenApplyAsync(voidObject -> {
            try {
              return wrapSupplier().get();
            } catch (Exception ex) {
              throw new CompletionException(ex);
            }
          });
          break;
        default:
          List<CompletableFuture<?>> futureList =
              dependencies
                  .stream()
                  .map(AsyncJob::getFuture)
                  .collect(Collectors.toList());
          CompletableFuture<Void> futures = CompletableFuture.allOf(
              futureList.toArray(new CompletableFuture[futureList.size()]));

          return futures.thenApplyAsync(voidObject -> {
            try {
              return wrapSupplier().get();
            } catch (Exception ex) {
              throw new CompletionException(ex);
            }
          });
      }
    }
    return future;
  }

  // TODO: Replace Supplier with Runnable.
  protected abstract Supplier<T> getSupplier() throws Exception;

  protected Supplier<T> wrapSupplier() throws Exception {
    return wrapMySupplier();
  }

  final protected Supplier<T> wrapMySupplier() {
    return () -> {
      try {
        result = getSupplier().get();
        completed.add(this);
        return result;
      } catch (Exception ex) {
        throw new CompletionException(ex);
      }
    };
  }
}
