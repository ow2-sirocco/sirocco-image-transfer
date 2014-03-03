package org.ow2.sirocco.imagetransfer;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class ImageTransferServer {

    private static Logger logger;

    private static URI BASE_URI = URI.create("http://localhost:8080/sirocco/");

    public static void main(final String[] args) {
        try {
            ImageTransferServer.logger = Logger.getLogger(ImageTransferServer.class.getName());
            
            if (args.length >0) {
            	if (args.length !=2) {
            		System.out.println("You have provided a wrong number of arguments\n");
            		System.out.println("USAGE: java [-DhttpRouteConxMax=max_value] ImageTransferServer [hostIP_value port_value]\n");
            		return;
            	} else {
                    String str = "http://" + args[0]+ ":" + args[1] + "/sirocco/";
                    ImageTransferServer.BASE_URI = URI.create(str);
                }
            }
            String nbRoutes = System.getProperty("httpRouteConxMax");

            ImageTransferServer.logger.info("server will start on URL: " + ImageTransferServer.BASE_URI + " with maximum "
                + (nbRoutes = (nbRoutes != null) ? nbRoutes : String.valueOf(2))
                + "\nsimultaneous image transfers using the same HTTP route");
            ImageTransferServer.logger.info(String.format("Application started.%nTry out %s%nHit enter to stop it...",
                ImageTransferServer.BASE_URI));

            final HttpServer server = ImageTransferServer.createServer();
            
            System.in.read();
            server.stop();
        } catch (IOException ex) {
            Logger.getLogger(ImageTransferServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static HttpServer createServer() {

        LoggingFilter loggingFilter = new LoggingFilter(ImageTransferServer.logger, true);

        // create a resource config that scans for JAX-RS resources and
        // providers
        final ResourceConfig rc = new ResourceConfig().packages("org.ow2.sirocco.imagetransfer").register(new MoxyXmlFeature())
            .register(loggingFilter);
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(ImageTransferServer.BASE_URI, rc);
    }
}
