/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.shell.command.nosecurity;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.springframework.xd.shell.command.AbstractKafkaSourceSinkTests;

/**
 * Integration tests for Kafka source and sinks.
 *
 * @author Gunnar Hillert
 */
public class KafkaSourceSinkTests extends AbstractKafkaSourceSinkTests {

	@BeforeClass
	public static synchronized void startUp() throws InterruptedException, IOException {
		doStartUp();
	}

	@AfterClass
	public static void shutdown() {
		doShutdown();
	}
}
