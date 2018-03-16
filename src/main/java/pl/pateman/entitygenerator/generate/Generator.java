package pl.pateman.entitygenerator.generate;

import java.util.Map;
import pl.pateman.entitygenerator.GeneratedEntity;

/**
 * An interface which defines the functionality of a source code generator.
 */
public interface Generator {

  /**
   * Generates the source code for the given entity.
   *
   * @param generatedEntity Entity to generate the source code for.
   * @param parameters Parameters which could be used for generating the source code.
   * @throws Exception An exception could be thrown by the generator to indicate that something went wrong.
   * @return A string literal which contains the source code.
   */
  String generateSource(GeneratedEntity generatedEntity, Map<String, Object> parameters) throws Exception;
}
