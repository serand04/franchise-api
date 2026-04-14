variable "aws_region" {
  description = "AWS region donde se despliegan los recursos"
  type        = string
  default     = "us-east-1"
}

variable "app_name" {
  description = "Nombre base de la aplicación (usado en todos los recursos)"
  type        = string
  default     = "franchise-api"
}

variable "environment" {
  description = "Entorno de despliegue: dev, staging, prod"
  type        = string
  default     = "dev"
}

# ── DynamoDB ──────────────────────────────────────────────────────────────────

variable "table_name" {
  description = "Nombre de la tabla DynamoDB"
  type        = string
  default     = "franchises"
}

variable "billing_mode" {
  description = "Modo de facturación DynamoDB: PAY_PER_REQUEST o PROVISIONED"
  type        = string
  default     = "PAY_PER_REQUEST"

  validation {
    condition     = contains(["PAY_PER_REQUEST", "PROVISIONED"], var.billing_mode)
    error_message = "billing_mode debe ser PAY_PER_REQUEST o PROVISIONED."
  }
}

variable "read_capacity" {
  description = "Unidades de lectura (solo para modo PROVISIONED)"
  type        = number
  default     = 5
}

variable "write_capacity" {
  description = "Unidades de escritura (solo para modo PROVISIONED)"
  type        = number
  default     = 5
}

# ── ECS / Fargate ─────────────────────────────────────────────────────────────

variable "task_cpu" {
  description = "CPU asignada a la tarea ECS (en unidades de CPU: 256 = 0.25 vCPU)"
  type        = number
  default     = 256
}

variable "task_memory" {
  description = "Memoria asignada a la tarea ECS en MB"
  type        = number
  default     = 512
}

variable "desired_count" {
  description = "Número de instancias del contenedor a mantener corriendo"
  type        = number
  default     = 1
}

variable "container_port" {
  description = "Puerto expuesto por el contenedor Spring Boot"
  type        = number
  default     = 8080
}
