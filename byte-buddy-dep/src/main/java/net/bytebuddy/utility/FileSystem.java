/*
 * Copyright 2014 - Present Rafael Winterhalter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bytebuddy.utility;

import java.io.*;
import java.security.AccessController;

/**
 * A dispatcher to interact with the file system. If NIO2 is available, the API is used. Otherwise, byte streams are used.
 */
public enum FileSystem {

    /**
     * The singleton instance.
     */
    INSTANCE;

    /**
     * A dispatcher to resolve a {@link File} to a {@code java.nio.file.Path}.
     */
    private static final Dispatcher DISPATCHER = AccessController.doPrivileged(JavaDispatcher.of(Dispatcher.class));

    /**
     * A dispatcher to interact with {@code java.nio.file.Files}.
     */
    private static final Files FILES = AccessController.doPrivileged(JavaDispatcher.of(Files.class));

    /**
     * A dispatcher to interact with {@code java.nio.file.StandardCopyOption}.
     */
    private static final StandardCopyOption STANDARD_COPY_OPTION = AccessController.doPrivileged(JavaDispatcher.of(StandardCopyOption.class));

    /**
     * Copies a file.
     *
     * @param source The source file.
     * @param target The target file.
     * @throws IOException If an I/O exception occurs.
     */
    public void copy(File source, File target) throws IOException {
        Object[] option = STANDARD_COPY_OPTION.toArray(1);
        if (option.length == 0) {
            InputStream inputStream = new FileInputStream(source);
            try {
                OutputStream outputStream = new FileOutputStream(target);
                try {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } else {
            option[0] = STANDARD_COPY_OPTION.valueOf("REPLACE_EXISTING");
            FILES.copy(DISPATCHER.toPath(source), DISPATCHER.toPath(target), option);
        }
    }

    /**
     * Moves a file.
     *
     * @param source The source file.
     * @param target The target file.
     * @throws IOException If an I/O exception occurs.
     */
    public void move(File source, File target) throws IOException {
        Object[] option = STANDARD_COPY_OPTION.toArray(1);
        if (option.length == 0) {
            InputStream inputStream = new FileInputStream(source);
            try {
                OutputStream outputStream = new FileOutputStream(target);
                try {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
            if (!source.delete()) {
                source.deleteOnExit();
            }
        } else {
            option[0] = STANDARD_COPY_OPTION.valueOf("REPLACE_EXISTING");
            FILES.move(DISPATCHER.toPath(source), DISPATCHER.toPath(target), option);
        }
    }

    /**
     * A dispatcher to resolve a {@link File} to a {@code java.nio.file.Path}.
     */
    @JavaDispatcher.Proxied("java.io.File")
    protected interface Dispatcher {

        /**
         * Resolves a {@link File} to a {@code java.nio.file.Path}.
         *
         * @param value The file to convert.
         * @return The transformed {@code java.nio.file.Path}.
         * @throws IOException If an I/O exception occurs.
         */
        Object toPath(File value) throws IOException;
    }

    /**
     * A dispatcher to interact with {@code java.nio.file.Files}.
     */
    @JavaDispatcher.Proxied("java.nio.file.Files")
    protected interface Files {

        /**
         * Copies a {@code java.nio.file.Path} to a different location.
         *
         * @param source The source {@code java.nio.file.Path}.
         * @param target The target {@code java.nio.file.Path}.
         * @param option The copy options.
         * @return The targeted {@code java.nio.file.Path}.
         * @throws IOException If an I/O exception occurs.
         */
        @JavaDispatcher.IsStatic
        Object copy(@JavaDispatcher.Proxied("java.nio.file.Path") Object source,
                    @JavaDispatcher.Proxied("java.nio.file.Path") Object target,
                    @JavaDispatcher.Proxied("java.nio.file.CopyOption") Object[] option) throws IOException;

        /**
         * Moves a {@code java.nio.file.Path} to a different location.
         *
         * @param source The source {@code java.nio.file.Path}.
         * @param target The target {@code java.nio.file.Path}.
         * @param option The copy options.
         * @return The targeted {@code java.nio.file.Path}.
         * @throws IOException If an I/O exception occurs.
         */
        @JavaDispatcher.IsStatic
        Object move(@JavaDispatcher.Proxied("java.nio.file.Path") Object source,
                    @JavaDispatcher.Proxied("java.nio.file.Path") Object target,
                    @JavaDispatcher.Proxied("java.nio.file.CopyOption") Object[] option) throws IOException;
    }

    /**
     * A dispatcher to interact with {@code java.nio.file.StandardCopyOption}.
     */
    @JavaDispatcher.Proxied("java.nio.file.StandardCopyOption")
    protected interface StandardCopyOption {

        /**
         * Creates an array of type {@code java.nio.file.StandardCopyOption}.
         *
         * @param size The array's size.
         * @return An array of type {@code java.nio.file.StandardCopyOption}.
         */
        @JavaDispatcher.Defaults
        @JavaDispatcher.Container
        Object[] toArray(int size);

        /**
         * Resolve an enumeration for {@code java.nio.file.StandardCopyOption}.
         *
         * @param name The enumeration name.
         * @return The enumeration value.
         */
        @JavaDispatcher.IsStatic
        Object valueOf(String name);
    }
}
