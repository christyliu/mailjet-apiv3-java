package com.mailjet.api.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;

import com.mailjet.api.MailJetBaseT;
import com.mailjet.api.client.MailJetApiCall.Create;
import com.mailjet.api.client.MailJetApiCallException.Error;
import com.mailjet.api.client.config.MailJetClientConfiguration;
import com.mailjet.api.client.connect.MailJetConnectionProvider;
import com.mailjet.api.client.connect.MailJetConnectionProvider.MailJetApiCredentials;
import com.mailjet.api.client.connect.MailJetConnectionProvider.MailJetApiResponse;
import com.mailjet.api.client.model.MailJetKeyProperty;
import com.mailjet.api.client.transform.DataTransformer;

/**
 * Tests that {@link MailJetApiCall} implementations must pass.
 * 
 * @author MailJet
 * 
 */
public abstract class CreateTest extends MailJetBaseT {

	/**
	 * Base URL.
	 */
	protected static final String MAILJET_URL = "http://base";

	private static final String DEFAULT_PARAMS = "style=flat&format=json";

	/**
	 * Test that call is not performed without properties.
	 */
	@Test
	public final void testExecuteWithoutProperties() {
		try {
			createCall().execute();
			Assert.fail();
		} catch (MailJetApiCallException e) {
			Assert.assertEquals(Error.ClientMisuse, e.getError());
		}
	}

	/**
	 * Tests that client doesn't allow null property.
	 */
	@Test
	public final void testNullProperty() {
		try {
			createCall().property(null, new Object()).execute();
			Assert.fail();
		} catch (MailJetApiCallException e) {
			Assert.assertEquals(Error.ClientMisuse, e.getError());
		}
	}

	/**
	 * Tests that client doesn't allow null property names.
	 * 
	 */
	@Test
	public final void testNullPropertyName() {
		try {
			createCall()
					.property(
							new MailJetKeyProperty<Object, String>(null, String.class),
							"value");
			Assert.fail();
		} catch (MailJetApiCallException e) {
			Assert.assertEquals(Error.ClientMisuse, e.getError());
		}
	}

	/**
	 * Tests that client doesn't allow setting a property twice.
	 * 
	@Test
	public final void testSetPropertyTwice() {
		try {
			createCall().property(
					new MailJetKeyProperty<Object,String>("property1", String.class),
					"value").property(
					new MailJetKeyProperty<Object,String>("property1", String.class),
					"value");
			Assert.fail();
		} catch (MailJetApiCallException e) {
			Assert.assertEquals(Error.ClientMisuse, e.getError());
		}
	}
	
	 */

	/**
	 * Test that if null property value is specified it is sent as empty string
	 * to server.
	 * 
	 * @throws MailJetApiCallException
	 *             not expected
	 * @throws URISyntaxException
	 *             not expected
	 */
	@Test
	public final void testNullPropertyValueIsSentAsEmptyString()
			throws MailJetApiCallException, URISyntaxException {
		final MailJetConnectionProvider connProvider = context
				.mock(MailJetConnectionProvider.class);

		context.checking(new Expectations() {
			{
				oneOf(connProvider).post(createCallUri(),
						"{" + property("property", "", false) + "}",
						defaultCredetials());
				will(returnValue(sampleResponse()));
			}

		});

		TestObject object = createTestObjectCall(connProvider).property(
				new MailJetKeyProperty<TestObject, String>("property", String.class), null)
				.execute();
		assertSampleResponse(object);
	}

	/**
	 * Test that if all property values are included in request.
	 * 
	 * @throws MailJetApiCallException
	 *             not expected
	 * @throws URISyntaxException
	 *             not expected
	 */
	@Test
	public final void testMultipleProperties() throws MailJetApiCallException,
			URISyntaxException {
		final MailJetConnectionProvider connProvider = context
				.mock(MailJetConnectionProvider.class);

		context.checking(new Expectations() {
			{
				oneOf(connProvider).post(
						createCallUri(),
						"{" + property("property1", "v1")
								+ property("property2", "v2", false) + "}",
						defaultCredetials());
				will(returnValue(sampleResponse()));
			}
		});

		TestObject object = createTestObjectCall(connProvider)
				.property(
						new MailJetKeyProperty<TestObject, String>("property1",
								String.class), "v1")
				.property(
						new MailJetKeyProperty<TestObject, String>("property2",
								String.class), "v2").execute();
		assertSampleResponse(object);
	}

