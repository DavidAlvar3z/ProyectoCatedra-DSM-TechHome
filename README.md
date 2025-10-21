# 🛍️ TechHome - Parte 2: Productos y Perfil de Usuario
**Desarrollado por: Ashley Valdez**

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=for-the-badge&logo=square&logoColor=white)
![Glide](https://img.shields.io/badge/Glide-00C4CC?style=for-the-badge&logo=android&logoColor=white)

## 📋 Descripción
Segunda fase del desarrollo de TechHome, enfocada en la experiencia de compra del usuario. Esta parte incluye el catálogo completo de productos, gestión de inventario, perfil de usuario y toda la funcionalidad core del e-commerce.

## ✨ Características Implementadas

### 🏠 Pantalla de Bienvenida
- ✅ Header con gradiente personalizado
- ✅ Grid de 4 categorías con diseños únicos
- ✅ Navegación inferior funcional
- ✅ Card de bienvenida con info del usuario

### 📦 Sistema de Productos
- ✅ Listado de productos en grid (2 columnas)
- ✅ Sincronización con Best Buy API
- ✅ Almacenamiento local en Firestore
- ✅ Gestión de inventario y stock
- ✅ Estados visuales de disponibilidad
- ✅ Chips de descuento y stock
- ✅ Ratings y reseñas

### 🔍 Detalle de Producto
- ✅ Vista completa con toda la información
- ✅ Imágenes de alta calidad
- ✅ Información de marca y modelo
- ✅ Cálculo automático de descuentos
- ✅ Estados de stock en tiempo real
- ✅ Botón "Agregar al carrito"
- ✅ Botón "Comprar ahora" con reducción de stock
- ✅ Enlace directo a Best Buy

### 👤 Perfil de Usuario
- ✅ Foto de perfil personalizada
- ✅ Subida de imágenes a Firebase Storage
- ✅ Formulario completo de información personal
- ✅ Campos: nombre, apellido, edad, sexo, teléfono, biografía
- ✅ Validaciones de campos
- ✅ Guardado en Firestore
- ✅ Opción de cerrar sesión

### 🎨 Adaptadores Personalizados
- ✅ ProductAdapter para API de Best Buy
- ✅ ProductLocalAdapter con gestión de stock
- ✅ ViewHolders optimizados
- ✅ Carga de imágenes con Glide
- ✅ Formateo de precios y fechas

## 🛠️ Tecnologías Utilizadas

### Frontend
- **UI Framework:** Material Design 3
- **Layouts:** ConstraintLayout, CardView, RecyclerView
- **Componentes:** Chips, FAB, BottomNavigationView
- **Imágenes:** Glide para carga eficiente

### Backend
- **Base de datos:** Cloud Firestore
- **Almacenamiento:** Firebase Storage
- **API Externa:** Best Buy Products API
- **Networking:** Retrofit 2 + OkHttp3

### Arquitectura
- **Patrón:** Repository Pattern
- **Manejo de estado:** LiveData-like callbacks
- **Transacciones:** Firestore Transactions para stock

## 📱 Pantallas Implementadas

1. **WelcomeActivity** - Dashboard con categorías
2. **ProductsActivity** - Listado de productos por categoría
3. **ProductDetailActivity** - Detalle completo del producto
4. **ProfileActivity** - Perfil de usuario editable

## 🎯 Funcionalidades Destacadas

### 🔄 Sincronización Inteligente
- Productos se sincronizan desde Best Buy
- Se guardan localmente en Firestore
- Generación automática de metadatos
- Stock aleatorio para simulación

### 📊 Gestión de Inventario
- Estados: En Stock, Pocas Unidades, Agotado
- Reducción de stock con transacciones
- Umbral de stock bajo configurable
- Validaciones de disponibilidad

### 💰 Sistema de Precios
- Precio regular y precio de oferta
- Cálculo automático de porcentaje de descuento
- Formato de moneda USD
- Visualización de ahorros

### ⭐ Sistema de Calificaciones
- Ratings de 1 a 5 estrellas
- Conteo de reseñas
- Generación aleatoria para demo

## 🔧 Configuración

### Prerrequisitos
- Android Studio Hedgehog o superior
- Kotlin 1.9+
- SDK mínimo: 24 (Android 7.0)
- SDK objetivo: 34 (Android 14)

### Setup
1. Clonar el repositorio
2. Checkout a la rama `products-and-profile-ui`
3. Agregar `google-services.json`
4. Configurar API Key de Best Buy en `BestBuyApiService.kt`
5. Sincronizar Gradle
6. Ejecutar en dispositivo/emulador

## 📂 Estructura de Archivos
```
app/src/main/
├── java/com/techhome/
│   ├── activities/
│   │   ├── WelcomeActivity.kt
│   │   ├── ProductsActivity.kt
│   │   ├── ProductDetailActivity.kt
│   │   └── ProfileActivity.kt
│   ├── adapters/
│   │   ├── ProductAdapter.kt
│   │   └── ProductLocalAdapter.kt
│   ├── models/
│   │   ├── ProductLocal.kt
│   │   └── StockStatus.kt
│   ├── network/
│   │   └── BestBuyApiService.kt
│   └── repository/
│       └── ProductRepository.kt
├── res/
│   ├── layout/
│   │   ├── activity_welcome.xml
│   │   ├── activity_products.xml
│   │   ├── activity_product_detail.xml
│   │   ├── activity_profile.xml
│   │   ├── item_product.xml
│   │   └── item_product_local.xml
│   ├── menu/
│   │   └── bottom_nav_menu.xml
│   └── drawable/
│       └── (todos los iconos y gradientes)
```

## 🎨 Categorías Disponibles

| Categoría | ID Best Buy | Icono | Gradiente |
|-----------|-------------|-------|-----------|
| 📱 Teléfonos | `abcat0800000` | ic_smartphone | Morado-Azul |
| 💻 Laptops | `abcat0502000` | ic_laptop | Verde |
| 🎧 Audio | `abcat0200000` | ic_headset | Amarillo-Rosa |
| ⌚ Smartwatches | `pcmcat748302045979` | ic_watch | Azul-Rosa |

## 📈 Mejoras Futuras

- [ ] Implementar carrito de compras funcional
- [ ] Sistema de favoritos
- [ ] Historial de compras
- [ ] Búsqueda y filtros avanzados
- [ ] Notificaciones push
- [ ] Pasarela de pago
- [ ] Reviews y comentarios de usuarios

## 🐛 Debugging

Los logs están habilitados en:
- `ProductRepository`: Tag "ProductRepository"
- `ProfileActivity`: Tag "ProfileActivity"
- Retrofit HTTP Logging: BODY level

## 👤 Desarrolladora

**Ashley Valdez**
- Commits: 16-30
- Enfoque: E-commerce, UI/UX, Gestión de Productos, Perfil

## 🤝 Integración con Parte 1

Esta parte se integra perfectamente con la infraestructura creada por David Alvarez:
- ✅ Usa los modelos de datos definidos
- ✅ Se conecta con Firebase Auth
- ✅ Utiliza el ProductRepository
- ✅ Mantiene el diseño Material Design 3
- ✅ Sigue los mismos patrones de arquitectura

---

**Fecha de inicio:** Octubre 2 - Octubre 10 (2025)
**Estado:** ✅ Fase 2 Completada  
**Rama:** `products-and-profile-ui`
