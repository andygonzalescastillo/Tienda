# 🛒 Tienda Full-Stack Platform

<div align="center">
  <img src="https://img.shields.io/badge/Java_25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25" />
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/React_19-20232A?style=for-the-badge&logo=react&logoColor=61DAFB" alt="React 19" />
  <img src="https://img.shields.io/badge/Vite_7-646CFF?style=for-the-badge&logo=vite&logoColor=white" alt="Vite" />
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis" />
</div>

<br />

Una plataforma web con arquitectura escalable y moderna que incluye un sistema de autenticación ultra-seguro (JWT en cookies HttpOnly), sincronización de estado en tiempo real (WebSockets STOMP) y una gestión robusta de roles a través de un panel de administración dedicado.

## 🚀 Live Demo (Producción)

Prueba el sistema en vivo en los siguientes enlaces:

- 👤 **Frontend User:** [https://tu-enlace-user.com](https://tu-enlace-user.com)
- 👑 **Frontend Admin:** [https://tu-enlace-admin.com](https://tu-enlace-admin.com)

*(Opcional) Puedes ingresar al panel de administrador con estas credenciales de prueba:*
> **Email:** admin@gmail.com  
> **Contraseña:** Admin123!

> ℹ️ **Nota sobre la API:** La documentación interactiva de la API (Swagger/OpenAPI) no está expuesta en los enlaces de producción por motivos de seguridad y mejores prácticas. Solo está disponible ejecutando el proyecto en tu entorno local.

---

## 🏗️ Arquitectura del Proyecto

El proyecto sigue una arquitectura de micro-servicios frontend divididos, conectados a un cerebro central:

1. **`backend/` (API REST):** Construido en Spring Boot. Maneja toda la seguridad, persistencia de datos (PostgreSQL), caché de tokens y OTPs (Redis), y la conexión WebSocket en tiempo real.
2. **`frontend-user/`:** Aplicación cliente (React + Vite) donde los usuarios pueden registrarse, iniciar sesión (vía credenciales o OAuth2), recuperar contraseñas y ver su perfil.
3. **`frontend-admin/`:** Panel de control (React + Vite) para usuarios con rol `ADMIN`. Permite gestionar usuarios, ver sesiones activas, cambiar roles y activar/desactivar cuentas.

---

## ✨ Características Principales

- 🔐 **Seguridad Bancaria (JWT Stateless):** Los tokens de acceso se almacenan de forma segura en cookies `HttpOnly` y `Secure`. Gestión de sesiones mediante `RefreshToken` y lista negra (Blacklist) en Redis para revocación inmediata.
- 👨‍💻 **Gestión Avanzada de Sesiones:** Los usuarios pueden auditar desde qué dispositivos (navegador/SO) tienen sesiones activas y cerrar sesiones específicas o todas a la vez de forma remota.
- 🛡️ **Gestión de Roles y Estados:** Panel de control donde los administradores pueden visualizar usuarios, cambiar roles y activar o desactivar cuentas instantáneamente.
- ⚡ **Sincronización en Tiempo Real (WebSockets):** Mantiene la interfaz de usuario perfectamente sincronizada **tanto entre diferentes pestañas como entre diferentes navegadores o dispositivos**. Si un administrador bloquea a un usuario, el servidor emite un evento STOMP que fuerza el cierre de sesión inmediato en todas sus pantallas abiertas.
- 🌐 **Multi-Login (OAuth2):** Integración nativa para inicio de sesión con **Google, GitHub, Facebook y Microsoft**.
- 📧 **Sistema OTP por Email:** Recuperación de contraseña y verificación de cuenta utilizando códigos numéricos temporales (OTP) almacenados en Redis y enviados mediante la API REST de **Brevo**.
- 🧪 **100% Test Coverage en Backend:** El servidor cuenta con una suite de pruebas automatizadas (Unitarias y de Integración) asegurando estabilidad total ante refactorizaciones.
- 🚀 **Optimizado para GraalVM:** El backend soporta compilación a **Native Image** para arranques en milisegundos y menor consumo de RAM.

---

## 🛠️ Tech Stack

### Backend
- **Core:** Java 25, Spring Boot 4.x
- **Seguridad:** Spring Security, JWT (JSON Web Tokens), OAuth2 Client
- **Base de Datos:** Spring Data JPA, PostgreSQL, H2 (para tests)
- **Caché & Sesiones:** Redis (Data Redis)
- **Mensajería:** Spring WebSocket (STOMP)
- **Integraciones:** Brevo REST API (Mailing)

### Frontends (Admin & User)
- **Core:** React 19, Vite
- **Estilos:** Tailwind CSS 4, shadcn/ui, Radix UI
- **Estado y Fetching:** Zustand, React Query (@tanstack/react-query), Axios
- **Formularios:** React Hook Form, Zod (Validación)
- **WebSockets:** `@stomp/stompjs`

---

## 💻 Prerrequisitos (Desarrollo Local)

Para correr el proyecto en tu máquina necesitas:
- Java 25 o superior.
- Node.js 22 o superior.
- **Docker Desktop** (Altamente recomendado para levantar PostgreSQL y Redis sin instalar nada más).
- *(Alternativa a Docker)*: PostgreSQL (puerto 5432) y Redis (puerto 6379) instalados nativamente.

---

## ⚙️ Instalación y Ejecución

### 1. Levantar el Backend
Gracias a `spring-boot-docker-compose`, **no necesitas levantar las bases de datos manualmente**. Al arrancar el proyecto, Spring Boot detectará el archivo `docker-compose.yml`, levantará PostgreSQL y Redis por ti, y los apagará cuando detengas la aplicación.

1. Navega a la carpeta del backend: `cd backend`
2. Duplica el archivo `.env.example`, renómbralo a `.env` y llena las variables requeridas (API Key de Brevo, Credenciales OAuth2, etc.).
3. Compila y arranca el servidor Spring Boot (esto encenderá Docker automáticamente):
   ```bash
   ./mvnw spring-boot:run
   ```

### 2. Levantar los Frontends
En terminales separadas, haz lo mismo para `frontend-admin` y `frontend-user`:
1. Navega a la carpeta: `cd frontend-admin` (o `frontend-user`)
2. Duplica el archivo `.env.example`, renómbralo a `.env` y asegúrate de que las URLs apunten al backend.
3. Instala dependencias:
   ```bash
   npm install
   ```
4. Ejecuta el servidor de desarrollo:
   ```bash
   npm run dev
   ```

---

## 🧪 Pruebas (Testing)

El backend incluye una suite completa de pruebas (Controllers, Services, Repositories, Security, WebSockets) con 100% de cobertura funcional.
Para ejecutar todos los tests, navega a la carpeta `backend/` y corre:

```bash
./mvnw test
```

---

## 📁 Estructura del Repositorio

```text
tienda/
├── backend/                # Spring Boot REST API
│   ├── src/main/java/      # Código fuente (Controllers, Services, Security, Events)
│   ├── src/main/resources/ # application.yml, templates Thymeleaf (Mailing)
│   └── src/test/           # Tests Unitarios y de Integración
├── frontend-admin/         # React App para Administradores
│   ├── src/components/     # UI Components (shadcn/ui)
│   ├── src/pages/          # Vistas (Dashboard, Usuarios, Configuración)
│   └── src/store/          # Zustand global state
├── frontend-user/          # React App para Clientes Públicos
│   └── src/                # Vistas y lógica de cliente
└── README.md               # Este archivo
```
