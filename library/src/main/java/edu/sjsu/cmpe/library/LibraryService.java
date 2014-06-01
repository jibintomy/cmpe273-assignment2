package edu.sjsu.cmpe.library;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;

import edu.sjsu.cmpe.library.api.resources.BookResource;
import edu.sjsu.cmpe.library.api.resources.RootResource;
import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Book.Status;
import edu.sjsu.cmpe.library.repository.BookRepository;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;
import edu.sjsu.cmpe.library.ui.resources.HomeResource;

public class LibraryService extends Service<LibraryServiceConfiguration> {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private static String libraryName;
	static BookRepositoryInterface bookRepository;

	public String getLibraryName() {
		return libraryName;
	}

	public static void setLibraryName(String args[]) {

		if (args[1].contains("library_a"))
			libraryName = "library-a";
		else if (args[1].contains("library_b"))
			libraryName = "library-b";
		else
			libraryName = "library-x";
	}

	public static void main(String[] args) throws Exception {
		bookRepository = new BookRepository();
		setLibraryName(args);
		new LibraryService().run(args);
	}

	@Override
	public void initialize(Bootstrap<LibraryServiceConfiguration> bootstrap) {
		bootstrap.setName("library-service");
		bootstrap.addBundle(new ViewBundle());
		bootstrap.addBundle(new AssetsBundle());
		Thread one = new Thread(new Runnable() {
			public void run() {
				try {
					startListener();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
		one.start();
	}

	public void startListener() throws JMSException {

		String libraryName = getLibraryName();
		System.out.println(libraryName);
		String user = env("APOLLO_USER", "admin");
		String password = env("APOLLO_PASSWORD", "password");
		String host = env("APOLLO_HOST", "54.215.133.131");
		int port = Integer.parseInt(env("APOLLO_PORT", "61613"));
		String destination = null;
		if (libraryName.equals("library-a"))
			destination = arg(0, "/topic/36451.book.*");
		else if (libraryName.equals("library-b"))
			destination = arg(0, "/topic/36451.book.computer");

		StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
		factory.setBrokerURI("tcp://" + host + ":" + port);

		Connection connection = factory.createConnection(user, password);
		connection.start();
		Session session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);
		Destination dest = new StompJmsDestination(destination);

		MessageConsumer consumer = session.createConsumer(dest);
		System.currentTimeMillis();
		System.out.println("Waiting for messages...");
		String body = new String();
		while (true) {
			Message msg = consumer.receive();
			if (msg instanceof TextMessage) {
				body = ((TextMessage) msg).getText();
				if ("SHUTDOWN".equals(body)) {
					break;
				}
				System.out.println("Received message = " + body);

			} else if (msg instanceof StompJmsMessage) {
				StompJmsMessage smsg = ((StompJmsMessage) msg);
				body = smsg.getFrame().contentAsString();
				if ("SHUTDOWN".equals(body)) {
					break;
				}
				System.out.println("Received message = " + body);

			} else {
				System.out
						.println("Unexpected message type: " + msg.getClass());
			}
			updateRepository(body);
		}

		connection.close();
	}

	public void updateRepository(String bookReceived) {

		String[] bookDetails = bookReceived.split(":");
		long isbn = Integer.parseInt(bookDetails[0]);
		Book book = bookRepository.getBookByISBN(isbn);
		if (book != null) {
			// Book exists in library
			if (book.getStatus() == Status.lost)
				book.setStatus(Status.available);
		} else {
			// Add book to library
			book = new Book();
			book.addNewBook(bookReceived);
			bookRepository.addNewBook(book);
		}
	}

	private static String env(String key, String defaultValue) {
		String rc = System.getenv(key);
		if (rc == null) {
			return defaultValue;
		}
		return rc;
	}

	private static String arg(int index, String defaultValue) {
		return defaultValue;
	}

	@Override
	public void run(LibraryServiceConfiguration configuration,
			Environment environment) throws Exception {
		// This is how you pull the configurations from library_x_config.yml
		String queueName = configuration.getStompQueueName();
		String topicName = configuration.getStompTopicName();
		log.debug("{} - Queue name is {}. Topic name is {}",
				configuration.getLibraryName(), queueName, topicName);
		// TODO: Apollo STOMP Broker URL and login

		/** Root API */
		environment.addResource(RootResource.class);
		/** Books APIs */
		// BookRepositoryInterface bookRepository = new BookRepository();
		environment.addResource(new BookResource(bookRepository, libraryName));

		/** UI Resources */
		environment.addResource(new HomeResource(bookRepository));
	}
}
