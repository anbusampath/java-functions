/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.java.function.cassandra.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.cassandraunit.spring.CassandraUnitDependencyInjectionIntegrationTestExecutionListener;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.WriteResult;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StringUtils;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.pivotal.java.function.cassandra.consumer.domain.Book;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Artem Bilan
 */
@TestExecutionListeners(mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
		listeners = CassandraUnitDependencyInjectionIntegrationTestExecutionListener.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = {
				"spring.data.cassandra.keyspaceName=" + CassandraConsumerApplicationTests.CASSANDRA_KEYSPACE,
				"cassandra.cluster.createKeyspace=true" })
@EmbeddedCassandra(configuration = EmbeddedCassandraServerHelper.CASSANDRA_RNDPORT_YML_FILE, timeout = 120000)
@DirtiesContext
abstract class CassandraConsumerApplicationTests {

	static final String CASSANDRA_KEYSPACE = "test";

	@Autowired
	protected CassandraOperations cassandraTemplate;

	@Autowired
	protected Function<Object, Mono<? extends WriteResult>> cassandraConsumer;

	@BeforeAll
	static void setUp() {
		EmbeddedCassandraServerHelper.getSession();
		System.setProperty("spring.data.cassandra.port", "" + EmbeddedCassandraServerHelper.getNativeTransportPort());
	}

	@AfterAll
	static void cleanup() {
		System.clearProperty("spring.data.cassandra.port");
	}

	@AfterEach
	void tearDown() {
		this.cassandraTemplate.truncate(Book.class);
	}

	protected static List<Book> getBookList(int numBooks) {

		List<Book> books = new ArrayList<>();

		Book b;
		for (int i = 0; i < numBooks; i++) {
			b = new Book();
			b.setIsbn(UUID.randomUUID());
			b.setTitle("Spring Cloud Data Flow Guide");
			b.setAuthor("SCDF Guru");
			b.setPages(i * 10 + 5);
			b.setInStock(true);
			b.setSaleDate(new Date());
			books.add(b);
		}

		return books;
	}

}
