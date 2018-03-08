package pl.pateman.entitygenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import pl.pateman.entitygenerator.EntityRelationSideDescriptor.Side;
import pl.pateman.entitygenerator.GeneratedEntity.Attribute;
import pl.pateman.entitygenerator.GeneratedEntity.RelationInfo;
import pl.pateman.entitygenerator.GeneratedEntity.RelationInfo.CollectionType;
import pl.pateman.entitygenerator.exception.EntityGeneratorException;

/**
 * Main entity generator class.
 *
 * It exposes a single public method called {@link EntityGenerator#generateEntities(Collection)} which takes a
 * {@link Collection<InputStream>} with input streams to all resources which should be processed.
 * Use {@link ClasspathEntitiesSchemaScanner} in order to obtain such a collection, or provide your own means of
 * generating the collection.
 *
 * A resource should be a valid JSON file that matches the schema described by the following classes:
 * {@link EntitySchemaDescriptor}, {@link EntityAttributeDescriptor}, {@link EntityRootDescriptor},
 * and {@link EntityDescriptor}.
 *
 * Note that this class does not generate the actual files, only metadata which could be used afterwards to turn
 * it into, for example, Java code.
 *
 * This class is thread-safe, provided the aforementioned collection of input streams is NOT shared between threads
 * (unless of course the collection itself is thread-safe).
 */
public final class EntityGenerator {

  private static final Type SCHEMA_DESCRIPTOR_TYPE = new TypeToken<EntitySchemaDescriptor>() {
  }.getType();

  private final Gson gson;

  public EntityGenerator() {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    this.gson = gsonBuilder.create();
  }

  /**
   * Validates the provided entity descriptor.
   *
   * @param entityDescriptor Entity descriptor which should be validated.
   * @throws EntityGeneratorException If there is a violation.
   */
  private void validateEntityDescriptor(final EntityDescriptor entityDescriptor) {
    final EntityRootDescriptor root = entityDescriptor.getRoot();

    if (StringUtils.isBlank(entityDescriptor.getName())) {
      throw new EntityGeneratorException("An entity needs a name");
    }

    if (root != null && StringUtils.isBlank(root.getName())) {
      throw new EntityGeneratorException("When defining a root, its name needs to be provided");
    }

    if (StringUtils.isBlank(entityDescriptor.getDeployment()) && root == null) {
      throw new EntityGeneratorException("An entity needs a valid deployment table");
    }

    if (StringUtils.isBlank(entityDescriptor.getClassFile()) && root == null) {
      throw new EntityGeneratorException("An entity needs a valid class file");
    }

    //  Check whether the attributes are all right. A valid attribute needs a name and a type.
    final Collection<EntityAttributeDescriptor> attributes = entityDescriptor.getAttributes();
    if (!attributes.isEmpty()) {
      for (final EntityAttributeDescriptor descriptor : attributes) {
        if (StringUtils.isBlank(descriptor.getName())) {
          throw new EntityGeneratorException("An entity attribute needs a name");
        }
        if (StringUtils.isBlank(descriptor.getType())) {
          throw new EntityGeneratorException("An entity attribute needs a type");
        }
      }
    }
  }

  /**
   * Validates the provided schema descriptor, then in turn calls
   * {@link EntityGenerator#validateEntityDescriptor(EntityDescriptor)} to validate each {@link EntityDescriptor}
   * in the schema.
   *
   * @param schemaDescriptor Schema descriptor to validate.
   * @throws EntityGeneratorException If there is a violation.
   */
  private void validateSchemaDescriptor(final EntitySchemaDescriptor schemaDescriptor) {
    if (schemaDescriptor.getEntities().isEmpty()) {
      throw new EntityGeneratorException("A schema descriptor needs at least one entity");
    }

    schemaDescriptor.getEntities().forEach(this::validateEntityDescriptor);
  }

  /**
   * Takes an input stream with the JSON schema, parses it, and validates using
   * {@link EntityGenerator#validateSchemaDescriptor(EntitySchemaDescriptor)}.
   *
   * @param schemaStream Input stream with the JSON schema to parse.
   * @return An {@link EntitySchemaDescriptor} with parsed entity descriptors and relations.
   * @throws EntityGeneratorException If there's a problem with reading the stream, or if validation fails.
   */
  private EntitySchemaDescriptor parseSchemaStream(final InputStream schemaStream) {
    try (final JsonReader jsonReader = new JsonReader(new InputStreamReader(schemaStream))) {

      final EntitySchemaDescriptor schemaDescriptor = this.gson.fromJson(jsonReader, SCHEMA_DESCRIPTOR_TYPE);
      this.validateSchemaDescriptor(schemaDescriptor);

      return schemaDescriptor;
    } catch (final IOException e) {
      throw new EntityGeneratorException("Unable to parse schema", e);
    } finally {
      try {
        schemaStream.close();
      } catch (IOException e) {
        throw new EntityGeneratorException("Unable to close the schema stream", e);
      }
    }
  }

