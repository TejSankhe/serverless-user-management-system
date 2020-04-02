package lambda;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import java.util.UUID;

public class SendEmails implements RequestHandler<SNSEvent, Object> {
	static DynamoDB dynamoDB;

	public Object handleRequest(SNSEvent request, Context context) {

		String domain = System.getenv("DomainName");

		final String From = "no-reply@" + domain;
		final String To = request.getRecords().get(0).getSNS().getMessage();

		try {
			initDynamoDB();
			Table dynamoDBTable = dynamoDB.getTable("sendEmailcsye6225DynamoDB");
			long ttl = Instant.now().getEpochSecond() + 60 * 60;
			long now = Instant.now().getEpochSecond();
			if (dynamoDBTable == null) {
				System.out.println("Table not found");
			} else {
				Item item = dynamoDBTable.getItem("EmailAddress", To);
				if (item == null || (item != null && Long.parseLong(item.get("TTL").toString()) < now)) {
					Item newItem = new Item().withPrimaryKey("EmailAddress", To)
							.withNumber("TTL", ttl);

					dynamoDBTable.putItem(newItem);

					AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
							.withRegion(Regions.US_EAST_1).build();
					SendEmailRequest req = new SendEmailRequest().withDestination(new Destination().withToAddresses(To))
							.withMessage(new Message()
									.withBody(new Body().withHtml(new Content().withCharset("UTF-8")
											.withData("Below is the reqguested bills<br/>")))
									.withSubject(new Content().withCharset("UTF-8").withData("Requested Bills")))
							.withSource(From);
					SendEmailResult response = client.sendEmail(req);
					System.out.println("Email sent");
				} else {
					System.out.println("Email already sent!");
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return null;
	}

	private static void initDynamoDB() throws Exception {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
		dynamoDB = new DynamoDB(client);
	}
}