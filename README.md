# 🔐 TechHome – Autenticación y Pantalla de Bienvenida

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github)

![Status](https://img.shields.io/badge/Status-En%20Desarrollo-yellow?style=for-the-badge)
![Branch](https://img.shields.io/badge/Branch-Auth--and--welcome--ui-blue?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-1.0.0-brightgreen?style=for-the-badge)

</div>

## 📌 Descripción corta
Esta rama implementa el **flujo de autenticación** (registro e inicio de sesión) y la **pantalla de bienvenida** de la aplicación **TechHome**.  
El objetivo es permitir que los usuarios se registren, inicien sesión con su cuenta (correo o Google) y sean redirigidos a la interfaz principal de bienvenida.

---

## 📱 Pantallas desarrolladas

| Paso | Pantalla | Descripción |
|------|-----------|-------------|
| 1️⃣ | **Iniciar sesión** | Permite al usuario ingresar con correo y contraseña o mediante cuenta de Google. |
| 2️⃣ | **Registrarse** | Permite crear una cuenta con nombre, apellido, correo y contraseña. Incluye validaciones de campos y confirmación visual. |
| 3️⃣ | **Bienvenida** | Muestra el mensaje de bienvenida y navegación principal tras autenticación exitosa. |

---

## 🎯 Objetivos principales
- Implementar **autenticación funcional** usando **Firebase Authentication** (correo/contraseña y Google).
- Diseñar las pantallas basadas en los **mockups de Balsamiq**.
- Validar los campos de entrada (correo, contraseña, etc.).
- Gestionar la navegación entre las pantallas de registro, login y bienvenida.
- Mantener la sesión activa hasta cierre manual o expiración.

---

## 🧩 Estructura del módulo

```
📂 app/
┣ 📂 java/com/techhome/
┃ ┣ 📂 auth/
┃ ┃ ┣ LoginActivity.kt
┃ ┃ ┣ RegisterActivity.kt
┃ ┃ ┗ WelcomeActivity.kt
┃ ┗ 📂 utils/
┃   ┗ ValidationUtils.kt
┣ 📂 res/layout/
┃ ┣ activity_login.xml
┃ ┣ activity_register.xml
┃ ┗ activity_welcome.xml
┗ AndroidManifest.xml
```

---

## 🛠️ Tecnologías utilizadas

| Tipo | Herramienta / Tecnología |
|------|---------------------------|
| **Lenguaje** | Kotlin |
| **IDE** | Android Studio Narwal 2025.1.1 |
| **Autenticación** | Firebase Authentication |
| **Diseño UI** | XML clásico |
| **Control de versiones** | Git + GitHub |

---

## 🧠 Lógica implementada
- **Registro de usuarios:** creación de cuenta con validaciones (campos vacíos, formato de correo, longitud de contraseña).
- **Inicio de sesión:** validación de credenciales con Firebase y manejo de errores.
- **Inicio con Google:** integración con Firebase Google Sign-In (opcional).
- **Redirección automática:** si el usuario ya está autenticado, pasa directamente a la pantalla de bienvenida.
- **Cierre de sesión:** mediante menú o botón "Cerrar sesión".

---

---

## 👨‍💻 Desarrollador

<table>
  <tr>
    <td align="center">
      <img src="https://img.shields.io/badge/Developer-Full%20Stack-blue?style=flat-square" alt="Dev Badge"/><br />
      <b>David Alejandro Álvarez Moreira</b><br />
      <sub>UI, Autenticación y Lógica de negocio</sub>
    </td>
  </tr>
</table>

---

## 📄 Licencia

![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)

Este módulo forma parte del proyecto **TechHome**, bajo la licencia **Apache License 2.0**.  
Consulta más información en [https://www.apache.org/licenses/LICENSE-2.0](https://www.apache.org/licenses/LICENSE-2.0)

---

<div align="center">

**Desarrollado con ❤️ por el equipo TechHome**

![Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Built%20for-Android-3DDC84?style=flat-square&logo=android&logoColor=white)

</div>