  /**
   * Validates whether entity descriptors point to a correct root entity in case they're extending or inheriting from
   * another entity.
   *
   * @param entityDescriptorMap Map of entity descriptors to validate.
   * @throws EntityGeneratorException If there is a violation.
   */
  private void validateRoots(final Map<String, List<EntityDescriptor>> entityDescriptorMap) {
    entityDescriptorMap.forEach((k, v) -> v.forEach(ed -> {
      final EntityRootDescriptor root = ed.getRoot();
      if (root != null && !entityDescriptorMap.containsKey(root.getName())) {
        throw new EntityGeneratorException("Entity '" + k + "' references an unknown root '" + root.getName() + "'");
      }
    }));
  }

  /**
   * Checks whether there are duplicate entity descriptors.
   *
   * A duplicate is when there are two descriptors with the same name, but one is not a root entity of the other.
   *
   * @param entityDescriptorMap Map of entity descriptors to validate.
   * @throws EntityGeneratorException If there is a violation.
   */
  private void validateDuplicateEntities(final Map<String, List<EntityDescriptor>> entityDescriptorMap) {
    //  Locate entity descriptors that don't have a root defined and group them by name.
    final Map<String, List<EntityDescriptor>> entitiesWithoutARootOrExtension = entityDescriptorMap
        .entrySet()
        .stream()
        .flatMap(x -> x.getValue().stream())
        .filter(ed -> ed.getRoot() == null)
        .collect(Collectors.groupingBy(EntityDescriptor::getName));

    //  For each pair of name->descriptors, there must be only one descriptor - otherwise we've found a duplicate.
    entitiesWithoutARootOrExtension.forEach((name, descriptors) -> {
      if (descriptors.size() > 1) {
        throw new EntityGeneratorException("Duplicate '" + name + "' entity definition");
      }
    });
  }

  /**
   * Processes an entity descriptor and stores the result of the processing in the outcome map.
   *
   * What the processing is is basically turning an {@link EntityDescriptor} into a {@link GeneratedEntity} equivalent
   * and validating data in the process. A {@link GeneratedEntity} holds metadata about a certain entity and it is
   * final - after all descriptors have been processed, it can be used to generate actual source files.
   *
   * @param entityDescriptor Entity descriptor to process.
   * @param outcome A {@link Map<String, GeneratedEntity>} which holds the result of the processing.
   * @throws EntityGeneratorException If either any {@link EntityAttributeDescriptor#getType()} denotes a Java class
   * which couldn't be found or if there are duplicate attribute definitions.
   */
  private void processEntityDescriptor(final EntityDescriptor entityDescriptor,
      final Map<String, GeneratedEntity> outcome) {
    GeneratedEntity generatedEntity = outcome.getOrDefault(entityDescriptor.getName(), new GeneratedEntity());
    final Set<Attribute> attributes = new LinkedHashSet<>(generatedEntity.getAttributes());

    //  Check if the descriptor has a root.
    final EntityRootDescriptor root = entityDescriptor.getRoot();
    if (root != null) {
      final GeneratedEntity rootEntity = outcome.get(root.getName());

      //  It has, so we need to check whether we're extending the root (i.e. adding/modifying attributes), or creating
      //  an entity which inherits from it.
      if (!root.extendsRoot()) {
        generatedEntity.setRoot(rootEntity);
        attributes.addAll(rootEntity.getAttributes());
      } else if (rootEntity != null) {
        generatedEntity = rootEntity;
      }
    }

    if (StringUtils.isBlank(generatedEntity.getName())) {
      generatedEntity.setName(entityDescriptor.getName());
    }
    if (StringUtils.isBlank(generatedEntity.getDeployment())) {
      generatedEntity.setDeployment(entityDescriptor.getDeployment());
    }
    if (StringUtils.isBlank(generatedEntity.getClassFile())) {
      generatedEntity.setClassFile(entityDescriptor.getClassFile());
    }

    //  Convert attribute definitions into actual metadata representation.
    final Set<Attribute> generatedAttributes = entityDescriptor.getAttributes()
        .stream()
        .map(entityAttributeDescriptor -> {
          final Attribute attribute = new Attribute();
          attribute.setName(entityAttributeDescriptor.getName());
          try {
            attribute.setType(Class.forName(entityAttributeDescriptor.getType()));
          } catch (final ClassNotFoundException e) {
            throw new EntityGeneratorException(
                "Unable to locate class for attribute '" + entityAttributeDescriptor.getName() + "'", e);
          }

          //  Reintroducing an attribute means that another descriptor (which either extends or inherits from another
          //  entity) provides an alternative definition of an attribute which is present in the root definition.
          if (attributes.contains(attribute) && !entityAttributeDescriptor.isReintroduced()) {
            throw new EntityGeneratorException("Duplicate attribute '" + entityAttributeDescriptor.getName() + "'");
          }

          attribute.setReintroduced(entityAttributeDescriptor.isReintroduced());
          attribute.setFlags(entityAttributeDescriptor.getFlags());
          return attribute;
        })
        .sorted(Comparator.comparing(Attribute::getName))
        .collect(LinkedHashSet::new, HashSet::add, AbstractCollection::addAll);

    //  We need to replace existing attribute definitions with new ones just in case an attribute has been reintroduced.
    generatedAttributes.forEach(a -> {
      if (!attributes.add(a)) {
        attributes.remove(a);
        attributes.add(a);
      }
    });

    generatedEntity.setAttributes(attributes);
    outcome.put(entityDescriptor.getName(), generatedEntity);
  }

