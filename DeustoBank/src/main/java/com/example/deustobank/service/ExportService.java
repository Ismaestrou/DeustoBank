package com.example.deustobank.service;

import com.example.deustobank.model.User;
import com.example.deustobank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private UserRepository userRepository;

    public byte[] exportUsersAsCsv() throws IOException {
        List<User> users = userRepository.findAll();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            writer.println("ID,Nombre Completo,DNI,Email,Teléfono,Rol,Estado,Intentos Fallidos");

            for (User u : users) {
                writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%d%n",
                        u.getId(),
                        safe(u.getFullName()),
                        safe(u.getDni()),
                        safe(u.getEmail()),
                        safe(u.getPhone()),
                        u.getRole(),
                        u.isActive() ? "ACTIVO" : "BLOQUEADO",
                        u.getFailedLoginAttempts()
                );
            }
        }
        return baos.toByteArray();
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\"", "\"\"");
    }
}
