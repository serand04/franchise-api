output "dynamodb_table_name" {
  description = "Nombre de la tabla DynamoDB"
  value       = aws_dynamodb_table.franchises.name
}

output "dynamodb_table_arn" {
  description = "ARN de la tabla DynamoDB"
  value       = aws_dynamodb_table.franchises.arn
}

output "ecr_repository_url" {
  description = "URL del repositorio ECR para hacer push de la imagen Docker"
  value       = aws_ecr_repository.franchise_api.repository_url
}

output "ecs_cluster_name" {
  description = "Nombre del cluster ECS"
  value       = aws_ecs_cluster.main.name
}

output "ecs_service_name" {
  description = "Nombre del servicio ECS"
  value       = aws_ecs_service.franchise_api.name
}

output "alb_dns_name" {
  description = "DNS público del Application Load Balancer (URL de la API)"
  value       = "http://${aws_lb.main.dns_name}"
}

output "swagger_ui_url" {
  description = "URL de la documentación Swagger UI"
  value       = "http://${aws_lb.main.dns_name}/swagger-ui.html"
}

output "aws_account_id" {
  description = "ID de la cuenta AWS utilizada"
  value       = data.aws_caller_identity.current.account_id
}
