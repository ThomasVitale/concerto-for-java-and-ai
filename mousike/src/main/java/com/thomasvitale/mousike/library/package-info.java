/**
 * The instrument library: the composer's Kontakt libraries, reached over MCP. Optional at runtime; the app works
 * without the library server, it just cannot name presets.
 */
@NullMarked
@ApplicationModule(displayName = "Library", allowedDependencies = {})
package com.thomasvitale.mousike.library;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
