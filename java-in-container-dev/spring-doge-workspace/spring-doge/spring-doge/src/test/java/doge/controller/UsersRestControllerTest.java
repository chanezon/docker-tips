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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import doge.domain.User;
import doge.domain.UserRepository;
import doge.service.DogeService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Tests for {@link UsersRestController}.
 *
 * @author Josh Long
 * @author Phillip Webb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfiguration.class })
@WebAppConfiguration
@ActiveProfiles("unittest")
public class UsersRestControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DogeService dogePhotoService;

	private MockMvc mvc;

	@Before
	public void setup() {
		this.mvc = webAppContextSetup(this.context).build();
	}

	@Test
	@Ignore
	public void getUser() throws Exception {
		given(this.userRepository.findOne("1")).willReturn(
				new User("philwebb", "Phil Webb"));
		ResultActions result = this.mvc.perform(get("/users/philwebb").accept(
				MediaType.APPLICATION_JSON));
		result.andExpect(status().isOk());
		result.andExpect(content().string(containsString("Phil Webb")));
	}

}

@Configuration
@EnableAutoConfiguration
@Import(UsersRestController.class)
@Profile("unittest")
class TestConfiguration {

	@Bean
	public SimpMessagingTemplate messageTemplate() {
		return mock(SimpMessagingTemplate.class);
	}

	@Bean
	public UserRepository userRepository() {
		return mock(UserRepository.class);
	}

	@Bean
	public DogeService dogePhotoService() {
		return mock(DogeService.class);
	}

}
