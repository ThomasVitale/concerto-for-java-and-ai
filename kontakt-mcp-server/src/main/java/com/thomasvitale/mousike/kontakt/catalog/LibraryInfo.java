package com.thomasvitale.mousike.kontakt.catalog;

import com.thomasvitale.mousike.kontakt.instrument.Library;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An installed library as the client should see it.
 *
 * @param author the Author of its presets (the Browser's Brand is not in Kontakt's database)
 * @param available whether the library's folder is reachable now; false when it sits on a drive that is not mounted
 */
public record LibraryInfo(
        String product,
        String author,
        int presets,
        @Nullable String version,
        boolean available
) {

    static LibraryInfo of(Library library) {
        return new LibraryInfo(library.product(), library.author(), library.presets(), library.version(),
                library.folder() != null && Files.isDirectory(Path.of(library.folder())));
    }

}
