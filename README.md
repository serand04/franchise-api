# Franchise API

REST API reactiva para gestión de franquicias, desarrollada como prueba técnica para el rol de **Backend Developer en Accenture**.

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Framework | Spring Boot 3.2 + Spring WebFlux (Project Reactor) |
| Arquitectura | Clean Architecture |
| Persistencia | AWS DynamoDB (Enhanced Client) |
| Simulación local | LocalStack |
| Contenerización | Docker + Docker Compose |
| IaC | Terraform |
| Tests | JUnit 5 + StepVerifier + WebTestClient |
| Documentación API | Springdoc OpenAPI (Swagger UI) |

---

## Estructura del proyecto

```
franchise-api/
├── src/
│   ├── main/java/com/accenture/franchise/
│   │   ├── domain/
│   │   │   ├── model/          # Entidades del dominio
│   │   │   └── port/
│   │   │       ├── in/         # Puertos de entrada (use cases)
│   │   │       └── out/        # Puertos de salida (repositorios)
│   │   ├── application/
│   │   │   └── usecase/        # Implementación de casos de uso
│   │   ├── infrastructure/
│   │   │   ├── adapter/
│   │   │   │   └── dynamodb/   # Adaptador DynamoDB
│   │   │   └── config/         # Configuración de beans
│   │   └── api/
│   │       ├── handler/        # Handlers reactivos + DTOs
│   │       └── router/         # RouterFunctions
│   └── test/                   # Pruebas unitarias e integración
├── terraform/                  # IaC para DynamoDB en AWS
├── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## Prerequisitos

Asegúrate de tener instalado en tu sistema:

- **JDK 17+** → `java -version`
- **Maven 3.9+** → `mvn -version`
- **Docker Engine 24+** → `docker --version`
- **Docker Compose v2+** → `docker compose version`
- **Terraform 1.5+** → `terraform -version` *(solo para despliegue en AWS)*

En Linux Mint / Ubuntu puedes instalar Docker con:

```bash
sudo apt update && sudo apt install docker.io docker-compose-v2 -y
sudo usermod -aG docker $USER   # reinicia sesión después
```

---

## Despliegue local (recomendado para desarrollo)

El entorno local levanta la aplicación junto a **LocalStack**, que emula DynamoDB en tu máquina sin necesidad de credenciales AWS reales.

### 1. Clonar el repositorio

```bash
git clone https://github.com/<tu-usuario>/franchise-api.git
cd franchise-api
```

### 2. Levantar con Docker Compose

```bash
docker compose up --build
```

Esto realizará automáticamente:
- Construcción del JAR con Maven (multi-stage Docker build).
- Inicio de LocalStack exponiendo DynamoDB en `localhost:4566`.
- Inicio de la API en `localhost:8080`.
- Creación de la tabla `franchises` en DynamoDB al arrancar la aplicación.

### 3. Verificar que la API está activa

```bash
curl http://localhost:8080/api-docs
```

Abre la documentación interactiva en tu navegador:

```
http://localhost:8080/swagger-ui.html
```

---

## Despliegue sin Docker (desarrollo directo)

Si prefieres ejecutar la aplicación con Maven directamente, necesitas LocalStack corriendo en segundo plano.

### 1. Levantar solo LocalStack

```bash
docker compose up localstack -d
```

### 2. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

---

## Ejecutar los tests

```bash
mvn test
```

Los tests incluyen:

- **Pruebas unitarias de casos de uso** (`FranchiseUseCaseImplTest`) usando `StepVerifier` para validar el comportamiento reactivo de cada operación del dominio.
- **Pruebas de handlers WebFlux** (`FranchiseHandlerTest`) usando `WebTestClient` para validar los endpoints HTTP con mocks del caso de uso.

---

## Endpoints de la API

### Franquicias

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/franchises` | Crear franquicia |
| `PATCH` | `/api/v1/franchises/{franchiseId}` | Actualizar nombre de franquicia |
| `GET` | `/api/v1/franchises/{franchiseId}/top-stock-products` | Producto con más stock por sucursal |

### Sucursales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/franchises/{franchiseId}/branches` | Agregar sucursal |
| `PATCH` | `/api/v1/franchises/{franchiseId}/branches/{branchId}` | Actualizar nombre de sucursal |

### Productos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products` | Agregar producto |
| `DELETE` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}` | Eliminar producto |
| `PATCH` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` | Modificar stock |
| `PATCH` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` | Actualizar nombre del producto |

---

## Ejemplos de uso con cURL

### Crear una franquicia

```bash
curl -X POST http://localhost:8080/api/v1/franchises \
  -H "Content-Type: application/json" \
  -d '{"name": "McDonald'\''s Colombia"}'
