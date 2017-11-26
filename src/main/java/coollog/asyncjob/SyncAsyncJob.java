package coollog.asyncjob;

import java.util.function.Supplier;

public class SyncAsyncJob<T> extends AsyncJob<T> {

  private final Supplier<T> supplier;

  SyncAsyncJob(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  protected Supplier<T> getSupplier() {
    return supplier;
  }
}
