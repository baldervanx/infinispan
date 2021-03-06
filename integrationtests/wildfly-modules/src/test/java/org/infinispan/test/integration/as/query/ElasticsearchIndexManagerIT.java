package org.infinispan.test.integration.as.query;

import org.elasticsearch.plugin.deletebyquery.DeleteByQueryPlugin;
import org.infinispan.Version;
import org.infinispan.query.test.elasticsearch.ElasticSearchCluster;
import org.infinispan.query.test.elasticsearch.ElasticSearchCluster.ElasticSearchClusterBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.se.manifest.ManifestDescriptor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @since 9.0
 */
@RunWith(Arquillian.class)
public class ElasticsearchIndexManagerIT extends BaseQueryTest {

   @Deployment(name = "dep.active-1")
   @TargetsContainer("container.active-1")
   public static Archive<?> createTestDeploymentOne() {
      return deployment();
   }

   @Deployment(name = "dep.active-2")
   @TargetsContainer("container.active-2")
   public static Archive<?> createTestDeploymentTwo() {
      return deployment();
   }

   private static Archive<?> deployment() {
      return ShrinkWrap.create(WebArchive.class)
            .addClasses(ElasticSearchCluster.class, BaseQueryTest.class, ElasticsearchIndexManagerIT.class,
                  ElasticQueryConfiguration.class, Book.class, GridService.class)
            .add(manifest(), "META-INF/MANIFEST.MF")
            .addAsResource("elasticsearch-indexing.xml")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   private static Asset manifest() {
      String manifest = Descriptors.create(ManifestDescriptor.class)
            .attribute("Dependencies",
                  "org.infinispan:" + Version.getModuleSlot() + " services, "
                        + "org.infinispan.query:" + Version.getModuleSlot() + " services")
            .exportAsString();
      return new StringAsset(manifest);
   }


   private static ElasticSearchCluster elasticSearchCluster;

   @BeforeClass
   @RunAsClient
   public static void startElastic() throws Exception {
      elasticSearchCluster = new ElasticSearchClusterBuilder()
            .withNumberNodes(2)
            .addPlugin(DeleteByQueryPlugin.class)
            .build();
      elasticSearchCluster.start();
   }

   @AfterClass
   @RunAsClient
   public static void stopElastic() throws Exception {
      if (elasticSearchCluster != null) {
         elasticSearchCluster.stop();
      }
   }

}
