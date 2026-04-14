#!/bin/bash
# =============================================================================
# deploy.sh — Script de despliegue completo en AWS
#
# Uso:
#   chmod +x deploy.sh
#   ./deploy.sh                          # despliegue completo
#   ./deploy.sh --infra-only             # solo provisiona infraestructura
#   ./deploy.sh --app-only               # solo construye y despliega imagen
#   ./deploy.sh --destroy                # destruye toda la infraestructura
# =============================================================================

set -euo pipefail

# ── Colores para output ───────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log()    { echo -e "${BLUE}[INFO]${NC}  $1"; }
ok()     { echo -e "${GREEN}[OK]${NC}    $1"; }
warn()   { echo -e "${YELLOW}[WARN]${NC}  $1"; }
error()  { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# ── Variables configurables ───────────────────────────────────────────────────
AWS_REGION="${AWS_REGION:-us-east-1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"
APP_NAME="franchise-api"
TERRAFORM_DIR="./terraform"
IMAGE_TAG="${IMAGE_TAG:-latest}"

# ── Verificar prerequisitos ───────────────────────────────────────────────────
check_prerequisites() {
  log "Verificando prerequisitos..."

  command -v aws       >/dev/null 2>&1 || error "AWS CLI no encontrado. Instálalo: https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2-linux.html"
  command -v docker    >/dev/null 2>&1 || error "Docker no encontrado."
  command -v terraform >/dev/null 2>&1 || error "Terraform no encontrado."
  command -v mvn       >/dev/null 2>&1 || error "Maven no encontrado."

  # Verificar credenciales AWS activas
  aws sts get-caller-identity --region "$AWS_REGION" > /dev/null 2>&1 \
    || error "Credenciales AWS no válidas. Ejecuta: aws configure"

  AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
  ok "Credenciales AWS OK — Cuenta: $AWS_ACCOUNT_ID"
}

# ── Infraestructura con Terraform ─────────────────────────────────────────────
provision_infrastructure() {
  log "Provisionando infraestructura con Terraform..."

  cd "$TERRAFORM_DIR"

  terraform init -input=false

  terraform plan \
    -var="aws_region=$AWS_REGION" \
    -var="environment=$ENVIRONMENT" \
    -out=tfplan \
    -input=false

  terraform apply -input=false -auto-approve tfplan

  # Capturar outputs
  ECR_URL=$(terraform output -raw ecr_repository_url)
  ALB_DNS=$(terraform output -raw alb_dns_name)
  ECS_CLUSTER=$(terraform output -raw ecs_cluster_name)
  ECS_SERVICE=$(terraform output -raw ecs_service_name)

  cd ..

  ok "Infraestructura provisionada correctamente."
  log "ECR URL:     $ECR_URL"
  log "API URL:     $ALB_DNS"
}

# ── Build y push de imagen Docker ─────────────────────────────────────────────
build_and_push_image() {
  log "Construyendo JAR con Maven..."
  mvn package -DskipTests -q
  ok "JAR construido."

  log "Obteniendo datos de ECR desde Terraform outputs..."
  cd "$TERRAFORM_DIR"
  ECR_URL=$(terraform output -raw ecr_repository_url)
  cd ..

  AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

  log "Autenticando Docker con ECR..."
  aws ecr get-login-password --region "$AWS_REGION" \
    | docker login --username AWS --password-stdin \
      "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
  ok "Autenticación Docker OK."

  log "Construyendo imagen Docker..."
  docker build -t "${APP_NAME}:${IMAGE_TAG}" .
  docker tag "${APP_NAME}:${IMAGE_TAG}" "${ECR_URL}:${IMAGE_TAG}"
  ok "Imagen construida: ${ECR_URL}:${IMAGE_TAG}"

  log "Subiendo imagen a ECR..."
  docker push "${ECR_URL}:${IMAGE_TAG}"
  ok "Imagen subida a ECR."
}

# ── Forzar nuevo despliegue en ECS ────────────────────────────────────────────
deploy_to_ecs() {
  log "Forzando nuevo despliegue en ECS..."

  cd "$TERRAFORM_DIR"
  ECS_CLUSTER=$(terraform output -raw ecs_cluster_name)
  ECS_SERVICE=$(terraform output -raw ecs_service_name)
  ALB_DNS=$(terraform output -raw alb_dns_name)
  SWAGGER_URL=$(terraform output -raw swagger_ui_url)
  cd ..

  aws ecs update-service \
    --cluster "$ECS_CLUSTER" \
    --service "$ECS_SERVICE" \
    --force-new-deployment \
    --region "$AWS_REGION" \
    --output text > /dev/null

  log "Esperando que el servicio estabilice (puede tomar ~2 min)..."
  aws ecs wait services-stable \
    --cluster "$ECS_CLUSTER" \
    --services "$ECS_SERVICE" \
    --region "$AWS_REGION"

  ok "Despliegue completado exitosamente."
  echo ""
  echo -e "${GREEN}════════════════════════════════════════${NC}"
  echo -e "${GREEN}  API desplegada en AWS ECS Fargate      ${NC}"
  echo -e "${GREEN}════════════════════════════════════════${NC}"
  echo -e "  API Base URL:  ${BLUE}${ALB_DNS}/api/v1${NC}"
  echo -e "  Swagger UI:    ${BLUE}${SWAGGER_URL}${NC}"
  echo -e "  API Docs:      ${BLUE}${ALB_DNS}/api-docs${NC}"
  echo -e "${GREEN}════════════════════════════════════════${NC}"
}

# ── Destruir infraestructura ──────────────────────────────────────────────────
destroy_infrastructure() {
  warn "¡ATENCIÓN! Esto destruirá TODOS los recursos AWS creados por Terraform."
  read -p "¿Estás seguro? Escribe 'yes' para confirmar: " confirm
  [[ "$confirm" == "yes" ]] || { log "Operación cancelada."; exit 0; }

  cd "$TERRAFORM_DIR"
  terraform destroy \
    -var="aws_region=$AWS_REGION" \
    -var="environment=$ENVIRONMENT" \
    -auto-approve
  cd ..

  ok "Infraestructura destruida."
}

# ── Main ──────────────────────────────────────────────────────────────────────
MODE="${1:-full}"

check_prerequisites

case "$MODE" in
  --infra-only)
    provision_infrastructure
    ;;
  --app-only)
    build_and_push_image
    deploy_to_ecs
    ;;
  --destroy)
    destroy_infrastructure
    ;;
  full|*)
    provision_infrastructure
    build_and_push_image
    deploy_to_ecs
    ;;
esac