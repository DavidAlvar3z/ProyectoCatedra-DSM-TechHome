# 📱 TechHome

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

- Clientes: Exploran catálogo, consultan disponibilidad, buscan y filtran productos.
- Vendedores: Registran ventas y consultan inventario en tiempo real.
- Administradores: Gestionan productos, precios y stock mediante el módulo CRUD.

## 📌 Beneficios Esperados

- Mejor experiencia de compra para los clientes.
- Mayor control y precisión en la gestión de inventarios.
- Reducción del tiempo de atención al cliente y de los errores en ventas.
- Incremento de la competitividad y de las ventas de la tienda.

## 🛠️ Herramientas Tecnológicas

Para garantizar un desarrollo eficiente, organizado y de alta calidad de la aplicación móvil TechHome, se utilizarán las siguientes tecnologías y herramientas, clasificadas según su propósito:

1. Entorno de desarrollo integrado (IDE)
- Android Studio – versión Narval 2025.1.1
IDE principal para el desarrollo en Kotlin. Permite codificación, diseño de interfaces, depuración y simulación en múltiples dispositivos Android.

2. Lenguaje de programación y frameworks
- Kotlin – Lenguaje principal de desarrollo.
- MVVM (Model-View-ViewModel) – Patrón de arquitectura para separar la lógica de negocio de la UI.
- Jetpack Compose (opcional) – UI declarativa moderna que facilita interfaces dinámicas.
- Coroutines / Flow (a considerar) – Para manejo de operaciones asíncronas y flujos de datos de manera eficiente.

3. Base de datos
- MySQL / MySQL Workbench – Base de datos relacional centralizada para productos, ventas y usuarios.
- Room (opcional) – Base de datos local para carrito de compras, historial offline y preferencias.
- Firebase Firestore (a considerar) – Alternativa NoSQL en la nube para sincronización en tiempo real de inventario y ventas.

4. Control de versiones y colaboración
- Git y GitHub – Versionado del código fuente y colaboración entre integrantes.
- GitHub Actions (a considerar) – Para integraciones y pruebas automáticas al subir cambios.

5. Gestión de tareas y planificación
- Trello – Organización de tareas, asignación de responsables y seguimiento de sprints.
- Notion (a considerar) – Documentación y seguimiento de tareas más detallado, con integración de bases de conocimiento del proyecto.

6. Pruebas y calidad
- JUnit / Espresso – Pruebas unitarias y de integración de funciones críticas.
- Lint / SonarQube (opcional) – Análisis de código estático para detectar errores y mejorar calidad.
- Mockito (a considerar) – Para pruebas unitarias con simulación de objetos y escenarios complejos.

7. Diseño y prototipado
- Balsamiq – Creación de mockups y prototipos para validar flujo de usuario.
- Figma (opcional) – Prototipos interactivos y colaboración en diseño de UI moderna.
- Adobe XD (a considerar) – Alternativa avanzada para prototipado y pruebas de usabilidad.

8. Comunicaciones y documentación
- Google Drive / OneDrive – Respaldo de documentación y entregables.
- Slack / Discord (opcional) – Comunicación rápida y coordinación del equipo.
- Confluence (a considerar) – Documentación técnica estructurada del proyecto y manuales de usuario.

9. Seguridad y autenticación
- JWT (JSON Web Tokens) – Autenticación y autorización por rol (Cliente, Vendedor, Administrador).
- HTTPS / SSL – Seguridad en la comunicación con el backend.
- OAuth2 (a considerar) – Para integración futura de login con cuentas externas (Google, Facebook).

## 👨‍💻 Equipo de Desarrollo

- Waldo José Pérez Aguillon – Programador Frontend/Backend
- Camila Elizabeth Castillo Joya – Programadora Frontend/Backend
- David Alejandro Alvarez Moreira – Programador Frontend/Backend
- Caleb Alejandro Peñate Deras – Programador Frontend/Backend
- Ashley Gabriela Valdez González – Programadora Frontend/Backend

## 📄 Licencia

Este proyecto está bajo la licencia Apache License 2.0.
Puedes usar, modificar y distribuir este software de manera libre, siempre y cuando incluyas una copia de la licencia original y mantengas los avisos de copyright.

Para más detalles consulta el archivo LICENSE: https://www.apache.org/licenses/LICENSE-2.0
