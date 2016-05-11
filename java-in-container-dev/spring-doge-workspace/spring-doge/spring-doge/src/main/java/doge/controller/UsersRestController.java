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

package doge.controller;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import doge.domain.DogePhoto;
import doge.domain.User;
import doge.domain.UserRepository;
import doge.photo.Photo;
import doge.photo.PhotoResource;
import doge.service.DogeService;

/**
 * MVC Controller for '/users' REST endpoints.
 *
 * @author Josh Long
 * @author Phillip Webb
 */
@RestController
@RequestMapping("/users")
public class UsersRestController {

	private final UserRepository userRepository;

	private final DogeService dogeService;

	private final SimpMessagingTemplate messaging;

	@Autowired
	public UsersRestController(UserRepository userRepository, DogeService dogeService,
			SimpMessagingTemplate messaging) {
		this.userRepository = userRepository;
		this.dogeService = dogeService;
		this.messaging = messaging;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	@RequestMapping(method = RequestMethod.POST, value = "{userId}/doge")
	public ResponseEntity<?> postDogePhoto(@PathVariable String userId,
			@RequestParam MultipartFile file, UriComponentsBuilder uriBuilder)
			throws IOException {
		Photo photo = file::getInputStream;
		User user = this.userRepository.findOne(userId);
		DogePhoto doge = this.dogeService.addDogePhoto(user, photo);
		URI uri = uriBuilder.path("/users/{userId}/doge/{dogeId}")
				.buildAndExpand(userId, doge.getId()).toUri();
		sendMessage(user, uri);
		return ResponseEntity.created(uri).build();
	}

	private void sendMessage(User user, URI uri) {
		Map<String, String> msg = new HashMap<>();
		msg.put("dogePhotoUri", uri.toString());
		msg.put("userId", user.getId());
		msg.put("userName", user.getName());
		msg.put("uploadDate", java.time.Clock.systemUTC().instant().toString());
		this.messaging.convertAndSend("/topic/alarms", msg);
	}

	@RequestMapping(method = RequestMethod.GET, value = "{userId}/doge/{dogeId}", produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public Resource getDogePhoto(@PathVariable String userId, @PathVariable String dogeId)
			throws IOException {
		User user = this.userRepository.findOne(userId);
		Photo photo = this.dogeService.getDogePhoto(user, dogeId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		return new PhotoResource(photo);
	}

}
