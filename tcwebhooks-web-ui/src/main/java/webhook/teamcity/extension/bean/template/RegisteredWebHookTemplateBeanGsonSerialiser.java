package webhook.teamcity.extension.bean.template;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RegisteredWebHookTemplateBeanGsonSerialiser {

    private RegisteredWebHookTemplateBeanGsonSerialiser() {
    }

    public static String serialise(RegisteredWebHookTemplateBean templates) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(templates);
    }

}
