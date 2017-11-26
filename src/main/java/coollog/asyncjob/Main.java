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
import java.util.stream.Collectors;

public class Main {

  private static final List<Integer> MANIFEST_LAYERS = new ArrayList<>();
  private static final List<Integer> CACHED_LAYERS = new ArrayList<>();

  public static void main(String[] args) {
    for (int i = 0; i < 20; i ++) {
      MANIFEST_LAYERS.add(i);
    }
    CACHED_LAYERS.addAll(Arrays.asList(3, 4, 7, 12, 14));

    /**
     * Pull manifest (has list of manifestLayers)
     *
     * Load manifestLayers from cache
     *
     * Pull manifestLayers
     *  dep Pull manifest
     *  dep Load manifestLayers from cache
     *
     * Put manifestLayers in image
     *  dep Pull manifestLayers
     */

    CompletableFuture<List<Integer>> pullManifest = CompletableFuture.supplyAsync(() -> {
      waitForSomeTime();
      return MANIFEST_LAYERS;
    });

    CompletableFuture<CompletableFuture<List<Integer>>> loadCachedLayers = CompletableFuture.supplyAsync(() -> {
      waitForSomeTime();

      List<CompletableFuture<Integer>> getLayerJobs = new ArrayList<>();

      for (int layerId : CACHED_LAYERS) {
        CompletableFuture<Integer> getLayer = CompletableFuture.supplyAsync(() -> {
          waitForSomeTime();
          return layerId;
        });
        getLayerJobs.add(getLayer);
      }

      CompletableFuture<Void> getLayerJobsAll = CompletableFuture.allOf(
          getLayerJobs.toArray(new CompletableFuture[getLayerJobs.size()]));

      return getLayerJobsAll.thenApplyAsync(voidObject ->
          getLayerJobs
              .stream()
              .map(CompletableFuture::join)
              .collect(Collectors.toList()));
    });

    CompletableFuture<CompletableFuture<List<Integer>>> pullLayers = CompletableFuture.supplyAsync(() -> {
      List<Integer> manifestLayers = pullManifest.join();
      CompletableFuture<List<Integer>> cachedLayersJob = loadCachedLayers.join();

      return cachedLayersJob.thenApplyAsync(cachedLayers -> {
        waitForSomeTime();

        return manifestLayers
            .stream()
            .filter(layer -> !cachedLayers.contains(layer))
            .collect(Collectors.toList());
      });
    });


    CompletableFuture<CompletableFuture<Void>> printLayers =
        pullLayers.thenApplyAsync(pullLayersJob ->
            pullLayersJob.thenAcceptAsync(layers -> {
              layers.forEach(System.out::println);
            }));

    printLayers.join().join();
  }

  private static void waitForSomeTime() {
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }
}
