# 📱 TechHome

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-En%20Desarrollo-yellow?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-1.0.0-brightgreen?style=for-the-badge)

</div>

## 📌 Descripción corta
TechHome es una aplicación móvil en Kotlin que ofrece un catálogo digital de electrodomésticos, permitiendo a clientes consultar precios y disponibilidad, y a vendedores y administradores gestionar inventario y ventas en tiempo real.

## 📌 Descripción
TechHome es una aplicación móvil desarrollada en Kotlin cuyo propósito es modernizar la gestión y experiencia de compra en una tienda de electrodomésticos.
Actualmente, la tienda presenta dificultades por la falta de un catálogo digital, la gestión manual de inventarios y la demora en la atención al cliente.

La app permitirá a los clientes explorar productos en un catálogo digital actualizado, mientras que los administradores y vendedores contarán con herramientas para gestionar inventarios y registrar ventas en tiempo real.

## 🎯 Objetivos
- Desarrollar un catálogo digital con fotos, descripciones, precios y disponibilidad en tiempo real.
- Permitir a los clientes filtrar productos por categoría, marca y rango de precio.
- Facilitar a los administradores y vendedores un módulo de gestión de inventario (CRUD).
- Implementar un sistema de registro de ventas que actualice el stock automáticamente.
- Reducir tiempos de atención y errores en la gestión interna de la tienda.

## 👥 Roles
- **Clientes**: Exploran catálogo, consultan disponibilidad, buscan y filtran productos.
- **Vendedores**: Registran ventas y consultan inventario en tiempo real.
- **Administradores**: Gestionan productos, precios y stock mediante el módulo CRUD.

## 📌 Beneficios Esperados
- Mejor experiencia de compra para los clientes.
- Mayor control y precisión en la gestión de inventarios.
- Reducción del tiempo de atención al cliente y de los errores en ventas.
- Incremento de la competitividad y de las ventas de la tienda.

## 🛠️ Herramientas Tecnológicas

Para garantizar un desarrollo eficiente, organizado y de alta calidad de la aplicación móvil TechHome, se utilizarán las siguientes tecnologías y herramientas, clasificadas según su propósito:

