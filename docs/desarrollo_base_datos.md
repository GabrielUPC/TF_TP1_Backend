# Base de datos de desarrollo

Spring Boot usa ddl-auto=update para crear o actualizar tablas dentro de una base
existente. Esa opcion no crea la base PostgreSQL.

## Variables de entorno

Configure localmente, fuera de Git:

- DB_URL=jdbc:postgresql://localhost:5432/TrabajoFinal_TP1
- DB_USERNAME=<usuario local>
- DB_PASSWORD=<contrasena local>
- JWT_SECRET=<secreto largo y aleatorio>
- MODELO_IPRESS_URL=http://localhost:8000

Las credenciales y el secreto que alguna vez estuvieron publicados deben rotarse.

## Reconstruccion reproducible

1. Inicie PostgreSQL.
2. Cree la base con CREATE DATABASE TrabajoFinal_TP1.
3. Exporte las cinco variables de entorno.
4. Inicie el backend. Hibernate creara las tablas del esquema actual.
5. Cree o siembre los roles ADMINISTRADOR, ADMISION_REGISTROS y ATENCION_HOSPITALIZACION.
6. Cree la IPRESS con RENIPRESS canonico de ocho digitos.
7. Cree usuarios de prueba y asigne cada usuario hospitalario a una IPRESS.
8. Inicie FastAPI en el valor de MODELO_IPRESS_URL.
9. Cargue el historico D1 mediante el usuario de admision.
10. Ingrese como usuario hospitalario y analice un periodo ofrecido por el catalogo binario.

La reconstruccion es recomendable porque prediccion_riesgo cambio de un esquema
multiclase legacy al contrato binario. No ejecute DROP DATABASE desde la aplicacion:
respalde lo necesario y haga la operacion manualmente en desarrollo.

## Tablas esperadas

rol, ipress, usuario, archivo_cargado, registro_hospitalario,
indicador_hospitalario, prediccion_riesgo, reporte, hallazgo_calidad_datos y
registro_pendiente_calidad_datos.

No se crea una tabla de servicios: el catalogo RAW demostro que id_hospitalizacion
no identifica de manera estable un unico servicio. La clave predictiva longitudinal
es el texto canonico del servicio y el ID se conserva para validar 24xx/25xx.
