package pl.pateman.entitygenerator;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.pateman.entitygenerator.GeneratedEntity.Attribute;
import pl.pateman.entitygenerator.exception.EntityGeneratorException;

public class EntityGeneratorTest {

  private Collection<GeneratedEntity> generatedEntities;
  private ClasspathEntitiesSchemaScanner classpathEntitiesSchemaScanner;

  private GeneratedEntity findBy(final Predicate<GeneratedEntity> predicate) {
    return this.generatedEntities.stream().filter(predicate).findFirst().orElse(null);
  }

  @Before
  public void initializeTestData() {
    this.classpathEntitiesSchemaScanner = new ClasspathEntitiesSchemaScanner();
    final Collection<String> entitySchemas = this.classpathEntitiesSchemaScanner.findEntitySchemas();
    final Collection<InputStream> streams = this.classpathEntitiesSchemaScanner
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

  @Test
  public void generateEntitiesValidEntityExtension() {
    final GeneratedEntity userEntity = this.findBy(e -> "User".equals(e.getName()));
    final Optional<Attribute> dateOfBirthAttrib = userEntity.getAttributes()
        .stream()
        .filter(attribute -> "dateOfBirth".equals(attribute.getName()))
        .findFirst();
    Assert.assertTrue(dateOfBirthAttrib.isPresent());
  }

  @Test
  public void generateEntitiesValidReintroduction() {
    final GeneratedEntity userEntity = this.findBy(e -> "User".equals(e.getName()));
    final Attribute dateOfBirthAttrib = userEntity.getAttributes()
        .stream()
        .filter(attribute -> "dateOfBirth".equals(attribute.getName()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Missing attribute 'dateOfBirth'"));
    Assert.assertTrue(dateOfBirthAttrib.isReintroduced());
    Assert.assertEquals(LocalDate.class, dateOfBirthAttrib.getType());
  }

  @Test(expected = EntityGeneratorException.class)
  public void generateEntitiesDuplicateAttributes() {
    final Collection<String> duplicateAttribSchema = this.classpathEntitiesSchemaScanner
        .findEntitySchemas("entitiesWithDuplicateAttributes\\.json");
    final Collection<InputStream> inputStreams = this.classpathEntitiesSchemaScanner
        .retrieveInputStreamsToResources(duplicateAttribSchema);
    new EntityGenerator().generateEntities(inputStreams);
  }

  @Test(expected = EntityGeneratorException.class)
  public void generateEntitiesDuplicateEntities() {
    final Collection<String> duplicateEntitySchema = this.classpathEntitiesSchemaScanner
        .findEntitySchemas("duplicatedEntities1\\.json");
    final Collection<InputStream> inputStreams = this.classpathEntitiesSchemaScanner
        .retrieveInputStreamsToResources(duplicateEntitySchema);
    new EntityGenerator().generateEntities(inputStreams);
  }

  @Test(expected = EntityGeneratorException.class)
  public void generateEntitiesNoDeployment() {
    final Collection<String> duplicateEntitySchema = this.classpathEntitiesSchemaScanner
        .findEntitySchemas("entitiesWithoutDeployment\\.json");
    final Collection<InputStream> inputStreams = this.classpathEntitiesSchemaScanner
        .retrieveInputStreamsToResources(duplicateEntitySchema);
    new EntityGenerator().generateEntities(inputStreams);
  }

}