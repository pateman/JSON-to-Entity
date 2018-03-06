package pl.pateman.entitygenerator;

import java.io.InputStream;
import java.util.Collection;
import java.util.function.Predicate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EntityGeneratorTest {

  private Collection<GeneratedEntity> generatedEntities;

  private GeneratedEntity findBy(final Predicate<GeneratedEntity> predicate) {
    return this.generatedEntities.stream().filter(predicate).findFirst().orElse(null);
  }

  @Before
  public void initializeTestData() {
    final ClasspathEntitiesSchemaScanner classpathEntitiesSchemaScanner = new ClasspathEntitiesSchemaScanner();
    final Collection<String> entitySchemas = classpathEntitiesSchemaScanner.findEntitySchemas();
    final Collection<InputStream> streams = classpathEntitiesSchemaScanner
        .retrieveInputStreamsToResources(entitySchemas);
    this.generatedEntities = new EntityGenerator().generateEntities(streams);
  }

  @Test
  public void generateEntities() {
    Assert.assertFalse(this.generatedEntities.isEmpty());
    Assert.assertEquals(3, this.generatedEntities.size());
  }

  @Test
  public void generateEntitiesRoots() {
    final GeneratedEntity userEntity = this.findBy(e -> "User".equals(e.getName()));
    final GeneratedEntity customerEntity = this.findBy(e -> "Customer".equals(e.getName()));
    final GeneratedEntity employeeEntity = this.findBy(e -> "Employee".equals(e.getName()));

    Assert.assertNull(userEntity.getRoot());
    Assert.assertNotNull(customerEntity.getRoot());
    Assert.assertEquals(userEntity, customerEntity.getRoot());
    Assert.assertEquals(userEntity, employeeEntity.getRoot());
  }

}