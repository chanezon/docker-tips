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

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Adapter class to convert a {@link Photo} to a Spring {@link Resource}.
 *
 * @author Josh Long
 * @author Phillip Webb
 */
public class PhotoResource extends AbstractResource {

	private final Photo photo;

	public PhotoResource(Photo photo) {
		Assert.notNull(photo, "Photo must not be null");
		this.photo = photo;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.photo.getInputStream();
	}

	@Override
	public long contentLength() throws IOException {
		return -1;
	}

}
