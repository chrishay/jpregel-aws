/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsb.jpregel.clouds;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import edu.ucsb.jpregel.system.ClientToMaster;
import edu.ucsb.jpregel.system.FileSystem;
import edu.ucsb.jpregel.system.Master;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Set;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.compute.domain.NodeMetadata;

/**
 *
 * @author Charles
 */
class CloudMasterMachineGroup extends CloudMachineGroup<ClientToMaster> {

	public CloudMasterMachineGroup(Set<? extends NodeMetadata> nodes, @Named("compute") ApiMetadata apiMetadata) {
		super(nodes, apiMetadata);
	}

	@Override
	protected ClientToMaster getRemoteReference() {
		ClientToMaster remoteObject = null;
		String url = "//" + getHostname() + ":" + Master.PORT + "/" + Master.CLIENT_SERVICE_NAME;
		for (int i = 0;; i += 5000) {
			try {
				remoteObject = (ClientToMaster) Naming.lookup(url);
			} catch (Exception ex) {
				System.out.println("Master not up yet. Trying again in 5 seconds...");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ex1) {
					System.out.println("Waiting interrupted, trying again immediately");
				}
				continue;
			}
			break;
		}
		return remoteObject;
	}

	public static class CloudMaster extends Master {

		private final ApiMetadata storage;

		public static void main(String[] args) throws AlreadyBoundException, InterruptedException, IOException {
			System.setSecurityManager(new RMISecurityManager());
			Registry registry = LocateRegistry.createRegistry(Master.PORT);
			ApiMetadata storage = (ApiMetadata) new ObjectInputStream(new FileInputStream("storage"));
			ClientToMaster master = new CloudMaster(storage);
			registry.bind(Master.SERVICE_NAME, master);
			master.init(Integer.parseInt(args[0]));
			registry.bind(Master.CLIENT_SERVICE_NAME, master);
			System.out.println("Master ready");
		}
		@Inject
		public CloudMaster(@Named("storage") ApiMetadata storage) throws RemoteException {
			this.storage = storage;
		}

		@Override
		public FileSystem makeFileSystem(String jobDirectoryName) {
			return null;//new CloudFileSystem(jobDirectoryName, storage);
		}
	}
}
