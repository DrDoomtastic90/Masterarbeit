package serverImplementation;


import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import serviceImplementation.DRLFileController;
	 
public class HttpDRLFileServer {
	private static int port;
	private static String passphrase;
	private static int numberOfInstances;
	private static boolean automaticShutDown;
		
	//from http://marcelhodan.de/blog/2017/07/18/meine-ersten-erfahrungen-mit-embedded-jetty/
	public static void main(String[] args) throws Exception {
		numberOfInstances = 0;
		if(args.length>0) {
			port= Integer.parseInt(args[0]);
			passphrase = args[1];
			startMicroserviceOnSpecificPort(port, passphrase);
			automaticShutDown=false;
		}else {
			startMicroserviceOnAnyPort();
			automaticShutDown=true;
		}
	}
		
	//Start Microservice on specific Port. Microservice needs to work independently. HEnce not started from other service.
	private static void startMicroserviceOnSpecificPort(int port, String passphrase) throws Exception {
		
		// Create https Server
		//Server server = initializeSecureServer();
				
		//Create http Server
		Server server = new Server(port);
		
		// Configure ServletContextHandler	
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		HandlerList handlers = new HandlerList();

        ShutdownHandler shutdownHandler = new ShutdownHandler(passphrase);
		handlers.addHandler(shutdownHandler);
		handlers.addHandler(context);
		context.setContextPath("/");
		server.setHandler(handlers);

		// Create Servlet Container
		ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);
 
		// Tells the Jersey Servlet which REST service/class to load.
		jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", DRLFileController.class.getCanonicalName());
 
		// Start the server
	    server.setStopTimeout(300000);
		server.start();
		server.join();
		
		//TODO Try catch? Log + Return Mssage?

	}
		
	//Start Microservice on any Port. Temporary Connection closes after Execution
	private static void startMicroserviceOnAnyPort() throws Exception {

		// Create Server on f
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            port = socket.getLocalPort();
            passphrase  = RandomStringUtils.random(20, true, true);

    		// Create https Server
    		//Server server = initializeSecureServer(); 				
    		//Create http Server
    		Server server = new Server();
    		
    		// Configure ServletContextHandler	
    		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
    		HandlerList handlers = new HandlerList();

            ShutdownHandler shutdownHandler = new ShutdownHandler(passphrase);
    		handlers.addHandler(shutdownHandler);
    		handlers.addHandler(context);
    		context.setContextPath("/");
    		server.setHandler(handlers);

    		// Create Servlet Container
    		ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
    		jerseyServlet.setInitOrder(0);
     
    		// Tells the Jersey Servlet which REST service/class to load.
    		jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", DRLFileController.class.getCanonicalName());
     
    		// Start the server
    	    server.setStopTimeout(300000);
    		server.start();
    		server.join();
        } catch (Exception e) {
        	//TODO Log + Return MEssage?
        }
		

	}
	
	private static Server initializeSecureServer() {
		Server server = new Server();
		//TLS
	   //Prepare Secure Connection
	   SslContextFactory sslContextFactory = new SslContextFactory.Server();
	   sslContextFactory.setKeyStoreType("PKCS12");
	   sslContextFactory.setKeyStorePath("D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Services\\DataProvisioningServices\\ConfigFileService\\ConfigFileServiceKeystore.jks");
	   sslContextFactory.setKeyManagerPassword("ConfigFileService_safetyFirst");
	   sslContextFactory.setKeyStorePassword("ConfigFileService_safetyFirst"); 
		
	   //HttpSettings
	   HttpConfiguration http_config = new HttpConfiguration(); 
	   http_config.setSecureScheme("https");
	   HttpConfiguration https_config = new HttpConfiguration(http_config); 
	   SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer(); 
	   https_config.addCustomizer(secureRequestCustomizer);
	   
	   //Create https Connector
	   HttpConnectionFactory httpsConnectionFactory = new HttpConnectionFactory(https_config);
	   SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, "http/1.1");
	   ServerConnector httpsConnector = new ServerConnector(server, sslConnectionFactory, httpsConnectionFactory);
	   httpsConnector.setName("secureConnector");
	   httpsConnector.setPort(port);
	   httpsConnector.setIdleTimeout(50000);	
	   server.setConnectors(new Connector[] { httpsConnector });
	   return server;
	}
	
	
	
	//force shutdown themselves or other services
	public static void attemptShutdown(){
		attemptShutdown(port, passphrase);
	}
	
	public static void attemptShutdown(int port, String passphrase){
        if(numberOfInstances<=0) {
			try {
	            URL url = new URL("http://localhost:" + port + "/shutdown?token=" + passphrase);
	            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	            connection.setRequestMethod("POST");
	            connection.getResponseCode();
	        }
	        catch (Exception e) {
	            throw new RuntimeException(e);
	        }  
        }
    }
	
	//shutdown themselves or other services of no instnace is running
	public static void forceShutdown(){
		attemptShutdown(port, passphrase);
	}
	
	public static void forceShutdown(int port, String passphrase){
        try {
            URL url = new URL("http://localhost:" + port + "/shutdown?token=" + passphrase);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.getResponseCode();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }   	    
    }
	
	
	public static void increaseNumberOfInstances() {
		numberOfInstances++;
	}
	
	public static void decreaseNumberOfInstances() {
		if(numberOfInstances > 0) {
			numberOfInstances--;
		}else{
			//TODO Write Error Log -> e.g.("No Instance Running");
			attemptShutdown();
			
		}
	}
	
	
	public static boolean isAutomaticShutdown() {
		return automaticShutDown;
	}
}

