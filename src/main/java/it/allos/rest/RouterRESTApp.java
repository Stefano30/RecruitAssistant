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
import com.ibm.watson.assistant.v2.model.RuntimeIntent;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;

import it.allos.dto.Message;
import it.allos.watson.SingleAssistant;

@Path("/watson/api")
public class RouterRESTApp {

    @GET
    @Path("/status")
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return System.currentTimeMillis() + " - OK";
    }

    @POST
    @Path("/request")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Message request(Message request) {

        Assistant service = SingleAssistant.getAssistant();
        String sessionId = SingleAssistant.getSessionID();
        Message returnMessage;

        //invio messaggio
        MessageInput input = new MessageInput.Builder().messageType("text").text(request.getText()).build();
        MessageOptions messageOptions = new MessageOptions.Builder(SingleAssistant.ASSISTANT_ID, sessionId).input(input).build();

        //risposta
        MessageResponse response = service.message(messageOptions).execute().getResult();
        List<RuntimeResponseGeneric> responseGeneric = response.getOutput().getGeneric();
        if (responseGeneric.get(0).responseType().equals("text")) {
            returnMessage = new Message(responseGeneric.get(0).text());
        }

        //determino se la conversazione è finita
        List<RuntimeIntent> responseIntents = response.getOutput().getIntents();
        if(responseIntents.size() > 0 && responseIntents.get(0).intent() == "General_Ending") {
            SingleAssistant.deleteSession();
        }
        return new Message(responseGeneric.get(0).text());
    }

}