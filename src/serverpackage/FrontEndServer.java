package serverpackage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.omg.CORBA.ORB;

import ServerOperationApp.ServerOperationIDLPOA;

public class FrontEndServer extends ServerOperationIDLPOA {
	private ORB orb;
    
	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

	public void shutdown() {
		orb.shutdown(false);
	}

	public String createSRecord(String managerID, String firstName, String lastName, String[] coursesRegistered,
			String status, String statusDate) {
		return null;
	}

	public String createTRecord(String managerID, String firstName, String lastName, String address, String phone,
			String specialization, String location) {
		return null;
	}

	public String editRecord(String managerID, String recordID, String fieldName, String newValue) {
		return null;
	}

	public String getRecordCounts(String managerID) {
		return null;
	}
	public String transferRecord(String managerID, String recordID, String transferServerName) {
		return null;
	}
	
	public static void main(String[] args){
		BullyServer b=new BullyServer();
		b.setDaemon(false);
		b.start();
		
	}

}
