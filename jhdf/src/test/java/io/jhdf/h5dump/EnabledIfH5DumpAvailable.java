/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.h5dump;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@ExtendWith(EnabledIfH5DumpAvailableCondition.class)
public @interface EnabledIfH5DumpAvailable {
}

class EnabledIfH5DumpAvailableCondition implements ExecutionCondition {
	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("h5dump", "--version");
		try {
			Process start = processBuilder.start();
			String stdOut = IOUtils.toString(start.getInputStream(), StandardCharsets.UTF_8);
			if (StringUtils.contains(stdOut, "h5dump")) {
				return ConditionEvaluationResult.enabled("h5dump is on the path: " + stdOut);
			} else {
				return ConditionEvaluationResult.disabled("h5dump is not present");
			}

		} catch (Exception e) {
			return ConditionEvaluationResult.disabled("Exception testing h5dump");
		}
	}

}
