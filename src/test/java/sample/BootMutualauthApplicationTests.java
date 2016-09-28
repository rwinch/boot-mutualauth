package sample;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class BootMutualauthApplicationTests {

	@Test
	public void mutualAuthenticationWorks() throws Exception {
		try (CloseableHttpClient client = createClient()) {
			HttpGet get = new HttpGet("https://localhost:8443/");
			try (CloseableHttpResponse response = client.execute(get)) {
				assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
				String body = EntityUtils.toString(response.getEntity(), "UTF-8");
				assertThat(body).contains("rob");
			}
		}
	}

	private CloseableHttpClient createClient() throws Exception {
		KeyStore keyStore = keyStore();
		SSLContext sslContext = SSLContexts
				.custom()
				.loadKeyMaterial(keyStore, "password".toCharArray())
				.loadTrustMaterial(keyStore, null)
				.build();

		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext,
				new DefaultHostnameVerifier());

		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("https", sslConnectionFactory)
				.register("http", new PlainConnectionSocketFactory())
				.build();

		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setSSLSocketFactory(sslConnectionFactory);
		builder.setConnectionManager(new PoolingHttpClientConnectionManager(registry));

		return builder.build();
	}

	private KeyStore keyStore() throws Exception {
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try(InputStream input = loader.getResourceAsStream("ssl/client_keystore.p12")){
			ks.load(input, "password".toCharArray());
		}
		return ks;
	}

}
