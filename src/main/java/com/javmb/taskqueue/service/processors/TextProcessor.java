package com.javmb.taskqueue.service.processors;

import com.fasterxml.jackson.databind.JsonNode;
import com.javmb.taskqueue.model.TaskProcessor;
import com.javmb.taskqueue.model.Type;
import org.springframework.stereotype.Service;

/**
 * Procesador para tareas de transformación de texto.
 * Recibe un payload con un campo "text" y lo transforma a mayúsculas.
 * <p>
 * Ejemplo de payload: { "text": "hola mundo" }
 * Resultado: "HOLA MUNDO (longitud: 10)"
 */
@Service
public class TextProcessor implements TaskProcessor<String> {

    @Override
    public String handle(JsonNode payload) {

        if (payload == null || !payload.has("text")) {
            return "Error: payload debe contener el campo 'text'";
        }

        String text = payload.get("text").asText();
        if (text == null || text.isEmpty()) {
            return "Error: el campo 'text' no puede estar vacío";
        }

        String upperCase = text.toUpperCase();
        int length = text.length();

        return String.format("%s (longitud: %d)", upperCase, length);
    }

    @Override
    public Type getType() {
        return Type.TEXT_TRANSFORM;
    }
}