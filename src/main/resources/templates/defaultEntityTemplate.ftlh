package ${packageName};

@Entity
@Table(name = "${entity.deployment}")
public class ${entity.name}Entity {

  <#list entity.attributes as attr>
  <#if attr.flags?seq_contains("PRIMARY_KEY")>
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  </#if>
  private ${attr.type.simpleName} ${attr.name};
  </#list>

  <#list entity.attributes as attr>
  <#assign capitalizedAttr = attr.name?cap_first>
  public ${attr.type.simpleName} get${capitalizedAttr}() {
    return this.${attr.name};
  }

  public void set${capitalizedAttr}(${attr.type.simpleName} value) {
    this.${attr.name} = value;
  }

  </#list>
}