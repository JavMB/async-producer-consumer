package com.javmb.taskqueue.service.processors;

import com.fasterxml.jackson.databind.JsonNode;
import com.javmb.taskqueue.model.TaskProcessor;
import com.javmb.taskqueue.model.Type;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Procesador para tareas de cálculo de hash SHA-256.
 * Recibe un payload con un campo "text" y calcula su hash.
 * <p>
 * Ejemplo de payload: { "text": "hola mundo" }
 * Resultado: "aed2e80e5c2e5c9c5e5d2e80e..." (hash SHA-256 en hexadecimal)
 */
@Service
public class HashProcessor implements TaskProcessor<String> {

    @Override
    public String handle(JsonNode payload) {

        if (payload == null || !payload.has("text")) {
            return "Error: payload debe contener el campo 'text'";
        }

        String text = payload.get("text").asText();
        if (text == null || text.isEmpty()) {
            return "Error: el campo 'text' no puede estar vacío";
        }

        try {
            //SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            //a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "Error: algoritmo SHA-256 no disponible - " + e.getMessage();
        }
    }

    @Override
    public Type getType() {
        return Type.COMPUTE_HASH;
    }
}

