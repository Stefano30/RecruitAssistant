package it.allos.watson;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.Assistant;

public class SingleAssistant {

    public static final String ASSISTANT_ID = "b68d1fb2-35ca-415f-893e-8134484f11f9";
    public static final String API_KEY = "sYJ55YEoz8CDvulCnL0aFOsCF6VG_mFFHKx1Zbv2ST8b";
    public static final String SERVICE_URL = "https://api.eu-gb.assistant.watson.cloud.ibm.com/instances/8406380e-ddb7-4976-9b64-2d26cebbfd12";
    public static final String API_VERSION = "2020-07-17";

    private static Assistant assistantInstance = null;

    private SingleAssistant() {
    }

    public static Assistant getAssistant() {
        if (assistantInstance == null) {
            IamAuthenticator authenticator = new IamAuthenticator(API_KEY);
            assistantInstance = new Assistant(API_VERSION, authenticator);
            assistantInstance.setServiceUrl(SERVICE_URL);
        }
        return assistantInstance;
    }
}