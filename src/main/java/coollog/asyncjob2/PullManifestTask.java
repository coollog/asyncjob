package coollog.asyncjob2;

import coollog.asyncjob2.api.AsyncTask;
import coollog.asyncjob2.api.SingleAsyncTask;

import java.util.List;

public class PullManifestTask extends SingleAsyncTask<List<Integer>> {

  protected PullManifestTask(List<AsyncTask<?>> dependencies) {
    super(dependencies);
  }

  @Override
  public void execute() {
    Main.waitForSomeTime();

    setResult(Main.MANIFEST_LAYERS);
  }
}
