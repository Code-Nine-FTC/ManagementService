# ManagementService

# Deploy/Linux

export DB_URL=jdbc:postgresql://dbroute:5432/dbname
export DB_USER=username
export DB_PASSWORD=dbpassword
export SPRING_PROFILES_ACTIVE=prod

# Deploy/Windows

setx DB_URL "jdbc:postgresql://dbroute:5432/dbname"
setx DB_USER "username"
setx DB_PASSWORD "dbpassword"
setx SPRING_PROFILES_ACTIVE "prod"
