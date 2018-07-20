/*
 * Copyright 2016-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.appbroker.sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.appbroker.sample.fixtures.CloudFoundryApiFixture;
import org.springframework.cloud.appbroker.sample.fixtures.OpenServiceBrokerApiFixture;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.is;
import static org.springframework.cloud.appbroker.sample.CreateInstanceComponentTest.APP_NAME_1;
import static org.springframework.cloud.appbroker.sample.CreateInstanceComponentTest.APP_NAME_2;

@TestPropertySource(properties = {
	"spring.cloud.appbroker.apps[0].path=classpath:demo.jar",
	"spring.cloud.appbroker.apps[0].name=" + APP_NAME_1,
	"spring.cloud.appbroker.apps[1].path=classpath:demo.jar",
	"spring.cloud.appbroker.apps[1].name=" + APP_NAME_2
})
class CreateInstanceComponentTest extends WiremockComponentTest {
	static final String APP_NAME_1 = "first-app";
	static final String APP_NAME_2 = "second-app";

	@Autowired
	private OpenServiceBrokerApiFixture brokerFixture;

	@Autowired
	private CloudFoundryApiFixture cloudFoundryFixture;

	@Test
	void shouldPushAppWhenCreateServiceEndpointCalled() {
		// when a service instance is created
		given(brokerFixture.serviceInstanceRequest())
			.when()
			.put(brokerFixture.createServiceInstanceUrl(), "instance-id")
			.then()
			.statusCode(HttpStatus.CREATED.value());

		// then a backing application is deployed
		given(cloudFoundryFixture.request())
			.when()
			.get(cloudFoundryFixture.findApplicationUrl(APP_NAME_1))
			.then()
			.statusCode(HttpStatus.OK.value())
			.body("resources[0].entity.name", is(equalToIgnoringWhiteSpace(APP_NAME_1)));

		// then a backing application is deployed
		given(cloudFoundryFixture.request())
			.when()
			.get(cloudFoundryFixture.findApplicationUrl(APP_NAME_2))
			.then()
			.statusCode(HttpStatus.OK.value())
			.body("resources[0].entity.name", is(equalToIgnoringWhiteSpace(APP_NAME_2)));
	}
}