  private void validateRelationSideDescriptor(final EntityRelationSideDescriptor relationSideDescriptor) {
    if (StringUtils.isBlank(relationSideDescriptor.getEntity())) {
      throw new EntityGeneratorException("Invalid relation side definition. A valid entity name is required");
    }
    if (StringUtils.isBlank(relationSideDescriptor.getAttributeName())) {
      throw new EntityGeneratorException("Invalid relation side definition. A valid attribute name is required");
    }
    if (relationSideDescriptor.getSide() == null) {
      throw new EntityGeneratorException("Invalid relation side definition. A relation side is required");
    }
  }

  private void validateRelationDescriptorAttribute(final GeneratedEntity entity, final String attributeName) {
    final Optional<Attribute> existingAttribute = entity.findAttribute(a -> attributeName.equals(a.getName()));
    if (existingAttribute.isPresent()) {
      throw new EntityGeneratorException(
          "Invalid relation definition. Attribute '" + attributeName + "' is already defined on entity '" + entity
              .getName() + "'");
    }
  }

  private void validateRelationDescriptor(final EntityRelationDescriptor relationDescriptor,
      final Map<String, GeneratedEntity> generatedEntityMap) {
    final EntityRelationSideDescriptor source = relationDescriptor.getSource();
    final EntityRelationSideDescriptor target = relationDescriptor.getTarget();

    if (source == null || target == null) {
      throw new EntityGeneratorException("Invalid relation definition. Both source and target are required");
    }

    this.validateRelationSideDescriptor(source);
    this.validateRelationSideDescriptor(target);

    if (!generatedEntityMap.containsKey(source.getEntity())) {
      throw new EntityGeneratorException("Invalid relation definition. Source side points to an unknown entity");
    }
    if (!generatedEntityMap.containsKey(target.getEntity())) {
      throw new EntityGeneratorException("Invalid relation definition. Target side points to an unknown entity");
    }

    this.validateRelationDescriptorAttribute(generatedEntityMap.get(source.getEntity()), source.getAttributeName());
    this.validateRelationDescriptorAttribute(generatedEntityMap.get(target.getEntity()), target.getAttributeName());
  }

  private Attribute createRelationAttribute(final String attributeName,
      final EntityRelationSideDescriptor.CollectionType collectionType, final GeneratedEntity targetEntity,
      final String joinColumn, final String joinTable) {
    final Attribute attribute = new Attribute();
    attribute.setName(attributeName);
    attribute.setReintroduced(false);

    final RelationInfo relationInfo = new RelationInfo();
    relationInfo.setTarget(targetEntity);
    relationInfo.setJoinColumn(joinColumn);
    relationInfo.setJoinTable(joinTable);

    if (collectionType != null) {
      attribute.setType(EntityRelationSideDescriptor.CollectionType.LIST
          .equals(collectionType) ? List.class : Set.class);
      relationInfo.setCollectionType(EntityRelationSideDescriptor.CollectionType.LIST
          .equals(collectionType) ? CollectionType.LIST : CollectionType.SET);
    } else {
      //  TODO Needs refactoring
//      Class.fo
//      attribute.setType(targetEntity.);
    }

    attribute.setRelationInfo(relationInfo);
    return attribute;
  }

