/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package doge.photo;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

/**
 * Tests for {@link DogePhotoManipulator}.
 *
 * @author Phillip Webb
 * @author Josh Long
 */
public class DogePhotoManipulatorTest {

	private PhotoManipulator manipulator = new DogePhotoManipulator() {
		{
			addTextOverlay("very1", "so1", "what1");
			addTextOverlay("very2", "so2", "what2");
		}
	};

	private File file;

	@Before
	public void clean() {
		File target = new File(System.getProperty("java.io.tmpdir"));
		this.file = new File(target, "manipulatedhoff.jpg");
		this.file.delete();
		System.out.println(this.file);
	}

	@Test
	public void testDogePhotoManipulatorService() throws Exception {
		Photo photo = () -> new ClassPathResource("thehoff.jpg").getInputStream();
		Photo manipulated = this.manipulator.manipulate(photo);
		FileCopyUtils.copy(manipulated.getInputStream(), new FileOutputStream(this.file));
	}

}
