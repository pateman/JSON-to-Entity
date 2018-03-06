package pl.pateman.entitygenerator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

public final class ClasspathEntitiesSchemaScanner {

  private final Reflections reflections;

  public ClasspathEntitiesSchemaScanner() {
    this.reflections = new Reflections(null, new ResourcesScanner());
  }

  public Collection<String> findEntitySchemas() {
    return this.reflections.getResources(Pattern.compile(".*-entities\\.json"));
  }

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
