import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/server")
public class SNSServlet extends HttpServlet {

	private static HttpSession session;
	private static int i = 0;
	private static final long serialVersionUID = 1L;
	private static final String lat[] = { "36.782551", "37.982745", "38.782551", "39.782654", "40.782551", "41.982745",
			"42.782551", "43.782654" };
	private static final String lng[] = { "-121.445368", "-122.444586", "-123.843688", "-122.242815", "-119.445368",
			"-118.444586", "-117.843688", "-116.242815" };

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Inside Get");
		if (session == null) {
			System.out.println("inside If Session id is "+session.getId());
			session = req.getSession(true);
			session.setAttribute("req", req);
			session.setAttribute("resp", resp);
		}
		System.out.println("Outside if Session id is "+session.getId());
		resp.setContentType("text/html");
		resp.setHeader("Cache-Control", "no-cache");
		PrintWriter out = resp.getWriter();
		if(i==lat.length)
			i=0;
		out.write(lat[i]+","+lng[i]);
		i++;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("I am in Post");
		String messagetype = req.getHeader("x-amz-sns-message-type");
		if (messagetype == null)
			return;
		Scanner scan = new Scanner(req.getInputStream());
		StringBuilder builder = new StringBuilder();
		while (scan.hasNextLine()) {
			builder.append(scan.nextLine());
		}
		scan.close();
		Message msg = readMessageFromJson(builder.toString());

		if (msg.getSignatureVersion().equals("1")) {
			if (isMessageSignatureValid(msg))
				System.out.println(">>Signature verification succeeded");
			else {
				System.out.println(">>Signature verification failed");
				throw new SecurityException("Signature verification failed.");
			}
		} else {
			System.out.println(">>Unexpected signature version. Unable to verify signature.");
			throw new SecurityException("Unexpected signature version. Unable to verify signature.");
		}

		// Process the message based on type.
		if (messagetype.equals("Notification")) {
			// TODO: Do something with the Message and Subject.
			System.out.println("Inside Notification:next Call doGet ");
			AjaxServlet servlet = new AjaxServlet();
			servlet.doGet((HttpServletRequest) session.getAttribute("req"), (HttpServletResponse) session.getAttribute("resp"));
		}

		else if (messagetype.equals("SubscriptionConfirmation")) {
			// TODO: You should make sure that this subscription is from the
			// topic you expect. Compare topicARN to your list of topics
			// that you want to enable to add this endpoint as a subscription.

			// Confirm the subscription by going to the subscribeURL location
			// and capture the return value (XML message body as a string)
			Scanner sc = new Scanner(new URL(msg.getSubscribeURL()).openStream());
			StringBuilder sb = new StringBuilder();
			while (sc.hasNextLine()) {
				sb.append(sc.nextLine());
			}
			sc.close();
			System.out.println(
					">>Subscription confirmation (" + msg.getSubscribeURL() + ") Return value: " + sb.toString());
			// TODO: Process the return value to ensure the endpoint is
			// subscribed.
			SNSHelper.INSTANCE.confirmTopicSubmission(msg);
		} else if (messagetype.equals("UnsubscribeConfirmation")) {
			// TODO: Handle UnsubscribeConfirmation message.
			// For example, take action if unsubscribing should not have
			// occurred.
			// You can read the SubscribeURL from this message and
			// re-subscribe the endpoint.
			System.out.println(">>Unsubscribe confirmation: " + msg.getMessage());
		} else {
			// TODO: Handle unknown message type.
			System.out.println(">>Unknown message type.");
		}
		System.out.println(">>Done processing message: " + msg.getMessageId());

	}

	private Message readMessageFromJson(String string) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Message message = null;
		try {
			message = mapper.readValue(string, Message.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return message;
	}

	private boolean isMessageSignatureValid(Message msg) {

		try {
			URL url = new URL(msg.getSigningCertUrl());
			InputStream inStream = url.openStream();
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
			inStream.close();

			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(cert.getPublicKey());
			sig.update(getMessageBytesToSign(msg));
			return sig.verify(Base64.decodeBase64(msg.getSignature().getBytes()));
		} catch (Exception e) {
			throw new SecurityException("Verify method failed.", e);

		}
	}

	private byte[] getMessageBytesToSign(Message msg) {

		byte[] bytesToSign = null;
		if (msg.getType().equals("Notification"))
			bytesToSign = buildNotificationStringToSign(msg).getBytes();
		else if (msg.getType().equals("SubscriptionConfirmation") || msg.getType().equals("UnsubscribeConfirmation"))
			bytesToSign = buildSubscriptionStringToSign(msg).getBytes();
		return bytesToSign;
	}

	// Build the string to sign for Notification messages.
	private static String buildNotificationStringToSign(Message msg) {
		String stringToSign = null;

		// Build the string to sign from the values in the message.
		// Name and values separated by newline characters
		// The name value pairs are sorted by name
		// in byte sort order.
		stringToSign = "Message\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		if (msg.getSubject() != null) {
			stringToSign += "Subject\n";
			stringToSign += msg.getSubject() + "\n";
		}
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}

	// Build the string to sign for SubscriptionConfirmation
	// and UnsubscribeConfirmation messages.
	private static String buildSubscriptionStringToSign(Message msg) {
		String stringToSign = null;
		// Build the string to sign from the values in the message.
		// Name and values separated by newline characters
		// The name value pairs are sorted by name
		// in byte sort order.
		stringToSign = "Message\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		stringToSign += "SubscribeURL\n";
		stringToSign += msg.getSubscribeURL() + "\n";
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "Token\n";
		stringToSign += msg.getToken() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}

}
