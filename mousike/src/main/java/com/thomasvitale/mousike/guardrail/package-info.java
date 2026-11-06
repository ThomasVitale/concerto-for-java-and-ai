/**
 * Guardrails: cheap, local decisions that run before any chat model sees a request.
 */
@NullMarked
@ApplicationModule(displayName = "Guardrail", allowedDependencies = {})
package com.thomasvitale.mousike.guardrail;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
