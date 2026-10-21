# Matriz de trazabilidad HU–CP — Fases 1, 2, 3 y 4

> Estado real actualizado el 12/09/2026 después de la corrección y regresión final de los tres defectos encontrados en la Fase 3. PASS exige ejecución y evidencia observable; la inspección de código por sí sola no se consideró aprobación. La evidencia del fallo inicial de CP036, CP038 y CP040 se conserva en el reporte de ejecución.

| HU | Funcionalidad | Escenario / CP | Estado | Evidencia |
|---|---|---|---|---|
| HU001 | Registrar nuevos usuarios en la plataforma | CP001 — REGISTRO EXITOSO DE USUARIO | **PASS** | En la UI real se registró `analista.prueba@ipress.pe`; apareció «Usuario registrado correctamente», el total pasó de 4 a 5 y, tras recargar, quedó exactamente una fila activa con rol Atención de Hospitalización e IPRESS NAC. DANIEL A. CARRION. |
| HU001 | Registrar nuevos usuarios en la plataforma | CP002 — VALIDACIÓN DE DATOS OBLIGATORIOS | **PASS** | En Configuración > Usuarios se intentó guardar el formulario vacío. La UI mostró «Ingrese el nombre completo del usuario» y no creó una cuenta. |
| HU002 | Asignar un rol y una IPRESS a cada usuario | CP003 — ASIGNACIÓN VÁLIDA DE ROL E IPRESS | **PASS** | Se asignó NAC. DANIEL A. CARRION al usuario QA, se guardó, se inició sesión con esa cuenta y el acceso quedó limitado a Carga de información. |
| HU002 | Asignar un rol y una IPRESS a cada usuario | CP004 — ASIGNACIÓN INCOMPLETA | **PASS** | Tras la corrección se intentó registrar «Prueba CP004 Fase 2» con rol de Admisión y sin IPRESS. La UI mostró «Seleccione una IPRESS para el rol operativo asignado» y el total permaneció en 4 usuarios; no se creó la cuenta. |
| HU003 | Iniciar sesión con mis credenciales | CP005 — INICIO DE SESIÓN EXITOSO | **PASS** | Inicio real con cuenta habilitada de Atención de Hospitalización; se mostró Dashboard y la IPRESS NAC. DANIEL A. CARRION. |
| HU003 | Iniciar sesión con mis credenciales | CP006 — CREDENCIALES INVÁLIDAS | **PASS** | Contraseña incorrecta produjo HTTP 401 y el mensaje «Credenciales inválidas o usuario inactivo». |
| HU004 | Cerrar mi sesión | CP007 — CIERRE DE SESIÓN EXITOSO | **PASS** | La opción Salir eliminó la sesión y redirigió a /login. |
| HU004 | Cerrar mi sesión | CP008 — ACCESO POSTERIOR AL CIERRE | **PASS** | Después de salir, el acceso directo a /dashboard redirigió a /login. |
| HU005 | Cargar un archivo hospitalario estructurado en formato CSV o XLSX | CP009 — CARGA DE ARCHIVO COMPATIBLE | **PASS** | Se cargó ConsultaD1_2025_v20.csv: ID 4, DATASET_D1, 88 646 filas leídas y validación ejecutada. |
| HU005 | Cargar un archivo hospitalario estructurado en formato CSV o XLSX | CP010 — ARCHIVO NO SOPORTADO O VACÍO | **PASS** | La selección de .txt fue rechazada con «Solo se permiten archivos hospitalarios .csv o .xlsx» y sin predicción. |
| HU006 | Validar la estructura y las columnas del archivo hospitalario | CP011 — ESTRUCTURA RECONOCIDA | **PASS** | ConsultaD1_2025_v20.csv fue reconocido como DATASET_D1 y continuó al tratamiento. |
| HU006 | Validar la estructura y las columnas del archivo hospitalario | CP012 — COLUMNAS OBLIGATORIAS INCORRECTAS | **PASS** | CSV con cabecera incompatible quedó como NO_RECONOCIDO, 0 registros válidos, error FORMATO_NO_RECONOCIDO y 0 predicciones. |
| HU007 | Validar la calidad de los datos hospitalarios | CP013 — DATOS VÁLIDOS | **PASS** | El D1 válido produjo 190 registros válidos, 0 inválidos e indicadores calculados. |
| HU007 | Validar la calidad de los datos hospitalarios | CP014 — DATOS INVÁLIDOS | **PASS** | Fixture con ingreso negativo: 1 fila leída, 1 inválida, FILA_D1_INVALIDA y 0 registros válidos. |
| HU008 | Consultar los hallazgos y registros pendientes de validación | CP015 — CONSULTA DE HALLAZGOS DE CALIDAD | **PASS** | Fixture Q05/Q06 para IPRESS 00006218: grupo 2023-01 completo pendiente, 2 registros trazados con regla y motivo antes de consolidar. |
| HU008 | Consultar los hallazgos y registros pendientes de validación | CP016 — BLOQUEO POR DATOS PENDIENTES | **PASS** | El test Backend ejecutado `pendienteCalidadNoLlegaAFastApiNiCatalogo` produjo `CALIDAD_PENDIENTE`, retiró el periodo del catálogo y verificó cero interacciones con el cliente FastAPI. |
| HU009 | Procesar los registros válidos y calcular indicadores hospitalarios | CP017 — PROCESAMIENTO CORRECTO | **PASS** | La carga D1 válida terminó PROCESADO/VALIDADO, guardó 190 registros e indicadores, y generó 0 predicciones. |
| HU009 | Procesar los registros válidos y calcular indicadores hospitalarios | CP018 — SIN REGISTROS VÁLIDOS | **PASS** | Fixtures de otra IPRESS y valor negativo terminaron con SIN_REGISTROS_VALIDOS y sin predicciones. |
| HU010 | Consultar el historial de archivos cargados y su estado | CP019 — CONSULTA DE HISTORIAL | **PASS** | Historial real mostró archivos procesados y los nuevos IDs 3 a 9 con formato, estado, conteos y detalle de errores/advertencias. |
| HU010 | Consultar el historial de archivos cargados y su estado | CP020 — HISTORIAL SIN REGISTROS | **PASS** | Test Backend temporal: repositorio sin archivos devolvió lista vacía para una IPRESS autorizada. Test Angular temporal: mostró «0 archivo(s) encontrado(s)» y el estado vacío, sin tabla. Ambos tests fueron retirados después de ejecutarse. |
| HU011 | Consolidar los registros por IPRESS, servicio hospitalario y mes | CP021 — CONSOLIDACIÓN DE REGISTROS | **PASS** | Dos filas del mismo IPRESS-servicio-mes se consolidaron en una unidad válida; se informó la diferencia de capacidad >20 %. |
| HU011 | Consolidar los registros por IPRESS, servicio hospitalario y mes | CP022 — CONTROL DE DUPLICIDAD | **PASS** | La misma unidad consolidada permaneció única dentro de cada carga; una repetición creó otro archivo auditado, pero no duplicó la unidad dentro del archivo. |
| HU012 | Consultar los datos observados consolidados de un servicio y periodo | CP023 — CONSULTA CON DATOS DISPONIBLES | **PASS** | Dashboard recuperó datos observados reales para servicio y periodo seleccionados desde PostgreSQL. |
| HU012 | Consultar los datos observados consolidados de un servicio y periodo | CP024 — CONSULTA SIN DATOS | **PASS** | Test Backend temporal: `/dashboard/filtro` a nivel de servicio devolvió una lista vacía para una combinación sin registros. El test Angular de Dashboard vacío confirmó el mensaje «No hay datos observados para mostrar» sin resultados simulados. |
| HU013 | Visualizar los indicadores observados principales del periodo | CP025 — VISUALIZACIÓN DE INDICADORES | **PASS** | Dashboard mostró ingresos, egresos, estancias, pacientes-día, camas, días-cama, ocupación, presión y promedio de estancia. |
| HU013 | Visualizar los indicadores observados principales del periodo | CP026 — INDICADOR NO DISPONIBLE | **PASS** | En Pediatría abril 2025, «Camas físicas habilitadas» llegó no disponible y se mostró N/D sin convertirlo en prioridad ALTA. |
| HU014 | Visualizar los ingresos por servicio hospitalario | CP027 — GRÁFICO DE INGRESOS CON DATOS | **PASS** | El gráfico de ingresos mostró seis meses ordenados y total del periodo para la ventana seleccionada. |
| HU014 | Visualizar los ingresos por servicio hospitalario | CP028 — GRÁFICO DE INGRESOS SIN DATOS | **PASS** | Test Angular renderizado con serie vacía: mostró «No hay ingresos observados para el periodo seleccionado» y confirmó cero elementos `.bar-column`. |
| HU015 | Visualizar la evolución mensual del porcentaje de ocupación | CP029 — TENDENCIA DE OCUPACIÓN DISPONIBLE | **PASS** | Se mostró tendencia mensual de ocupación y promedio para una ventana real; la referencia operacional visible es 90 %. |
| HU015 | Visualizar la evolución mensual del porcentaje de ocupación | CP030 — TENDENCIA SIN HISTORIAL SUFICIENTE | **PASS** | Test Angular renderizado sin puntos mensuales: mostró «No hay datos mensuales de ocupación para el servicio seleccionado» y confirmó cero puntos `.occupancy-point`. |
| HU016 | Consultar acciones sugeridas de revisión basadas en indicadores observados | CP031 — ACCIONES SUGERIDAS CON DATOS | **PASS** | Las acciones sugeridas se alimentaron de valores reales y mostraron interpretación, responsable y aviso de apoyo no clínico. |
| HU016 | Consultar acciones sugeridas de revisión basadas en indicadores observados | CP032 — INDICADOR NO EVALUABLE | **PASS** | Test Angular con `null`, `undefined` y `NaN`: todas las acciones afectadas mostraron N/D y prioridad NO EVALUABLE; ninguna recibió prioridad ALTA. |
| HU017 | Consultar y exportar reportes de trazabilidad de predicciones | CP033 — CONSULTA DE TRAZABILIDAD | **PASS** | La tabla real mostró probabilidad, threshold operacional y alerta. Para Pediatría abril 2025 → mayo 2025 presentó 34.0 %, 36.0 % y NO ACTIVA; el histórico contenía 4 reportes. |
| HU017 | Consultar y exportar reportes de trazabilidad de predicciones | CP034 — EXPORTACIÓN DEL RESULTADO FILTRADO | **PASS** | El usuario confirmó la descarga física de `reportes_filtrados_2026-09-12.csv`; la prueba Angular verificó además encabezados binarios, filas filtradas, threshold, alerta y ejecución del enlace de descarga. |
| HU018 | Visualizar el porcentaje de ocupación mensual observado | CP035 — CÁLCULO DE OCUPACIÓN | **PASS** | Pediatría abril 2025 mostró 680 pacientes-día / 720 días-cama = 94.4 % aproximadamente (94.0 % redondeado en tarjeta). |
| HU018 | Visualizar el porcentaje de ocupación mensual observado | CP036 — OCUPACIÓN NO CALCULABLE | **PASS** | Regresión Fase 4: días-cama nulos, cero o negativos producen ocupación `null`; el DTO conserva N/D y Angular muestra N/D/NO EVALUABLE. Pacientes-día cero con denominador positivo conserva `0.0%` como dato válido. |
| HU019 | Visualizar la presión de ingresos sobre camas registradas | CP037 — CÁLCULO DE PRESIÓN INGRESOS/CAMAS | **PASS** | Pediatría abril 2025 mostró 143 ingresos / 24 camas = 5.96. |
| HU019 | Visualizar la presión de ingresos sobre camas registradas | CP038 — PRESIÓN NO CALCULABLE | **PASS** | Regresión Fase 4: camas nulas, cero o negativas producen presión `null`; Angular muestra N/D/NO EVALUABLE. Ingresos cero con camas positivas conserva `0` como dato válido. |
| HU020 | Visualizar el promedio de estancia hospitalaria | CP039 — CÁLCULO DE PROMEDIO DE ESTANCIA | **PASS** | Pediatría abril 2025 mostró 640 estancias / 153 egresos = 4.18 días. |
| HU020 | Visualizar el promedio de estancia hospitalaria | CP040 — PROMEDIO DE ESTANCIA NO CALCULABLE | **PASS** | Regresión Fase 4: egresos nulos, cero o negativos producen promedio `null`; Angular muestra N/D/NO EVALUABLE. Estancias cero con egresos positivos conserva `0 días` como dato válido. |
| HU021 | Visualizar el dashboard predictivo del periodo seleccionado | CP041 — DASHBOARD CON INFORMACIÓN | **PASS** | Dashboard real mostró datos observados, indicadores, gráficos, acciones y estado de predicción en una sola vista. |
| HU021 | Visualizar el dashboard predictivo del periodo seleccionado | CP042 — DASHBOARD SIN DATOS | **PASS** | Test Angular renderizado con respuestas vacías: mostró «No hay datos observados para mostrar» y no mostró probabilidad ni resultados simulados. |
| HU022 | Filtrar el análisis por servicio, año y mes, manteniendo la IPRESS asignada | CP043 — APLICACIÓN DE FILTROS | **PASS** | Cambiar servicio, año y mes actualizó los datos manteniendo fija la IPRESS autenticada. |
| HU022 | Filtrar el análisis por servicio, año y mes, manteniendo la IPRESS asignada | CP044 — LIMPIEZA DE FILTROS | **PASS** | Limpiar restableció los selectores y conservó la IPRESS asignada. |
| HU023 | Solicitar y visualizar la predicción binaria de alta presión para el siguiente mes | CP045 — PREDICCIÓN CON VENTANA VÁLIDA | **PASS** | Oncología Médica base 2025-06 produjo 2025-07, p=27.3 %, MEDIO, threshold 36 %, alerta inactiva; Spring construyó la ventana y llamó FastAPI. |
| HU023 | Solicitar y visualizar la predicción binaria de alta presión para el siguiente mes | CP046 — PREDICCIÓN BLOQUEADA POR VENTANA O CALIDAD | **PASS** | Tests Backend ejecutados: un hueco mensual produjo `MES_INTERMEDIO_FALTANTE` y un periodo pendiente produjo `CALIDAD_PENDIENTE`; en ambos casos se verificó que FastAPI no recibió ninguna llamada. |
| HU024 | Recibir una alerta operacional cuando la probabilidad alcance el umbral definido | CP047 — ALERTA ACTIVA | **PASS** | Traumatología base 2025-08 produjo 2025-09, p=91.7 %, nivel ALTO y alerta ACTIVA. |
| HU024 | Recibir una alerta operacional cuando la probabilidad alcance el umbral definido | CP048 — ALERTA NO ACTIVA | **PASS** | Oncología Médica base 2025-06 produjo p=27.3 %, MEDIO y alerta NO ACTIVA; coherente con threshold 0.36. |

