# DeustoBank

DeustoBank es una aplicación de banca digital moderna y segura, desarrollada con Spring Boot y una arquitectura basada en microservicios.

##  Configuración del Proyecto

El proyecto está construido sobre el ecosistema Java y utiliza las siguientes tecnologías clave:

- **Framework Principal:** [Spring Boot 3.4.1](https://spring.io/projects/spring-boot)
- **Lenguaje:** Java 17
- **Gestión de Dependencias:** Maven
- **Base de Datos:** H2 (En memoria, para desarrollo y pruebas rápidas)
- **Persistencia:** Spring Data JPA / Hibernate
- **Seguridad:** Spring Security (BCrypt para cifrado de contraseñas)
- **Documentación:** Springdoc OpenAPI (Swagger UI)

### Accesos Rápidos (Entorno de Desarrollo)

- **Aplicación:** [http://localhost:8080](http://localhost:8080)
- **Swagger UI (Documentación API):** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Consola H2 (Base de Datos):** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
    - **JDBC URL:** `jdbc:h2:mem:deustobank`
    - **User:** `sa`
    - **Password:** *(vacío)*

---

##  Instrucciones de Construcción

Para construir y ejecutar el proyecto desde el código fuente, sigue estos pasos utilizando el Maven Wrapper incluido (`mvnw`).

### Requisitos Previos

- Tener instalado **Java 17** o superior.
- Configurar la variable de entorno `JAVA_HOME` apuntando a tu JDK 17.

### Comandos de Construcción

1.  **Limpiar y Compilar:**
    ```powershell
    ./mvnw clean compile
    ```

2.  **Ejecutar Tests:**
    ```powershell
    ./mvnw test
    ```

3.  **Generar Artefacto (JAR):**
    ```powershell
    ./mvnw clean package
    ```
    El archivo generado se ubicará en la carpeta `target/DeustoBank-0.0.1-SNAPSHOT.jar`.


##  Proceso de Despliegue desde Cero

Sigue este flujo de trabajo para poner en marcha la aplicación en un entorno local de manera profesional:

### Paso 1: Clonar y Preparar
Asegúrate de estar en la raíz del proyecto. El proyecto utiliza un repositorio Git, por lo que puedes verificar el estado de los archivos con `git status`.

### Paso 2: Construcción del Proyecto
Ejecuta la construcción completa del ciclo de vida de Maven para asegurar que todas las dependencias se descarguen y los tests pasen:
```powershell
./mvnw clean verify
```

### Paso 3: Ejecución de la Aplicación
Puedes iniciar el servidor directamente mediante el plugin de Spring Boot:
```powershell
./mvnw spring-boot:run
```
Una vez que veas el mensaje `Started DeustoBankApplication in X seconds`, la aplicación estará lista para recibir peticiones en el puerto `8080`.

### Paso 4: Verificación
Accede a [http://localhost:8080](http://localhost:8080) para interactuar con la interfaz frontend y a la documentación de Swagger para probar los endpoints del backend de manera aislada.
