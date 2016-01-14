package com.aluxian.butler.corenlpserver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * This class launches the web application in an embedded Jetty container. This is the entry point to your application.
 * The Java command that is used for launching should fire this main method.
 */
public class Main {

    /**
     * @param args CL arguments
     */
    public static void main(String[] args) throws Exception {
        String webappDirLocation = "src/main/webapp/";

        // Initialise the StanfordCoreNLP parser
        CoreNLP.instance();

        // Get port from args or use the default
        int port = 7331;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server(port);
        WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);

        // Parent loader priority is a class loader setting that Jetty accepts.
        // By default Jetty will behave like most web containers in that it will
        // allow your application to replace non-server libraries that are part of the
        // container. Setting parent loader priority to true changes this behavior.
        // Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
        root.setParentLoaderPriority(true);

        server.setHandler(root);

        server.start();
        server.join();
    }

}
