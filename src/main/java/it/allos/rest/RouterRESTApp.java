package it.allos.rest;

import java.util.List;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.DialogNodeOutputOptionsElement;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.RuntimeIntent;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.allos.dto.Message;
import it.allos.watson.ConnectionDAO;
import it.allos.watson.SingleAssistant;

@Path("/watson/api")
public class RouterRESTApp {

    private static final Logger log = LoggerFactory.getLogger(RouterRESTApp.class);

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
    public Message<?> request(Message<String> request) {

        Assistant service = SingleAssistant.getAssistant();
        String sessionId = ConnectionDAO.getSessionID();
        Message<?> returnMessage = null;

        // invio messaggio
        MessageInput input = new MessageInput.Builder().messageType("text").text(request.getText()).build();
        MessageOptions messageOptions = new MessageOptions.Builder(SingleAssistant.ASSISTANT_ID, sessionId).input(input)
                .build();

        // risposta
        MessageResponse response = service.message(messageOptions).execute().getResult();
        List<RuntimeResponseGeneric> responseGeneric = response.getOutput().getGeneric();
        if (responseGeneric.get(0).responseType().equals("text")) {
            returnMessage = new Message<String>(responseGeneric.get(0).text());
            log.info("SessionID: {}", sessionId);
        }

        if (responseGeneric.get(0).responseType().equals("option")) {
            List<String> param = new ArrayList<String>();
            param.add(responseGeneric.get(0).title());
            for (DialogNodeOutputOptionsElement opt : responseGeneric.get(0).options()) {
                param.add(opt.getLabel());
            }
            returnMessage = new Message<List<String>>(param);
        }

        //determino se la conversazione è finita
        List<RuntimeIntent> responseIntents = response.getOutput().getIntents();
        try {
        log.info("Intent: {}", responseIntents.get(0).intent());
        } catch (Exception e) {
        }
        if(responseIntents.size() > 0 &&
        responseIntents.get(0).intent().equals("General_Ending")) {
        log.info("Deleted session {}", sessionId);
        //SingleAssistant.deleteSession();
        }
        return returnMessage;
    }

    @DELETE
    @Path("/delete/{sessionID}")
    public void deleteSession(@PathParam("sessionID") String sessionID) {
        ConnectionDAO.deleteSession(sessionID);
    }
}