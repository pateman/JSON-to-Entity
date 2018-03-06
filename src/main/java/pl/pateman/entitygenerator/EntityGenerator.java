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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import pl.pateman.entitygenerator.GeneratedEntity.Attribute;

public final class EntityGenerator {

  private static final Type SCHEMA_DESCRIPTOR_TYPE = new TypeToken<EntitySchemaDescriptor>() {
  }.getType();

  private final Gson gson;

  public EntityGenerator() {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    this.gson = gsonBuilder.create();
  }

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

  private void validateSchemaDescriptor(final EntitySchemaDescriptor schemaDescriptor) {
    if (schemaDescriptor.getEntities().isEmpty()) {
      throw new EntityGeneratorException("A schema descriptor needs at least one entity");
    }

    schemaDescriptor.getEntities().forEach(this::validateEntityDescriptor);
  }

  private Collection<EntityDescriptor> generateEntitiesFromSchema(final InputStream schemaStream) {
    try (final JsonReader jsonReader = new JsonReader(new InputStreamReader(schemaStream))) {

      final EntitySchemaDescriptor schemaDescriptor = this.gson.fromJson(jsonReader, SCHEMA_DESCRIPTOR_TYPE);
      this.validateSchemaDescriptor(schemaDescriptor);

      return new ArrayList<>(schemaDescriptor.getEntities());
    } catch (final IOException e) {
      throw new EntityGeneratorException("Unable to generate entities from schema", e);
    }
  }

  private void validateRoots(final Map<String, EntityDescriptor> entityDescriptorMap) {
    entityDescriptorMap.forEach((k, v) -> {
      final EntityRootDescriptor root = v.getRoot();
      if (root != null && !entityDescriptorMap.containsKey(root.getName())) {
        throw new EntityGeneratorException("Entity '" + k + "' references an unknown root '" + root.getName() + "'");
      }
    });
  }

  private void processEntityDescriptor(final EntityDescriptor entityDescriptor,
      final Map<String, GeneratedEntity> outcome) {
    GeneratedEntity generatedEntity = outcome.getOrDefault(entityDescriptor.getName(), new GeneratedEntity());

    final Set<Attribute> attributes = new LinkedHashSet<>(generatedEntity.getAttributes());
    final EntityRootDescriptor root = entityDescriptor.getRoot();
    if (root != null) {
      final GeneratedEntity rootEntity = outcome.get(root.getName());

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

          if (attributes.contains(attribute) && !entityAttributeDescriptor.isReintroduced()) {
            throw new EntityGeneratorException("Duplicate attribute '" + entityAttributeDescriptor.getName() + "'");
          }

          attribute.setReintroduced(entityAttributeDescriptor.isReintroduced());
          attribute.setFlags(entityAttributeDescriptor.getFlags());
          return attribute;
        })
        .sorted(Comparator.comparing(Attribute::getName))
        .collect(LinkedHashSet::new, HashSet::add, AbstractCollection::addAll);
    generatedAttributes.forEach(a -> {
      if (!attributes.add(a)) {
        attributes.remove(a);
        attributes.add(a);
      }
    });

    generatedEntity.setAttributes(attributes);
    outcome.put(entityDescriptor.getName(), generatedEntity);
  }

  public Collection<GeneratedEntity> generateEntities(final Collection<InputStream> schemaStreams) {
    if (schemaStreams == null || schemaStreams.isEmpty()) {
      throw new IllegalArgumentException("A valid schemas collection is required");
    }

    //  TODO Validate duplicate entity names.
    final Map<String, EntityDescriptor> unsorted = schemaStreams
        .stream()
        .flatMap(stream -> this.generateEntitiesFromSchema(stream).stream())
        .collect(HashMap::new, (map, item) -> {
          final String k = map.containsKey(item.getName()) ?
              item.getName() + "_" + ThreadLocalRandom.current().nextLong() : item.getName();
          map.put(k, item);
        }, HashMap::putAll);

    //  This sorting ensures that entities with a root are processed AFTER their parents.
    final Map<String, EntityDescriptor> entityDescriptors = unsorted
        .entrySet()
        .stream()
        .sorted((a, b) -> a.getValue().getRoot() == null ? -1 : ((a.getValue().getRoot().extendsRoot() ? -1 : 1)))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    if (entityDescriptors.isEmpty()) {
      return Collections.emptyList();
    }

    this.validateRoots(entityDescriptors);

    final Map<String, GeneratedEntity> generatedEntityMap = new HashMap<>(entityDescriptors.size());
    entityDescriptors.forEach((name, descriptor) -> this.processEntityDescriptor(descriptor, generatedEntityMap));

    return generatedEntityMap.values();
  }
}
