package it.allos.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

@ApplicationPath("/watson/api")
public class RouterRESTApp extends Application {
    @GET
    @Path("/prova")
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "Hello World!";
    }
}