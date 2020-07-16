package it.allos.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/watson/api")
public class RouterRESTApp {
	
    @GET
    @Path("/prova")
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "Ciaoneeee";
    }
    
}