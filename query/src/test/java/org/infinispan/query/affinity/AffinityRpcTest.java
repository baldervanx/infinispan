package org.infinispan.query.affinity;

import static java.text.MessageFormat.format;
import static java.util.Arrays.stream;
import static org.infinispan.hibernate.search.spi.InfinispanIntegration.DEFAULT_INDEXESDATA_CACHENAME;
import static org.infinispan.hibernate.search.spi.InfinispanIntegration.DEFAULT_INDEXESMETADATA_CACHENAME;
import static org.infinispan.hibernate.search.spi.InfinispanIntegration.DEFAULT_LOCKING_CACHENAME;
import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.infinispan.Cache;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.test.TestingUtil;
import org.testng.annotations.Test;

/**
 * @since 9.0
 */
@Test(groups = "functional", testName = "query.AffinityRpcTest")
public class AffinityRpcTest extends BaseAffinityTest {

   @Override
   protected int getNumOwners() {
      return 1;
   }

   public void shouldAvoidRpcsDuringIndexing() throws Exception {
      String[] indexCaches = {DEFAULT_INDEXESDATA_CACHENAME, DEFAULT_INDEXESMETADATA_CACHENAME, DEFAULT_LOCKING_CACHENAME};

      RpcCollector rpcCollector = new RpcCollector();

      // Load some data to the cache before starting the recording.
      // due to HSEARCH-2522
      populate(1, 10);

      cacheManagers.stream()
            .flatMap(cm -> cm.getCacheNames().stream().map(cm::getCache))
            .forEach(cache -> replaceRpcManager(cache, rpcCollector));

      waitForClusterToForm(indexCaches);

      populate(1, 500);

      stream(indexCaches).forEach(c -> assertNoRPCs(c, rpcCollector));
   }

   private void assertNoRPCs(String cacheName, RpcCollector rpcCollector) {
      Set<RpcDetail> rpcsForCache = rpcCollector.getRpcsForCache(cacheName);
      int numRpcs = rpcsForCache.size();
      assertEquals(numRpcs, 0, format("Cache {0} has done {1} Rpcs", cacheName, numRpcs));
   }

   private void replaceRpcManager(Cache<?, ?> cache, RpcCollector rpcCollector) {
      RpcManager current = cache.getAdvancedCache().getRpcManager();
      RpcManager replacement = new TrackingRpcManager(current, rpcCollector, cache.getName());
      TestingUtil.replaceComponent(cache, RpcManager.class, replacement, true);
   }

}
