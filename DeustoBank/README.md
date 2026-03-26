# 💳 DeustoBank
http://localhost:8080/swagger-ui/index.html#/

http://localhost:8080

# 💳 DeustoBank BD
http://localhost:8080/h2-console
Aplicación web bancaria desarrollada con Spring Boot y HTML/CSS/JavaScript.

---

## 🚀 Funcionalidades

- Registro de usuarios (email, DNI, contraseña)
- Inicio de sesión seguro
- Creación de cuentas bancarias
- Ingresos y retiradas
- Transferencias entre cuentas
- Eliminación de cuentas (si no hay deuda)

---

## 🛠️ Tecnologías utilizadas

- Backend: Spring Boot (Java)
- Base de datos: H2
- Frontend: HTML, CSS, JavaScript
- API REST

---

## 🔐 Autenticación

El sistema utiliza:
- DNI + contraseña
- Contraseñas encriptadas (BCrypt)

---

## 📡 API REST (Ejemplos)

### Login
POST /auth/login?dni=12345678A&password=1234

### Crear cuenta
POST /accounts?userId=1

### Transferencia
POST /accounts/transfer?fromId=1&toId=2&amount=100

---

## 💻 Ejecución del proyecto

1. Clonar repositorio:
```bash
git clone https://github.com/tu-usuario/DeustoBank