### 1. Entorno de desarrollo integrado (IDE)
![Android Studio](https://img.shields.io/badge/Android%20Studio-Narwal%202025.1.1-3DDC84?style=flat-square&logo=androidstudio)

**Android Studio – versión Narwal 2025.1.1**  
IDE principal para el desarrollo en Kotlin. Permite codificación, diseño de interfaces, depuración y simulación en múltiples dispositivos Android.

### 2. Lenguaje de programación y frameworks
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-orange?style=flat-square)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Optional-4285F4?style=flat-square&logo=jetpackcompose)
![Coroutines](https://img.shields.io/badge/Coroutines-Flow-purple?style=flat-square)

- **Kotlin** – Lenguaje principal de desarrollo.
- **MVVM (Model-View-ViewModel)** – Patrón de arquitectura para separar la lógica de negocio de la UI.
- **Jetpack Compose (opcional)** – UI declarativa moderna que facilita interfaces dinámicas.
- **Coroutines / Flow (a considerar)** – Para manejo de operaciones asíncronas y flujos de datos de manera eficiente.

### 3. Base de datos
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Room](https://img.shields.io/badge/Room-Optional-blue?style=flat-square)
![Firestore](https://img.shields.io/badge/Firestore-FFCA28?style=flat-square&logo=firebase)

- **MySQL / MySQL Workbench** – Base de datos relacional centralizada para productos, ventas y usuarios.
- **Room (opcional)** – Base de datos local para carrito de compras, historial offline y preferencias.
- **Firebase Firestore (a considerar)** – Alternativa NoSQL en la nube para sincronización en tiempo real de inventario y ventas.

### 4. Control de versiones y colaboración
![Git](https://img.shields.io/badge/Git-F05032?style=flat-square&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)

- **Git y GitHub** – Versionado del código fuente y colaboración entre integrantes.
- **GitHub Actions (a considerar)** – Para integraciones y pruebas automáticas al subir cambios.

### 5. Gestión de tareas y planificación
![Trello](https://img.shields.io/badge/Trello-0052CC?style=flat-square&logo=trello&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=flat-square&logo=notion&logoColor=white)

- **Trello** – Organización de tareas, asignación de responsables y seguimiento de sprints.  
  [📋 Link del tablero](https://trello.com/invite/b/68ab4256a40f6f021225983d/ATTIc5a8a6c9605b18540b91344929eda0adDA2DEEFA/dsm-proyecto-de-catedra-fase-1-fase-2)
- **Notion (a considerar)** – Documentación y seguimiento de tareas más detallado, con integración de bases de conocimiento del proyecto.

### 6. Pruebas y calidad
![JUnit](https://img.shields.io/badge/JUnit-25A162?style=flat-square&logo=junit5&logoColor=white)
![Espresso](https://img.shields.io/badge/Espresso-Testing-green?style=flat-square)
![SonarQube](https://img.shields.io/badge/SonarQube-Optional-4E9BCD?style=flat-square&logo=sonarqube)

- **JUnit / Espresso** – Pruebas unitarias y de integración de funciones críticas.
- **Lint / SonarQube (opcional)** – Análisis de código estático para detectar errores y mejorar calidad.
- **Mockito (a considerar)** – Para pruebas unitarias con simulación de objetos y escenarios complejos.

### 7. Diseño y prototipado
![Balsamiq](https://img.shields.io/badge/Balsamiq-CC0100?style=flat-square&logo=balsamiq&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=flat-square&logo=figma&logoColor=white)
![Adobe XD](https://img.shields.io/badge/Adobe%20XD-FF61F6?style=flat-square&logo=adobexd&logoColor=white)

- **Balsamiq** – Creación de mockups y prototipos para validar flujo de usuario.
- **Figma (opcional)** – Prototipos interactivos y colaboración en diseño de UI moderna.
- **Adobe XD (a considerar)** – Alternativa avanzada para prototipado y pruebas de usabilidad.

#### 📌 Mockups (rama dedicada en el repositorio: "mockups")
- **Home, Menú principal, Categorías, Ofertas destacadas**  
  [🎨 Ver en Figma](https://www.figma.com/design/LPRizpJ6zyi56dp81hTQu0/Sin-t%C3%ADtulo?node-id=0-1&t=cvkfUyoL3DcPPEbE-1)
- **Login, Registro, Bienvenida**  
  [🎨 Ver en Balsamiq](https://balsamiq.cloud/sk56pdi/pdim7qr)
- **Catálogo, Búsqueda, Filtros**  
  [🎨 Ver en Figma](https://www.figma.com/design/LPRizpJ6zyi56dp81hTQu0/Sin-t%C3%ADtulo?node-id=0-1&t=cvkfUyoL3DcPPEbE-1)
- **Detalle producto, Carrito, Agregar/Quitar productos**  
  [🎨 Ver en Balsamiq](https://balsamiq.cloud/sgno1zh/pr81jyw/r2278)

### 8. Comunicaciones y documentación
![Google Drive](https://img.shields.io/badge/Google%20Drive-4285F4?style=flat-square&logo=googledrive&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-Optional-4A154B?style=flat-square&logo=slack)
![Discord](https://img.shields.io/badge/Discord-Optional-5865F2?style=flat-square&logo=discord&logoColor=white)

- **Google Drive / OneDrive** – Respaldo de documentación y entregables.
- **Slack / Discord (opcional)** – Comunicación rápida y coordinación del equipo.
- **Confluence (a considerar)** – Documentación técnica estructurada del proyecto y manuales de usuario.

### 9. Seguridad y autenticación
![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)
![HTTPS](https://img.shields.io/badge/HTTPS-SSL-green?style=flat-square&logo=letsencrypt)
![OAuth2](https://img.shields.io/badge/OAuth2-Optional-EB5424?style=flat-square)

- **JWT (JSON Web Tokens)** – Autenticación y autorización por rol (Cliente, Vendedor, Administrador).
- **HTTPS / SSL** – Seguridad en la comunicación con el backend.
- **OAuth2 (a considerar)** – Para integración futura de login con cuentas externas (Google, Facebook).

## 👨‍💻 Equipo de Desarrollo

<table>
  <tr>
    <td align="center">
      <img src="https://img.shields.io/badge/Developer-Frontend%2FBackend-blue?style=flat-square" alt="Dev Badge"/><br />
      <b>Waldo José Pérez Aguillon</b>
    </td>
    <td align="center">
      <img src="https://img.shields.io/badge/Developer-Frontend%2FBackend-blue?style=flat-square" alt="Dev Badge"/><br />
      <b>Camila Elizabeth Castillo Joya</b>
    </td>
    <td align="center">
      <img src="https://img.shields.io/badge/Developer-Frontend%2FBackend-blue?style=flat-square" alt="Dev Badge"/><br />
      <b>David Alejandro Alvarez Moreira</b>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://img.shields.io/badge/Developer-Frontend%2FBackend-blue?style=flat-square" alt="Dev Badge"/><br />
      <b>Caleb Alejandro Peñate Deras</b>
    </td>
    <td align="center">
      <img src="https://img.shields.io/badge/Developer-Frontend%2FBackend-blue?style=flat-square" alt="Dev Badge"/><br />
      <b>Ashley Gabriela Valdez González</b>
    </td>
  </tr>
</table>

## 📄 Licencia

![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)

Este proyecto está bajo la licencia **Apache License 2.0**.  
Puedes usar, modificar y distribuir este software de manera libre, siempre y cuando incluyas una copia de la licencia original y mantengas los avisos de copyright.

Para más detalles consulta el archivo LICENSE: https://www.apache.org/licenses/LICENSE-2.0

---

<div align="center">

**Desarrollado con ❤️ por el equipo TechHome**

![Made with Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Built for Android](https://img.shields.io/badge/Built%20for-Android-3DDC84?style=flat-square&logo=android&logoColor=white)

</div>
