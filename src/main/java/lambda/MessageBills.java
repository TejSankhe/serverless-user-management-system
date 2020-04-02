package lambda;

import java.util.List;

public class MessageBills {

	private String emailId;
	private List<String> bills;
	
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public List<String> getBills() {
		return bills;
	}
	public void setBills(List<String> bills) {
		this.bills = bills;
	}

	public MessageBills() {
	}

	public MessageBills(String emailId, List<String> bills) {
		super();
		this.emailId = emailId;
		this.bills = bills;
	}
	
}