```

### Agregar una sucursal

```bash
curl -X POST http://localhost:8080/api/v1/franchises/{franchiseId}/branches \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Norte"}'
```

### Agregar un producto

```bash
curl -X POST http://localhost:8080/api/v1/franchises/{franchiseId}/branches/{branchId}/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Big Mac", "stock": 150}'
```

### Consultar producto con más stock por sucursal

```bash
curl http://localhost:8080/api/v1/franchises/{franchiseId}/top-stock-products
```

---

## Despliegue en AWS (cuenta real)

La infraestructura completa en AWS incluye: **DynamoDB + ECR + VPC + ECS Fargate + ALB + IAM + CloudWatch Logs**, todo provisionado con Terraform.

### Arquitectura en AWS

```
Internet → ALB (puerto 80)
             ↓
        ECS Fargate (subnet privada)
          [franchise-api container]
             ↓
        DynamoDB (tabla franchises)
```

### Prerequisitos en Linux Mint

```bash
# AWS CLI v2
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip && sudo ./aws/install

# Terraform
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install terraform -y
```

### 1. Configurar credenciales AWS

```bash
aws configure
# AWS Access Key ID:     <tu-access-key>
# AWS Secret Access Key: <tu-secret-key>
# Default region:        us-east-1
# Default output format: json
```

### 2. Despliegue completo con un solo comando

```bash
chmod +x deploy.sh
./deploy.sh
```

El script ejecuta automáticamente:
1. Verifica prerequisitos y credenciales AWS
2. Provisiona toda la infraestructura con Terraform (VPC, ECR, ECS, ALB, DynamoDB)
3. Construye el JAR con Maven
4. Construye la imagen Docker y la sube a ECR
5. Fuerza un nuevo despliegue en ECS y espera que estabilice
6. Imprime las URLs finales de la API

### 3. Modos del script de despliegue

```bash
./deploy.sh                # Despliegue completo (infra + imagen + ECS)
./deploy.sh --infra-only   # Solo provisiona infraestructura Terraform
./deploy.sh --app-only     # Solo construye imagen y redespliega en ECS
./deploy.sh --destroy      # Destruye toda la infraestructura
```

### 4. Despliegue manual paso a paso

Si prefieres ejecutar cada paso por separado:

```bash
# Paso 1 — Provisionar infraestructura
cd terraform
terraform init
terraform plan -var="aws_region=us-east-1" -var="environment=dev"
terraform apply -auto-approve

# Paso 2 — Obtener URL del repositorio ECR
ECR_URL=$(terraform output -raw ecr_repository_url)
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
cd ..

# Paso 3 — Build del JAR
mvn package -DskipTests

# Paso 4 — Build y push de la imagen Docker
aws ecr get-login-password --region us-east-1 \
  | docker login --username AWS --password-stdin \
    "${AWS_ACCOUNT_ID}.dkr.ecr.us-east-1.amazonaws.com"

docker build -t franchise-api:latest .
docker tag franchise-api:latest "${ECR_URL}:latest"
docker push "${ECR_URL}:latest"

# Paso 5 — Forzar redespliegue en ECS
aws ecs update-service \
  --cluster franchise-api-cluster \
  --service franchise-api-service \
  --force-new-deployment \
  --region us-east-1
```

### URLs resultantes tras el despliegue

| Recurso | URL |
|---------|-----|
| API Base | `http://<alb-dns>/api/v1` |
| Swagger UI | `http://<alb-dns>/swagger-ui.html` |
| API Docs | `http://<alb-dns>/api-docs` |

El DNS del ALB se imprime al final del `deploy.sh` y también se puede consultar con:

```bash
cd terraform && terraform output alb_dns_name
```

### Variables de entorno disponibles

| Variable | Descripción | Default |
|----------|-------------|---------|
| `AWS_REGION` | Región de despliegue | `us-east-1` |
| `ENVIRONMENT` | Entorno (dev/staging/prod) | `dev` |
| `IMAGE_TAG` | Tag de la imagen Docker | `latest` |

```bash
AWS_REGION=us-east-1 ENVIRONMENT=prod ./deploy.sh
```

### Destruir la infraestructura

```bash
./deploy.sh --destroy
# o manualmente:
cd terraform && terraform destroy -auto-approve
```

---

## Infraestructura como Código — Recursos Terraform

| Recurso | Tipo | Descripción |
|---------|------|-------------|
| `aws_dynamodb_table` | DynamoDB | Tabla de franquicias con PITR habilitado |
| `aws_ecr_repository` | ECR | Registro privado de imágenes Docker |
| `aws_vpc` | VPC | Red privada con subnets públicas y privadas |
| `aws_ecs_cluster` | ECS | Cluster Fargate con Container Insights |
| `aws_ecs_task_definition` | ECS | Task con CPU 256 / Memoria 512 MB |
| `aws_ecs_service` | ECS | Servicio con 1 réplica (configurable) |
| `aws_lb` | ALB | Load balancer público |
| `aws_iam_role` | IAM | Roles de ejecución y tarea con permisos DynamoDB |
| `aws_cloudwatch_log_group` | CloudWatch | Logs del contenedor con retención 7 días |

---

## Entorno local

```bash
docker compose up --build    # Levanta app + LocalStack
docker compose down -v       # Detiene y limpia volúmenes
```