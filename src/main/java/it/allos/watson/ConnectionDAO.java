package it.allos.watson;

import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.DeleteSessionOptions;

public class ConnectionDAO {

    public static String getSessionID() {
        CreateSessionOptions createSessionOptions = new CreateSessionOptions.Builder(SingleAssistant.ASSISTANT_ID).build();
        return SingleAssistant.getAssistant().createSession(createSessionOptions).execute().getResult().getSessionId();
    }

    public static void deleteSession(String sessionID) {
        if (sessionID != null) {
            DeleteSessionOptions options = new DeleteSessionOptions.Builder(SingleAssistant.ASSISTANT_ID, sessionID).build();
            SingleAssistant.getAssistant().deleteSession(options).execute();
        }
    }
}