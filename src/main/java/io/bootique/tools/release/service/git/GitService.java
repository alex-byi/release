package io.bootique.tools.release.service.git;

import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.service.preferences.Preference;

import java.nio.file.Path;

public interface GitService {

    Preference<Path> BASE_PATH_PREFERENCE = Preference.of("git.base-path", Path.class);

    void clone(Repository repository);

    GitStatus status(Repository repository);

    void update(Repository repository);

    void deleteTag(Repository repository, String releaseVersion);

    void addAndCommit(Repository repository);

    enum GitStatus {
        OK,
        NEED_UPDATE,
        MISSING
    }
}
