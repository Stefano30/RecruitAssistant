package it.allos.rest;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.swing.text.Position;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.DialogNodeOutputOptionsElement;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.RuntimeIntent;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.allos.dto.Message;
import it.allos.dto.Positions;
import it.allos.watson.ConnectionDAO;
import it.allos.watson.SingleAssistant;
import it.allos.dto.Error;

@Path("/watson/api")
public class RouterRESTApp {

    private static final Logger log = LoggerFactory.getLogger(RouterRESTApp.class);

    @GET
    @Path("/status")
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return System.currentTimeMillis() + " - OK";
    }

    @GET
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Message startConversation() {
        Assistant service = SingleAssistant.getAssistant();
        String sessionID = ConnectionDAO.getSessionID();
        MessageInput input = new MessageInput.Builder().messageType("text").text("").build();
        MessageOptions messageOptions = new MessageOptions.Builder(SingleAssistant.ASSISTANT_ID, sessionID).input(input)
                .build();
        MessageResponse response = service.message(messageOptions).execute().getResult();
        Message returnMessage = new Message();
        returnMessage.setText(response.getOutput().getGeneric().get(0).text());
        returnMessage.setSessionID(sessionID);
        return returnMessage;
    }

    @POST
    @Path("/request")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Message request(Message request) throws JsonParseException, JsonMappingException, IOException {

        Assistant service = SingleAssistant.getAssistant();
        Message returnMessage = new Message();
        returnMessage.setSessionID(request.getSessionID());

        // invio messaggio
        MessageInput input = new MessageInput.Builder().messageType("text").text(request.getText()).build();
        MessageOptions messageOptions = new MessageOptions.Builder(SingleAssistant.ASSISTANT_ID, request.getSessionID()).input(input)
                .build();

        // risposta
        // MessageResponse response = service.message(messageOptions).execute().getResult();
        // List<RuntimeResponseGeneric> responseGeneric = response.getOutput().getGeneric();
        // if (responseGeneric.get(0).responseType().equals("text")) {
        //     returnMessage.setText(responseGeneric.get(0).text());
        //     log.info("sessionID: {}", sessionID);
        // }
        // if (responseGeneric.get(0).responseType().equals("option")) {
        //     List<String> param = new ArrayList<String>();
        //     param.add(responseGeneric.get(0).title());
        //     for (DialogNodeOutputOptionsElement opt : responseGeneric.get(0).options()) {
        //         param.add(opt.getLabel());
        //     }
        //     returnMessage.setOptions(param);
        // }
        String detectedIntent = service.message(messageOptions).execute().getResult().getOutput().getIntents().get(0).intent();
        if(detectedIntent.equals("Indicazione_posizioni_aperte")) { // Chiamata a SuccessFactors
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = this.getClass().getResourceAsStream(StringUtils.prependIfMissing("posizioni.json", "/"));
            Positions pos = mapper.readValue(is, Positions.class);
            returnMessage.setOptions(pos.getPosizioni());
        }
        // determino se la conversazione è finita
        // List<RuntimeIntent> responseIntents = response.getOutput().getIntents();
        // try {
        //     log.info("Intent: {}", responseIntents.get(0).intent());
        // } catch (Exception e) {
        // }
        // if (responseIntents.size() > 0 && responseIntents.get(0).intent().equals("General_Ending")) {
        //     log.info("Deleted session {}", sessionID);
        //     ConnectionDAO.deleteSession(sessionID);
        // }
        return returnMessage;
    }

    @DELETE
    @Path("/close/{sessionID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Message deleteSession(@PathParam("sessionID") String sessionID) {
        try {
            ConnectionDAO.deleteSession(sessionID);
            Message returnMessage = new Message();
            returnMessage.setSessionID(sessionID);
            returnMessage.setText("OK");
            return returnMessage;
        } catch(RuntimeException ex) {
            log.error("Error deleting session {}", sessionID, ex);
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error("Ops... qualcosa è andato storto, riprova più tardi.")).build());
        }
    }
}