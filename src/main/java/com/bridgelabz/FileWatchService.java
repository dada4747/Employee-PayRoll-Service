package com.bridgelabz;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatchService {
    private final WatchService watcher;
    private final Map<WatchKey, Path> dirWatchers;

    public FileWatchService(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.dirWatchers = new HashMap<>();
        scanAndRegisterDirectories(dir);
    }

    private void registerDirWatchers(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        dirWatchers.put(key, dir);
    }

    private void scanAndRegisterDirectories(final Path start) throws IOException {
        //register directories and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirWatchers(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @SuppressWarnings("rawtypes")
    void processEvents() {
        while (true) {
            WatchKey key; //wait for the key to be signalled
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                return;
            }
            Path dir = dirWatchers.get(key);
            if (dir == null) continue;
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                Path name = (Path) event.context();
                Path child = dir.resolve(name);
                System.out.format("%s: %s\n", event.kind().name(), child); //print out event

                //if directory is created then register it and its sub-directories
                if (kind.equals(ENTRY_CREATE)) {
                    if (Files.isDirectory(child)) {
                        try {
                            scanAndRegisterDirectories(child);
                        } catch (IOException e) {
                            //duck exception
                        }

                    } else if (kind.equals(ENTRY_DELETE)) {
                        if (Files.isDirectory(child)) dirWatchers.remove(key);
                    }
                }

            }
            boolean valid = key.reset();
            if (!valid) {
                dirWatchers.remove(key);
                if (dirWatchers.isEmpty()) break; //all directories are inaccessible
            }
        }
    }
}