package ${{ values.basePackage }};

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}

@RestController
class ChatController {

	private final ChatClient chatClient;
	private final VectorStore vectorStore;

	ChatController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
		this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

	@PostMapping("/chat")
	String chatWithDocument(@RequestBody String input) {
		return chatClient.prompt()
				.advisors(new QuestionAnswerAdvisor(vectorStore))
				.user(input)
				.call()
				.content();
	}

}

@Component
class IngestionPipeline {

	private static final Logger logger = LoggerFactory.getLogger(IngestionPipeline.class);
	private final JdbcClient jdbcClient;
	private final VectorStore vectorStore;

	@Value("classpath:documents/story.md")
	Resource textFile;

	public IngestionPipeline(JdbcClient jdbcClient, VectorStore vectorStore) {
        this.jdbcClient = jdbcClient;
        this.vectorStore = vectorStore;
	}

	@PostConstruct
	public void run() {
		if ((long) jdbcClient.sql("select count(*) from vector_store").query().singleValue() > 0) {
			return;
		}

		logger.info("Loading text files as Documents");
		var textReader = new TextReader(textFile);

		logger.info("Creating and storing Embeddings from Documents");
		vectorStore.add(textReader.get());
	}

}
