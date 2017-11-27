package coollog.asyncjob2;

import coollog.asyncjob2.api.AsyncTask;
import coollog.asyncjob2.api.SingleAsyncTask;

import java.util.Arrays;
import java.util.List;

public class SetUpTask extends SingleAsyncTask<Void> {

  protected SetUpTask(List<AsyncTask<?>> dependencies) {
    super(dependencies);
  }

  @Override
  public void execute() {
    for (int i = 0; i < 20; i ++) {
      Main.MANIFEST_LAYERS.add(i);
    }
    Main.CACHED_LAYERS.addAll(Arrays.asList(3, 4, 7, 12, 14));
  }
}
