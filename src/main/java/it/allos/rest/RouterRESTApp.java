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
        MessageOptions messageOptions = new MessageOptions.Builder(SingleAssistant.ASSISTANT_ID, request.getSessionID())
                .input(input).build();

        // risposta
        MessageResponse response = service.message(messageOptions).execute().getResult();
        List<RuntimeResponseGeneric> responseGenerics = response.getOutput().getGeneric();
        log.info(responseGenerics.size() + "");

        if (responseGenerics.size() != 0)
            returnMessage.setText(response.getOutput().getGeneric().get(0).text());

        String detectedIntent = response.getOutput().getIntents().get(0).intent();
        // Sostituire con chiamata ad API SuccessFactors
        if (detectedIntent.equals("Indicazione_posizioni_aperte")) {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = this.getClass().getResourceAsStream(StringUtils.prependIfMissing("posizioni.json", "/"));
            Positions pos = mapper.readValue(is, Positions.class);
            if (pos.getPosizioni().size() != 0) {
                returnMessage.setText("Ecco un elenco di posizioni che ho trovato per te:");
                returnMessage.setOptions(pos.getPosizioni());
            }
            else
                returnMessage.setText("Al momento non ci sono posizioni aperte disponibili");
        }
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
        } catch (RuntimeException ex) {
            log.error("Error deleting session {}", sessionID, ex);
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error("Ops... qualcosa è andato storto, riprova più tardi.")).build());
        }
    }
}