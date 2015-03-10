/**
 *
 * Copyright (c) 2006-2015, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.codegen.java.views;

import static com.speedment.codegen.Formatting.*;
import com.speedment.codegen.base.CodeGenerator;
import com.speedment.codegen.base.CodeView;
import com.speedment.codegen.base.DependencyManager;
import com.speedment.codegen.lang.models.File;
import com.speedment.util.CodeCombiner;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author Emil Forslund
 */
public class FileView implements CodeView<File> {
	private final static String PACKAGE_STRING = "package ";
	
	private String renderPackage(File file) {
		final Optional<String> name = fileToClassName(file.getName());
		if (name.isPresent()) {
			final Optional<String> pack = packageName(name.get());
			if (pack.isPresent()) {
				return PACKAGE_STRING + pack.get() + scdnl();
			}
		}
		
		return EMPTY;
	}
	
	@Override
	public Optional<String> render(CodeGenerator cg, File model) {
		final DependencyManager mgr = cg.getDependencyMgr();
		Optional<String> className = fileToClassName(model.getName());
		Optional<String> packageName = packageName(className.orElse(EMPTY));
		mgr.clearDependencies();
		
		if (packageName.isPresent()) {
			if (mgr.isIgnored(packageName.get())) {
				packageName = Optional.empty();
			} else {
				mgr.ignorePackage(packageName.get());
			}
		}

		final Optional<String> view = Optional.of(
			ifelse(cg.on(model.getJavadoc()), s -> s + nl(), EMPTY) +
			renderPackage(model) +
			cg.onEach(model.getImports())
                .distinct().sorted()
                .collect(CodeCombiner.joinIfNotEmpty(nl(), EMPTY, dnl())) +
			cg.onEach(model.getClasses()).collect(CodeCombiner.joinIfNotEmpty(dnl()))
		);
		
		if (packageName.isPresent()) {
			mgr.acceptPackage(packageName.get());
		}
		
		return view;
	}
}