package pl.pateman.entitygenerator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import pl.pateman.entitygenerator.exception.EntityGeneratorException;

/**
 * Utility class which allows to scan the classpath to locate entity definitions.
 *
 * This class is thread-safe.
 *
 * @author Patryk Nusbaum
 */
public final class ClasspathEntitiesSchemaScanner {

  private static final String DEFAULT_SCHEMAS_PATTERN = ".*-entities\\.json";
  private final Reflections reflections;

  public ClasspathEntitiesSchemaScanner() {
    this.reflections = new Reflections(null, new ResourcesScanner());
  }

  /**
   * Finds entity schemas using the default pattern {@link ClasspathEntitiesSchemaScanner#DEFAULT_SCHEMAS_PATTERN}.
   *
   * @return A {@link Collection<String>} of matching resource paths or an empty {@link List<String>} if nothing was
   * found.
   */
  public Collection<String> findEntitySchemas() {
    return this.findEntitySchemas(DEFAULT_SCHEMAS_PATTERN);
  }

  /**
   * Finds entity schemas that match the given Regex pattern.
   *
   * @param pattern Pattern used for matching.
   * @throws IllegalArgumentException if the provided pattern is empty.
   * @return A {@link Collection<String>} of matching resource paths or an empty {@link List<String>} if nothing was
   * found.
   */
  public Collection<String> findEntitySchemas(final String pattern) {
    if (StringUtils.isBlank(pattern)) {
      throw new IllegalArgumentException("A valid pattern is required");
    }
    return this.reflections.getResources(Pattern.compile(pattern));
  }

  /**
   * Retrieves input streams to the provided resource paths.
   *
   * @param resourcePaths A {@link Collection<String>} of resource paths that streams should be retrieved for.
   * @throws IllegalArgumentException If the given resource paths' collection is {@code null}.
   * @throws EntityGeneratorException If an input stream for any given path could not be found.
   * @return A {@code Collection<InputStream>} of input streams to the provided resource paths.
   */
  public Collection<InputStream> retrieveInputStreamsToResources(final Collection<String> resourcePaths) {
    if (resourcePaths == null) {
      throw new IllegalArgumentException("A valid resourcePaths collection is required");
    }

    final ClassLoader classLoader = this.getClass().getClassLoader();
    final List<InputStream> inputStreams = new ArrayList<>(resourcePaths.size());

    for (final String resourcePath : resourcePaths) {
      final InputStream resource = classLoader.getResourceAsStream(resourcePath);
      if (resource == null) {
        throw new EntityGeneratorException("Unable to open an InputStream for '" + resourcePath + "'");
      }
      inputStreams.add(resource);
    }

    return inputStreams;
  }
}
