package it.allos;

import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.SessionResponse;

import it.allos.watson.WatsonAuth;

import com.ibm.watson.assistant.v2.model.DeleteSessionOptions;
import com.ibm.watson.assistant.v2.model.DialogNodeOutputOptionsElement;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.RuntimeIntent;
import java.util.List;

public class RecruitAssistant {
  public static void main(String[] args) {

    Assistant service = WatsonAuth.makeAssistant();
    SessionResponse session = WatsonAuth.getSession(service);
    String sessionId = session.getSessionId();
    // Input/output loop
    MessageInput input = new MessageInput.Builder().messageType("text").text("").build();
    do {
      // Invio messaggio
      MessageOptions messageOptions = new MessageOptions.Builder(WatsonAuth.ASSISTANT_ID, sessionId).input(input).build();
      MessageResponse response = service.message(messageOptions).execute().getResult();

      // Stampo l'intent rilevato
      List<RuntimeIntent> responseIntents = response.getOutput().getIntents();
      if (responseIntents.size() > 0) {
        System.out.println("Detected intent: #" + responseIntents.get(0).intent());
      }
      //
      // Se l'intent rilevato è l'elenco delle posizioni aperte, stampare i risultati
      // ottenuti dal DB
      //
      // Stampo la risposta
      List<RuntimeResponseGeneric> responseGeneric = response.getOutput().getGeneric();
      if (responseGeneric.size() > 0) {
        for (int i = 0; i < responseGeneric.size(); i++) {
          if (responseGeneric.get(i).responseType().equals("text")) {
            System.out.println(responseGeneric.get(i).text());
          }
          if (responseGeneric.get(i).responseType().equals("option")) {
            System.out.println(responseGeneric.get(i).title());
            for (DialogNodeOutputOptionsElement opt : responseGeneric.get(i).options()) {
              System.out.println(opt.getLabel());
            }
          }
        }
      }

      // Prompt per il prossimo input
      System.out.print(">> ");
      String inputText = System.console().readLine();
      input = new MessageInput.Builder().messageType("text").text(inputText).build();
    } while (!input.text().equals("quit"));

    // Elimino la sessione
    DeleteSessionOptions deleteSessionOptions = new DeleteSessionOptions.Builder(WatsonAuth.ASSISTANT_ID, sessionId).build();
    service.deleteSession(deleteSessionOptions).execute();
    System.exit(0);
  }
}
