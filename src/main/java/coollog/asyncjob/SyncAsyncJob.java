package coollog.asyncjob;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SyncAsyncJob<T> extends AsyncJob<T> {

  static <U> SyncAsyncJob<List<U>> newCollectJobFromList(
      List<U> list, Function<U, Supplier<U>> supplierFunction, Class<U> classOfU) {
    return newCollectJob(fromList(list, supplierFunction), classOfU);
  }

  static <U> List<AsyncJob<U>> fromList(List<U> list, Function<U, Supplier<U>> fn) {
    return list
        .stream()
        .map(item -> new SyncAsyncJob<>(fn.apply(item)))
        .collect(Collectors.toList());
  }

  static <U> SyncAsyncJob<List<U>> newCollectJob(
      List<AsyncJob<U>> dependencies, Class<U> classOfU) {
    SyncAsyncJob<List<U>> collectJob = new SyncAsyncJob<>(() ->
        dependencies
            .stream()
            .map(AsyncJob::getResultFor)
            .map(classOfU::cast)
            .collect(Collectors.toList()));
    collectJob.dependsOn(dependencies);
    return collectJob;
  }

  private final Supplier<T> supplier;

  SyncAsyncJob(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  protected Supplier<T> getSupplier() {
    return supplier;
  }
}
