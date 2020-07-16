package it.allos.watson;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.SessionResponse;

public class WatsonAuth {

    public static final String ASSISTANT_ID = "b68d1fb2-35ca-415f-893e-8134484f11f9";
    public static final String API_KEY = "sYJ55YEoz8CDvulCnL0aFOsCF6VG_mFFHKx1Zbv2ST8b";
    public static final String SERVICE_URL = "https://api.eu-gb.assistant.watson.cloud.ibm.com/instances/8406380e-ddb7-4976-9b64-2d26cebbfd12";

    public static SessionResponse getSession(Assistant service) {
        // Creare la sessione
        CreateSessionOptions createSessionOptions = new CreateSessionOptions.Builder(ASSISTANT_ID).build();
        return service.createSession(createSessionOptions).execute().getResult();
    }

    public static Assistant makeAssistant() { //TODO: crea singleton per l'assistant
        // Autenticazione
        IamAuthenticator authenticator = new IamAuthenticator(API_KEY);
        Assistant service = new Assistant("2020-07-10", authenticator);
        service.setServiceUrl(SERVICE_URL);
        return service;
    }

}