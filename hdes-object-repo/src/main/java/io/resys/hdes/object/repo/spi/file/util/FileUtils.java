package io.resys.hdes.object.repo.spi.file.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.resys.hdes.object.repo.api.ObjectRepository.IsName;
import io.resys.hdes.object.repo.api.ObjectRepository.IsObject;
import io.resys.hdes.object.repo.spi.file.FileObjectRepository;
import io.resys.hdes.object.repo.spi.file.exceptions.FileCantBeWrittenException;

public class FileUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileObjectRepository.class);
  private static final String REPO_PATH = "repo";
  private static final String HEAD_PATH = "heads";
  private static final String OBJECTS_PATH = "objects";
  private static final String TAGS_PATH = "tags";

  @Value.Immutable
  public interface FileSystemConfig {
    File getRepo();
    File getHeads();
    File getObjects();
    File getTags();
  }

  @FunctionalInterface
  public interface FileReader<T> {
    T read(String id, byte[] content);
  }

  public static File mkdir(File src) {
    if (src.exists()) {
      return isWritable(src);
    }
    if (src.mkdir()) {
      return src;
    }
    throw new FileCantBeWrittenException(src);
  }

  public static File mkFile(File src) throws IOException {
    if (src.exists()) {
      return isWritable(src);
    }
    if (src.createNewFile()) {
      return src;
    }
    throw new FileCantBeWrittenException(src);
  }

  public static File isWritable(File src) {
    if (src.canWrite()) {
      return src;
    }
    throw new FileCantBeWrittenException(src);
  }

  public static String getCanonicalNameOrName(File file) {
    try {
      return file.getCanonicalPath();
    } catch (Exception e) {
      return file.getName();
    }
  }

  public static FileSystemConfig createOrGetRepo(File root) throws IOException {
    FileUtils.isWritable(root);
    StringBuilder log = new StringBuilder("Using file based storage. ");
    File repo = new File(root, REPO_PATH);
    if (repo.exists()) {
      log.append("Using existing repo: ");
    } else {
      FileUtils.mkdir(repo);
      log.append("No existing repo, init new: ");
    }
    FileUtils.isWritable(repo);
    File heads = FileUtils.mkdir(new File(repo, HEAD_PATH));
    File objects = FileUtils.mkdir(new File(repo, OBJECTS_PATH));
    File tags = FileUtils.mkdir(new File(repo, TAGS_PATH));
    
    log.append(System.lineSeparator())
        .append("  - ").append(repo.getAbsolutePath()).append(System.lineSeparator())
        .append("  - ").append(heads.getAbsolutePath()).append(System.lineSeparator())
        .append("  - ").append(objects.getAbsolutePath()).append(System.lineSeparator())
        .append("  - ").append(tags.getAbsolutePath()).append(System.lineSeparator());
    LOGGER.debug(log.toString());
    return ImmutableFileSystemConfig.builder()
        .heads(heads)
        .repo(repo)
        .objects(objects)
        .tags(tags)
        .build();
  }

  public static <T> Map<String, T> map(File dir, FileReader<T> consumer) throws IOException {
    Map<String, T> result = new HashMap<>();
    for (File subDir : dir.listFiles()) {
      if (subDir.isDirectory()) {
        for (File resourceFile : subDir.listFiles()) {
          String id = subDir.getName() + resourceFile.getName();
          T object = consumer.read(id, Files.readAllBytes(resourceFile.toPath()));
          result.put(((IsObject) object).getId(), object);
        }
      } else {
        String id = subDir.getName();
        T object = consumer.read(id, Files.readAllBytes(subDir.toPath()));
        result.put(((IsName) object).getName(), object);   
      }
    }
    return result;
  }
}