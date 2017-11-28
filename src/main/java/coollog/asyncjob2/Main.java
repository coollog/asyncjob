package coollog.asyncjob2;

import coollog.asyncjob2.api.AsyncTask;
import coollog.asyncjob2.api.AsyncTaskBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

  static final List<Integer> MANIFEST_LAYERS = new ArrayList<>();
  static final List<Integer> CACHED_LAYERS = new ArrayList<>();

  static void waitForSomeTime() {
    try {
      TimeUnit.SECONDS.sleep((long) (Math.random() * 2));
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  public static void main(String[] args) {
    AsyncTask<Void> setUpTask =
        AsyncTaskBuilder.newSingleAsyncTask(SetUpTask.class);

    AsyncTask<List<Integer>> pullManifestTask =
        AsyncTaskBuilder.newSingleAsyncTask(PullManifestTask.class, setUpTask);

    pullManifestTask.run();
    pullManifestTask.getResult().forEach(System.out::println);

    // TODO: Add the other tasks too.
  }
}