## Estado por historia de usuario

| HU | Resultado | Fundamento |
|---|---|---|
| HU001 | **CUMPLIDA** | CP001 PASS, CP002 PASS |
| HU002 | **CUMPLIDA** | CP003 PASS, CP004 PASS |
| HU003 | **CUMPLIDA** | CP005 PASS, CP006 PASS |
| HU004 | **CUMPLIDA** | CP007 PASS, CP008 PASS |
| HU005 | **CUMPLIDA** | CP009 PASS, CP010 PASS |
| HU006 | **CUMPLIDA** | CP011 PASS, CP012 PASS |
| HU007 | **CUMPLIDA** | CP013 PASS, CP014 PASS |
| HU008 | **CUMPLIDA** | CP015 PASS, CP016 PASS |
| HU009 | **CUMPLIDA** | CP017 PASS, CP018 PASS |
| HU010 | **CUMPLIDA** | CP019 PASS, CP020 PASS |
| HU011 | **CUMPLIDA** | CP021 PASS, CP022 PASS |
| HU012 | **CUMPLIDA** | CP023 PASS, CP024 PASS |
| HU013 | **CUMPLIDA** | CP025 PASS, CP026 PASS |
| HU014 | **CUMPLIDA** | CP027 PASS, CP028 PASS |
| HU015 | **CUMPLIDA** | CP029 PASS, CP030 PASS |
| HU016 | **CUMPLIDA** | CP031 PASS, CP032 PASS |
| HU017 | **CUMPLIDA** | CP033 PASS, CP034 PASS |
| HU018 | **CUMPLIDA** | CP035 PASS, CP036 PASS |
| HU019 | **CUMPLIDA** | CP037 PASS, CP038 PASS |
| HU020 | **CUMPLIDA** | CP039 PASS, CP040 PASS |
| HU021 | **CUMPLIDA** | CP041 PASS, CP042 PASS |
| HU022 | **CUMPLIDA** | CP043 PASS, CP044 PASS |
| HU023 | **CUMPLIDA** | CP045 PASS, CP046 PASS |
| HU024 | **CUMPLIDA** | CP047 PASS, CP048 PASS |
