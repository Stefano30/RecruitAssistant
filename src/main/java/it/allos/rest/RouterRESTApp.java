package it.allos.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;
import com.ibm.watson.assistant.v2.model.SessionResponse;

import it.allos.dto.Message;
import it.allos.watson.WatsonAuth;

@Path("/watson/api")
public class RouterRESTApp {
	
    @GET
    @Path("/status")
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return System.currentTimeMillis() + "";
    }

    @POST
    @Path("/request")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Message request(Message request) {
        Assistant service = WatsonAuth.makeAssistant();
        SessionResponse session = WatsonAuth.getSession(service);
        String sessionId = session.getSessionId();
        MessageInput input = new MessageInput.Builder().messageType("text").text("").build();
        MessageOptions messageOptions = new MessageOptions.Builder(WatsonAuth.ASSISTANT_ID, sessionId).input(input).build();
        MessageResponse response = service.message(messageOptions).execute().getResult();
        List<RuntimeResponseGeneric> responseGeneric = response.getOutput().getGeneric();
        return new Message(responseGeneric.get(0).text());
    }
    
}