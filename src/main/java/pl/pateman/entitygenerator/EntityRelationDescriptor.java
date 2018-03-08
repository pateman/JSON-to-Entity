package pl.pateman.entitygenerator;

final class EntityRelationDescriptor {

  private EntityRelationSideDescriptor source;
  private EntityRelationSideDescriptor target;
  private String joinTable;
  private String joinColumn;

  private EntityRelationDescriptor() {

  }

  public EntityRelationSideDescriptor getSource() {
    return source;
  }

  public EntityRelationSideDescriptor getTarget() {
    return target;
  }

  public String getJoinTable() {
    return joinTable;
  }

  public String getJoinColumn() {
    return joinColumn;
  }
}