  private void processRelationDescriptors(final EntitySchemaDescriptor schemaDescriptor,
      final Map<String, GeneratedEntity> generatedEntities) {
    final Collection<EntityRelationDescriptor> relations = schemaDescriptor.getRelations();
    if (relations.isEmpty()) {
      return;
    }

    for (final EntityRelationDescriptor relation : relations) {
      this.validateRelationDescriptor(relation, generatedEntities);

      final GeneratedEntity sourceEntity = generatedEntities.get(relation.getSource().getEntity());
      final GeneratedEntity targetEntity = generatedEntities.get(relation.getTarget().getEntity());

      final List<Attribute> sourceAttribs = new ArrayList<>(sourceEntity.getAttributes());
      final List<Attribute> targetAttribs = new ArrayList<>(targetEntity.getAttributes());

      //  One -> Many relation.
      if (Side.ONE.equals(relation.getSource().getSide()) && Side.MANY.equals(relation.getTarget().getSide())) {
        final Attribute sourceAttrib = this
            .createRelationAttribute(relation.getSource().getAttributeName(), relation.getSource().getCollectionType(),
                targetEntity, relation.getJoinColumn(), relation.getJoinTable());
        final Attribute targetAttrib = this
            .createRelationAttribute(relation.getTarget().getAttributeName(), null, sourceEntity,
                relation.getJoinColumn(), relation.getJoinTable());

        sourceAttribs.add(sourceAttrib);
        targetAttribs.add(targetAttrib);
      }

      sourceEntity.setAttributes(sourceAttribs);
      targetEntity.setAttributes(targetAttribs);
    }
  }

  /**
   * Generates entity metadata from the given input streams of schema definitions.
   *
   * Each input stream is expected to be a JSON schema, which is then parsed, validated, and converted
   * into a {@link GeneratedEntity}. Refer to {@link EntityGenerator#parseSchemaStream(InputStream)} and
   * {@link EntityGenerator#processEntityDescriptor(EntityDescriptor, Map)} to learn more about the process.
   *
   * @param schemaStreams A collection of JSON schema input streams which should be processed.
   * @return A {@link Collection<GeneratedEntity>} of converted entity definitions.
   * @throws IllegalArgumentException If the JSON schema streams collection is either {@code null} or empty.
   * @throws EntityGeneratorException If there is a problem during the operation (for instance, validation fails).
   */
  public Collection<GeneratedEntity> generateEntities(final Collection<InputStream> schemaStreams) {
    if (schemaStreams == null || schemaStreams.isEmpty()) {
      throw new IllegalArgumentException("A valid schemas collection is required");
    }

    //  Parse schema streams first.
    final List<EntitySchemaDescriptor> schemaDescriptors = schemaStreams
        .stream()
        .map(this::parseSchemaStream)
        .collect(Collectors.toList());

    final Map<String, List<EntityDescriptor>> unsortedDescriptors = schemaDescriptors
        .stream()
        .flatMap(s -> s.getEntities().stream())
        .collect(Collectors.groupingBy(EntityDescriptor::getName));
    this.validateDuplicateEntities(unsortedDescriptors);

    //  This sorting ensures that entities with a root are processed AFTER their parents.
    final Map<String, List<EntityDescriptor>> entityDescriptors = unsortedDescriptors
        .entrySet()
        .stream()
        .sorted((a, b) -> a.getValue()
            .stream()
            .filter(x -> x.getRoot() == null)
            .findAny()
            .map(x -> -1)
            .orElse(1))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));
    if (entityDescriptors.isEmpty()) {
      return Collections.emptyList();
    }

    this.validateRoots(entityDescriptors);

    //  Now we need to process each entity descriptor and convert it to a generated entity.
    final Map<String, GeneratedEntity> generatedEntityMap = new HashMap<>(entityDescriptors.size());
    entityDescriptors.forEach((name, descriptors) -> descriptors
        .forEach(descriptor -> this.processEntityDescriptor(descriptor, generatedEntityMap)));

    //  Finally, process relations.
    schemaDescriptors.forEach(sd -> this.processRelationDescriptors(sd, generatedEntityMap));

    return generatedEntityMap.values();
  }
}
