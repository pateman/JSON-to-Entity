package pl.pateman.entitygenerator;

final class EntityRelationSideDescriptor {

  private String entity;
  private String attributeName;
  private Side side;
  private CollectionType collectionType;

  private EntityRelationSideDescriptor() {

  }

  public String getEntity() {
    return entity;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public Side getSide() {
    return side;
  }

  public CollectionType getCollectionType() {
    return collectionType;
  }

  enum Side {
    ONE,
    MANY
  }

  enum CollectionType {
    LIST,
    SET
  }
}
