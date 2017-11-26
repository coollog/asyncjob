package coollog.asyncjob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Main {

  private static List<Integer> manifestLayers = new ArrayList<>();
  private static List<Integer> cachedLayers = new ArrayList<>();

  {
    for (int i = 0; i < 20; i ++) {
      manifestLayers.add(i);
    }
    manifestLayers.addAll(Arrays.asList(3, 4, 7, 12, 14));
  }


  public static void main(String[] args) {
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
      return manifestLayers;
    });

    pullManifest.thenAcceptAsync((layers) -> {
      layers.forEach(Main::printLayer);
    });
  }

  private static void printLayer(Integer layerId) {
    System.out.println(layerId);
  }

  private static void waitForSomeTime() {
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }
}
