package pl.pateman.entitygenerator;

final class EntityRootDescriptor {

  private String name;
  private Boolean extend;

  private EntityRootDescriptor() {

  }

  public String getName() {
    return name;
  }

  public boolean extendsRoot() {
    return this.extend == null ? true : this.extend;
  }
}
