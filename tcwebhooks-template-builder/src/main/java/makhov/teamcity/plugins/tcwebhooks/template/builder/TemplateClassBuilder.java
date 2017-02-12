package makhov.teamcity.plugins.tcwebhooks.template.builder;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;

public class TemplateClassBuilder {

    public void build(String templateName, String targetFileLocation) throws IOException {
        String camelName = StringUtils.capitalize(templateName);
        FieldSpec android = FieldSpec.builder(String.class, "android")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder(camelName)
                .addModifiers(Modifier.PUBLIC)
                .addField(android)
                .addField(String.class, "robot", Modifier.PRIVATE, Modifier.FINAL)
                .build();

        JavaFile javaFile = JavaFile.builder("webhook.teamcity.payload.template", helloWorld)
                .build();


        File classFile = new File(targetFileLocation);

        javaFile.writeTo(classFile);
    }
}

