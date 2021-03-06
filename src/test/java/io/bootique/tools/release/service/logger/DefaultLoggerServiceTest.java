package io.bootique.tools.release.service.logger;

import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Module;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TempDirectory.class)
class DefaultLoggerServiceTest {

    private DefaultLoggerService loggerService;
    private MockPreferenceService mockPreferenceService = new MockPreferenceService();
    private ReleaseDescriptor releaseDescriptor;
    private Repository repository;

    @BeforeEach
    void createService(@TempDirectory.TempDir Path tempDirectory) {
        Path path = tempDirectory.resolve(Paths.get( "release-status" + File.separator + "logs"));
        mockPreferenceService.set(LoggerService.LOGGER_BASE_PATH, path.toString());
        loggerService = new DefaultLoggerService();
        loggerService.preferenceService = mockPreferenceService;
        repository = new Repository();
        repository.setName("test-repo");
        releaseDescriptor = new ReleaseDescriptor(
                "1-SNAPSHOT",
                "1",
                "2-SNAPSHOT",
                Collections.singletonList(new Project(repository, Paths.get(""), new Module("test-group", "test-id", "test-version"))),
                ReleaseStage.RELEASE_PULL,
                RollbackStage.NO_ROLLBACK,
                false
        );
    }

    @Test
    @DisplayName("Prepare logger test")
    void prepareLoggerTest() {
        assertNull(loggerService.getMultiAppender());
        loggerService.prepareLogger(releaseDescriptor);
        assertNotNull(loggerService.getMultiAppender());
        assertEquals(loggerService.getMultiAppender().getAppenderMap().size(), 7);
    }

    @Test
    @DisplayName("Create logger map test")
    void loggerMapTest() {
        loggerService.prepareLogger(releaseDescriptor);
        for(List<String> path : loggerService.getMultiAppender().getAppenderMap().keySet()) {
            loggerService.getMultiAppender().setCurrentAppender(path);
        }

        assertTrue(Files.exists(Paths.get(mockPreferenceService.get(LoggerService.LOGGER_BASE_PATH))));
        Path loggerPath = Paths.get(mockPreferenceService.get(LoggerService.LOGGER_BASE_PATH), releaseDescriptor.getReleaseVersion(), repository.getName());
        Path loggerReleasePath = loggerPath.resolve(Paths.get("release"));
        Path loggerRollbackPath = loggerPath.resolve(Paths.get("rollback"));

        assertTrue(Files.exists(loggerReleasePath));
        assertTrue(Files.exists(loggerRollbackPath));

        for(ReleaseStage releaseStage : ReleaseStage.values()) {
            if(releaseStage == ReleaseStage.NO_RELEASE) {
                continue;
            }
            assertTrue(Files.exists(loggerReleasePath.resolve(releaseStage.name() + ".log")));
        }

        for(RollbackStage rollbackStage : RollbackStage.values()) {
            if(rollbackStage == RollbackStage.NO_ROLLBACK) {
                continue;
            }
            assertTrue(Files.exists(loggerRollbackPath.resolve(rollbackStage.name() + ".log")));
        }
    }
}
