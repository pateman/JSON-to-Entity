package pl.pateman.entitygenerator;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ClasspathEntitiesSchemaScannerTest {

  private ClasspathEntitiesSchemaScanner schemaScanner;

  @Before
  public void initializeSchemaScanner() {
    this.schemaScanner = new ClasspathEntitiesSchemaScanner();
  }

  @Test
  public void findEntitySchemas() {
    final Collection<String> entitySchemas = this.schemaScanner.findEntitySchemas();
    Assert.assertFalse(entitySchemas.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void retrieveInputStreamsToResourcesNullCollection() {
    this.schemaScanner.retrieveInputStreamsToResources(null);
  }

  @Test
  public void retrieveInputStreamsToResources() {
    final Collection<String> entitySchemas = this.schemaScanner.findEntitySchemas();
    final Collection<InputStream> inputStreams = this.schemaScanner.retrieveInputStreamsToResources(entitySchemas);

    Assert.assertFalse(inputStreams.isEmpty());
    Assert.assertEquals(entitySchemas.size(), inputStreams.size());
  }

  @Test(expected = EntityGeneratorException.class)
  public void retrieveInputStreamToResourcesInvalidPaths() {
    this.schemaScanner.retrieveInputStreamsToResources(Collections.singletonList("test-entities.json"));
  }
}