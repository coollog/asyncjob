package coollog.asyncjob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Main {

  private static final List<Integer> MANIFEST_LAYERS = new ArrayList<>();
  private static final List<Integer> CACHED_LAYERS = new ArrayList<>();

  public static void main(String[] args) throws Exception {
    for (int i = 0; i < 20; i ++) {
      MANIFEST_LAYERS.add(i);
    }
    CACHED_LAYERS.addAll(Arrays.asList(3, 4, 7, 12, 14));

    doWithJobs();
  }

  private static void doWithJobs() throws Exception {
    SyncAsyncJob<Boolean> setUpJob = new SyncAsyncJob<>(Main::someFunction);

    SyncAsyncJob<List<Integer>> pullManifestJob = new SyncAsyncJob<>(() -> {
      waitForSomeTime();

      System.out.println("SETUP JOB: " + AsyncJob.getResultFor(setUpJob));

      return MANIFEST_LAYERS;
    });
    pullManifestJob.dependsOn(setUpJob);

    SyncAsyncJob<AsyncJob<List<Integer>>> loadCachedLayersJob = new SyncAsyncJob<>(() -> {
      waitForSomeTime();

      return SyncAsyncJob.newCollectJobFromList(CACHED_LAYERS, layerId -> () -> {
        waitForSomeTime();
        return layerId;
      }, Integer.class);
    });

    AsyncAsyncJob<List<Integer>, List<Integer>> pullLayersJob = new AsyncAsyncJob<>(cachedLayers -> {
      List<Integer> manifestLayers = (List<Integer>) AsyncJob.getResultFor(pullManifestJob);

      waitForSomeTime();

      return manifestLayers
          .stream()
          .filter(layer -> !cachedLayers.contains(layer))
          .collect(Collectors.toList());
    });
    pullLayersJob.applyToJob(loadCachedLayersJob).dependsOn(pullManifestJob);

    AsyncAsyncJob<List<Integer>, Integer> printPulledLayersJob = new AsyncAsyncJob<>(layers -> {
      layers.forEach(layer -> {
        System.out.println("LAYER " + layer);
      });
      return 0;
    });
    printPulledLayersJob.applyToJob(pullLayersJob);

    printPulledLayersJob.run();
  }

  private static boolean someFunction() {
    waitForSomeTime();
    return true;
  }
  
  private static void waitForSomeTime() {
    try {
      TimeUnit.SECONDS.sleep((long) (Math.random() * 2));
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }
}
