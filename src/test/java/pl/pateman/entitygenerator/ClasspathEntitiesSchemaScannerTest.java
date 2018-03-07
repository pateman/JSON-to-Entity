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

  @Test
  public void findEntitySchemasWithPattern() {
    final Collection<String> entitySchemas = this.schemaScanner.findEntitySchemas("core-entities\\.json");
    Assert.assertEquals(1, entitySchemas.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void findEntitySchemasWithNullPattern() {
    this.schemaScanner.findEntitySchemas(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void findEntitySchemasWithEmptyPattern() {
    this.schemaScanner.findEntitySchemas(" ");
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