	/**
	 * Test that client works with single property.
	 * 
	 * @throws MailJetApiCallException
	 *             not expected
	 * @throws URISyntaxException
	 *             not expected
	 */
	@Test
	public final void testSingleProperty() throws MailJetApiCallException,
			URISyntaxException {
		final MailJetConnectionProvider connProvider = context
				.mock(MailJetConnectionProvider.class);

		context.checking(new Expectations() {
			{
				oneOf(connProvider).post(createCallUri(),
						"{" + property("property1", "v1", false) + "}",
						defaultCredetials());
				will(returnValue(sampleResponse()));
			}
		});

		TestObject object = createTestObjectCall(connProvider)
				.property(
						new MailJetKeyProperty<TestObject, String>("property1",
								String.class), "v1").execute();
		assertSampleResponse(object);
	}
	
	/**
	 * Test that client converts errors returned by API yp
	 * {@link MailJetApiCallException}.
	 * 
	 * @throws MailJetApiCallException
	 *             not expected
	 * @throws URISyntaxException
	 *             not expected
	 */
	@Test
	public final void testURLBuiltResponseError()
			throws MailJetApiCallException, URISyntaxException {
		final MailJetConnectionProvider connProvider = context
				.mock(MailJetConnectionProvider.class);

		context.checking(new Expectations() {
			{
				oneOf(connProvider).post(createCallUri(),
						"{" + property("property1", "v1", false) + "}",
						defaultCredetials());
				will(returnValue(sampleErrorResponse()));
			}
		});

		try {
			createTestObjectCall(connProvider).property(
					new MailJetKeyProperty<TestObject, String>("property1", String.class),
					"v1").execute();
			Assert.fail();
		} catch (MailJetApiCallException e) {
			Assert.assertEquals(Error.NotFound, e.getError());
		}

	}

	private void assertSampleResponse(TestObject object) {
		Assert.assertNotNull(object);
		Assert.assertEquals("testValue", object.property);
		Assert.assertNull(object.property2);
	}

	private MailJetApiResponse sampleResponse() {
		return new MailJetConnectionProvider.MailJetApiResponse(
				"{Count:1,Data:[{property:'testValue',unknown:'test'}]}", 201);
	}

	private MailJetApiResponse sampleErrorResponse() {
		return new MailJetConnectionProvider.MailJetApiResponse(
				"{ErrorInfo:'info', ErrorMessage: 'message', Status:404}", 404);
	}

	private MailJetApiCredentials defaultCredetials() {
		return new MailJetApiCredentials("key", "secret");
	}

	private MailJetClientConfiguration getConfiguration() {
		return new MailJetClientConfiguration().setBaseUrl(MAILJET_URL)
				.setDefaultApiKey("key").setDefaultSecretKey("secret");
	}

	private String quote(String string) {
		return "\"" + string + "\"";
	}

	private String property(String name, String value, boolean... hasNext) {
		return quote(name) + ":" + quote(value)
				+ ((hasNext.length == 0 || hasNext[0]) ? "," : "");
	}

	private Create<TestObject> createTestObjectCall(
			final MailJetConnectionProvider connProvider) {
		return createCall(getConfiguration(), TestObject.class, connProvider,
				createTestDataTransformer());
	}
	
	private URI createCallUri() throws URISyntaxException {
		return new URI(MAILJET_URL + "/TestObject?" + DEFAULT_PARAMS);
	}

	/**
	 * Implementation should return new instance.
	 * 
	 * @param <ResourceType>
	 *            type of resource
	 * @return not null
	 */
	public abstract <ResourceType> Create<ResourceType> createCall();

	/**
	 * Implementation should return instance using the supplied configuration.
	 * 
	 * @param config
	 *            not null
	 * @param resourceClass
	 *            not null
	 * @param provider
	 *            not null
	 * @param transformer
	 *            not null
	 * @param <ResourceType>
	 *            type of the resource
	 * @return not null
	 */
	public abstract <ResourceType> Create<ResourceType> createCall(
			MailJetClientConfiguration config,
			Class<ResourceType> resourceClass,
			MailJetConnectionProvider provider, DataTransformer transformer);

	protected abstract DataTransformer createTestDataTransformer();
	/**
	 * Test object.
	 * 
	 * @author MailJet
	 * 
	 */
	public static class TestObject {
		private String property;
		private String property2;
	}
}
