-- Create database if it doesn't exist
SELECT 'CREATE DATABASE talent_radar'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'talent_radar')\gexec

-- Connect to the database
\c talent_radar;

-- Create user if it doesn't exist
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'talent_radar_user') THEN

      CREATE USER talent_radar_user WITH PASSWORD 'talent_radar_pass';
   END IF;
END
$do$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE talent_radar TO talent_radar_user;
GRANT ALL ON SCHEMA public TO talent_radar_user;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Initial tables will be created by Hibernate