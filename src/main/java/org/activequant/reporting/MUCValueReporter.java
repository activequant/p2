package org.activequant.reporting;

import org.activequant.core.types.TimeStamp;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Logs values to an XMPP MUC.
 * 
 * @author Ulrich Staudinger
 * 
 */
public class MUCValueReporter implements IValueReporter {

	private XMPPConnection con;
	private MultiUserChat muc;

	public MUCValueReporter(String server, String username, String password,
			String resource, String confRoom) {
		try {
			con = new XMPPConnection(server);
			con.connect();						
			con.login(username, password,resource);
			muc = new MultiUserChat(con, confRoom);
			muc.join(resource);
			silentSend("System 5 is coming up.");
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void silentSend(String msg) {
		try {
			muc.sendMessage(msg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void report(TimeStamp timeStamp, String valueKey, Double value) {
		silentSend("["+timeStamp.getDate()+"] "+valueKey+" = "+value);
	}

	@Override
	public void flush() {
	}

}
