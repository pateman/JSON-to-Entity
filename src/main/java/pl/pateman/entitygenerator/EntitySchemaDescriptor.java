package pl.pateman.entitygenerator;

import java.util.Collection;
import java.util.Collections;

final class EntitySchemaDescriptor {

  private Collection<EntityDescriptor> entities;
  private Collection<EntityRelationDescriptor> relations;

  private EntitySchemaDescriptor() {

  }

  public Collection<EntityDescriptor> getEntities() {
    return this.entities == null ? Collections.emptyList() : Collections.unmodifiableCollection(entities);
  }

  public Collection<EntityRelationDescriptor> getRelations() {
    return this.relations == null ? Collections.emptyList() : Collections.unmodifiableCollection(this.relations);
  }
}
