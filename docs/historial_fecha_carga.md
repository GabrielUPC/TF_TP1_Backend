# Fecha de carga en el historial (CP019)

## Contrato temporal

- `archivo_cargado.fecha_carga` y `fechaCarga` son campos heredados `timestamp without time zone` / `LocalDateTime`. No identifican un instante ni una zona horaria.
- Las cargas nuevas guardan el instante UTC en la columna nullable `fecha_carga_utc` (`timestamp with time zone`). El API expone `fechaCargaUtc` como ISO 8601 con `Z`, por ejemplo `2026-11-01T19:37:07Z`. La columna y el campo existentes permanecen por compatibilidad.
- El historial Angular convierte solo `fechaCargaUtc` a `America/Lima`. Si el campo es nulo, muestra los componentes de `fechaCarga` tal como están, con la indicación `Zona histórica no verificada`; no realiza una conversión implícita.
- No se actualizan filas históricas. Antes de atribuirles UTC se requiere evidencia de la zona efectiva de la JVM cuando se creó cada registro. No basta con la zona actual de PostgreSQL o del contenedor.

La columna nueva es aditiva y nullable. El mapeo de `Instant` fija expresamente `SqlTypes.TIMESTAMP_WITH_TIMEZONE`, ademas del tipo SQL de la columna. No aplicar transformaciones masivas a `fecha_carga`.

## Verificacion del esquema

En `TrabajoFinal_TP1_Migrada` (Azure), `fecha_carga_utc` ya existe como
`timestamp with time zone` nullable. Los 67 registros historicos mantienen
`fecha_carga_utc = NULL`; `fecha_carga` no se modifico. El backend CP019 arranco
contra el esquema real con `spring.jpa.hibernate.ddl-auto=validate` usando una
cuenta sin permisos de escritura. No volver a ejecutar el DDL para esta publicacion.

El siguiente DDL queda solo como referencia para otros entornos donde falte la
columna; no forma parte de esta publicacion.

Para comprobar el tipo en otro entorno:

```sql
SELECT table_schema, column_name, data_type, udt_name, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' AND table_name = 'archivo_cargado'
  AND column_name IN ('fecha_carga', 'fecha_carga_utc');
```

Si `fecha_carga_utc` no existe, y se ha confirmado que la tabla objetivo es `public.archivo_cargado`, ejecutar en una ventana controlada:

```sql
BEGIN;
SET LOCAL lock_timeout = '5s';
ALTER TABLE public.archivo_cargado
  ADD COLUMN IF NOT EXISTS fecha_carga_utc timestamp with time zone;
COMMIT;
```

`IF NOT EXISTS` solo comprueba el nombre: si la columna ya existe, verificar antes y despues que `udt_name = 'timestamptz'` y `is_nullable = 'YES'`; detenerse si el tipo difiere. No se define `DEFAULT` ni se actualizan filas, de modo que las antiguas quedan con `NULL`. Antes de habilitar el backend nuevo, confirmar con `SELECT count(*) FILTER (WHERE fecha_carga_utc IS NOT NULL) FROM public.archivo_cargado;` que ninguna fila antigua tiene el campo poblado.

`spring.jpa.hibernate.ddl-auto=update` puede alterar el esquema automaticamente durante el arranque, incluso fuera de este cambio. Para esta publicacion, arrancar el backend nuevo con `ddl-auto=validate` (configuracion de despliegue autorizada), sin permitir `update`. La compatibilidad con PostgreSQL real se comprobo mediante el arranque local con la cuenta temporal de solo lectura; las pruebas H2 no sustituyen esa comprobacion.

Orden de publicacion: frontend compatible con ambos contratos; backend nuevo con validacion de esquema; prueba de carga nueva e historial. En caso de fallo, volver a las imagenes anteriores. Dejar la columna adicional intacta durante la reversion: eliminarla podria perder instantes de cargas nuevas y requiere otra autorizacion.

## Datos de reproducción QA

La cuenta preparada para reproducir CP019 es `qa.admision.a@ipress.test`, con rol Admision y Registros Medicos y asociada a `IPRESS QA SINTETICA`. El historial del archivo `d1_datos_validos.csv` (ID 38 en esta ejecucion, no identificador universal) muestra `QA Admisión A`, `VALIDADO` y `PROCESADO`. Estos datos pertenecen al entorno QA y no reemplazan la cuenta `admision@ipress.gob.pe` ni el RENIPRESS `00007636` del escenario CP019 original. La disponibilidad y comportamiento de esa cuenta y RENIPRESS originales no se han verificado.

El registro 38 contiene `2026-11-01 19:37:07.998074`. Si se demuestra que se escribió en UTC, corresponde a `01/11/2026 14:37` en Lima. Hasta verificar esa procedencia, la interfaz no debe desplazar su hora cinco horas ni presentarla como hora peruana confirmada.
