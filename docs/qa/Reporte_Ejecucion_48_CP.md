# Reporte de ejecución de 48 casos de prueba — Fases 1, 2, 3 y 4

## Identificación

- Fecha: 2026-09-12, America/Lima.
- Frontend: `rama2`, commit `ac8eddc685e4a28c13ac01e149701fa14657fb8f`.
- Backend: `rama2`, commit `f34d47d439202b852193bfa4e2fadb25247127eb`.
- Modelo/FastAPI: `rama2`, commit `63db01aed3bd1e5f5fa3ea0f856e40525da0ad38`.
- Entorno: Windows local; Angular 4200, Spring Boot 8080, FastAPI 8000 y PostgreSQL real activos durante la ejecución funcional.

## Fuentes

Se verificaron `C:\Users\gmuch\Downloads\Historias_de_Usuario.xlsx` (24 HU, 48 escenarios) y `C:\Users\gmuch\Downloads\Casos_de_Prueba.xlsx` (CP001–CP048). El usuario confirmó que son los archivos vigentes aunque sus nombres físicos no contienen los sufijos indicados inicialmente.

## Resultado

- CP PASS: **48/48**.
- CP FAIL: **0/48**.
- CP BLOCKED: **0/48**.
- CP NOT IMPLEMENTED: **0/48**.
- HU cumplidas: **24/24**.
- HU parciales: **0/24**.
- HU no cumplidas: **0/24**.

## Pruebas automáticas

- Backend: `mvn test` — **79 tests, 0 fallos, 0 errores, 0 omitidos**. La suite usa H2; la prueba funcional posterior usó PostgreSQL real.
- Frontend: `npm run build` — PASS, bundle inicial 799.93 kB; warnings de budgets/CSS sin impedir el build. Tests: **11/11 PASS** con launcher temporal sin GPU; la configuración temporal se retiró.
- Modelo: suite ejecutable excluyendo dos módulos ausentes de Dataset A — **485 passed, 1 skipped, 1 warning**. La colección completa sigue bloqueada por `src.evaluar_dataset_a` y `src.preparar_dataset_a`. No se entrenó el modelo.
- Fase 3 Backend: **28/28 PASS** en las clases relacionadas y **2/2 PASS** en pruebas temporales de respuestas vacías para CP020/CP024.
- Fase 3 Frontend: **17/17 PASS** en la ejecución con seis comprobaciones temporales del Dashboard y **12/12 PASS** en la ejecución con el estado vacío del historial. Los specs y la configuración Karma temporales fueron retirados después de obtener la evidencia.
- Fase 4 Backend: `mvn test` — **85 tests, 0 fallos, 0 errores, 0 omitidos**. Las pruebas de indicadores y mapper cubren denominadores inválidos, ceros válidos y datos `0.0` legados.
- Fase 4 Frontend: `npm test -- --watch=false` — **25/25 PASS**. Incluye `null`, `undefined`, `NaN` y cero válido para ocupación, presión y promedio de estancia. La configuración Karma temporal fue retirada.
- Fase 4 build: `npm run build` — PASS, bundle inicial 800.16 kB; warnings de budgets/CSS sin impedir el build.

## Evidencia integrada principal

- FastAPI `/health` respondió 200; Frontend cargó y Backend protegió endpoints sin JWT con 401.
- Carga real D1: archivo ID 4, 88 646 filas, 190 registros válidos, 0 inválidos, 88 456 filas de otras IPRESS omitidas y 0 predicciones automáticas.
- Calidad: archivo ID 6 dejó un grupo Q05/Q06 completo pendiente antes de consolidar, con 2 registros trazados y 0 predicciones.
- Consolidación: archivos ID 8 e ID 9 redujeron dos filas del mismo grupo a una unidad consolidada por carga.
- Predicción válida: Oncología Médica 2025-06 → 2025-07, p=27.3 %, MEDIO, threshold 0.36 y alerta inactiva.
- Predicción de alerta: Traumatología 2025-08 → 2025-09, p=91.7 %, ALTO y alerta activa.
- Reportes recuperó cuatro resultados persistidos y filtró correctamente por archivo, periodo base, servicio y nivel. La tabla mostró threshold y alerta, y la exportación CSV fue confirmada físicamente.

## Resultado detallado CP001–CP048

| CP | HU | Caso | Estado | Evidencia |
|---|---|---|---|---|
| CP001 | HU001 | REGISTRO EXITOSO DE USUARIO | **PASS** | En la UI real se registró `analista.prueba@ipress.pe`; apareció «Usuario registrado correctamente», el total pasó de 4 a 5 y, tras recargar, quedó exactamente una fila activa con rol Atención de Hospitalización e IPRESS NAC. DANIEL A. CARRION. |
| CP002 | HU001 | VALIDACIÓN DE DATOS OBLIGATORIOS | **PASS** | En Configuración > Usuarios se intentó guardar el formulario vacío. La UI mostró «Ingrese el nombre completo del usuario» y no creó una cuenta. |
| CP003 | HU002 | ASIGNACIÓN VÁLIDA DE ROL E IPRESS | **PASS** | Se asignó NAC. DANIEL A. CARRION al usuario QA, se guardó, se inició sesión con esa cuenta y el acceso quedó limitado a Carga de información. |
| CP004 | HU002 | ASIGNACIÓN INCOMPLETA | **PASS** | Tras la corrección se intentó registrar «Prueba CP004 Fase 2» con rol de Admisión y sin IPRESS. La UI mostró «Seleccione una IPRESS para el rol operativo asignado» y el total permaneció en 4 usuarios; no se creó la cuenta. |
| CP005 | HU003 | INICIO DE SESIÓN EXITOSO | **PASS** | Inicio real con cuenta habilitada de Atención de Hospitalización; se mostró Dashboard y la IPRESS NAC. DANIEL A. CARRION. |
| CP006 | HU003 | CREDENCIALES INVÁLIDAS | **PASS** | Contraseña incorrecta produjo HTTP 401 y el mensaje «Credenciales inválidas o usuario inactivo». |
| CP007 | HU004 | CIERRE DE SESIÓN EXITOSO | **PASS** | La opción Salir eliminó la sesión y redirigió a /login. |
| CP008 | HU004 | ACCESO POSTERIOR AL CIERRE | **PASS** | Después de salir, el acceso directo a /dashboard redirigió a /login. |
| CP009 | HU005 | CARGA DE ARCHIVO COMPATIBLE | **PASS** | Se cargó ConsultaD1_2025_v20.csv: ID 4, DATASET_D1, 88 646 filas leídas y validación ejecutada. |
| CP010 | HU005 | ARCHIVO NO SOPORTADO O VACÍO | **PASS** | La selección de .txt fue rechazada con «Solo se permiten archivos hospitalarios .csv o .xlsx» y sin predicción. |
| CP011 | HU006 | ESTRUCTURA RECONOCIDA | **PASS** | ConsultaD1_2025_v20.csv fue reconocido como DATASET_D1 y continuó al tratamiento. |
| CP012 | HU006 | COLUMNAS OBLIGATORIAS INCORRECTAS | **PASS** | CSV con cabecera incompatible quedó como NO_RECONOCIDO, 0 registros válidos, error FORMATO_NO_RECONOCIDO y 0 predicciones. |
| CP013 | HU007 | DATOS VÁLIDOS | **PASS** | El D1 válido produjo 190 registros válidos, 0 inválidos e indicadores calculados. |
| CP014 | HU007 | DATOS INVÁLIDOS | **PASS** | Fixture con ingreso negativo: 1 fila leída, 1 inválida, FILA_D1_INVALIDA y 0 registros válidos. |
| CP015 | HU008 | CONSULTA DE HALLAZGOS DE CALIDAD | **PASS** | Fixture Q05/Q06 para IPRESS 00006218: grupo 2023-01 completo pendiente, 2 registros trazados con regla y motivo antes de consolidar. |
| CP016 | HU008 | BLOQUEO POR DATOS PENDIENTES | **PASS** | Test Backend ejecutado: `CALIDAD_PENDIENTE`, periodo retirado del catálogo y cero interacciones con FastAPI. |
| CP017 | HU009 | PROCESAMIENTO CORRECTO | **PASS** | La carga D1 válida terminó PROCESADO/VALIDADO, guardó 190 registros e indicadores, y generó 0 predicciones. |
| CP018 | HU009 | SIN REGISTROS VÁLIDOS | **PASS** | Fixtures de otra IPRESS y valor negativo terminaron con SIN_REGISTROS_VALIDOS y sin predicciones. |
| CP019 | HU010 | CONSULTA DE HISTORIAL | **PASS** | Historial real mostró archivos procesados y los nuevos IDs 3 a 9 con formato, estado, conteos y detalle de errores/advertencias. |
| CP020 | HU010 | HISTORIAL SIN REGISTROS | **PASS** | Backend devolvió lista vacía para IPRESS autorizada sin archivos; Angular mostró 0 archivos y el estado vacío sin tabla. |
| CP021 | HU011 | CONSOLIDACIÓN DE REGISTROS | **PASS** | Dos filas del mismo IPRESS-servicio-mes se consolidaron en una unidad válida; se informó la diferencia de capacidad >20 %. |
| CP022 | HU011 | CONTROL DE DUPLICIDAD | **PASS** | La misma unidad consolidada permaneció única dentro de cada carga; una repetición creó otro archivo auditado, pero no duplicó la unidad dentro del archivo. |
| CP023 | HU012 | CONSULTA CON DATOS DISPONIBLES | **PASS** | Dashboard recuperó datos observados reales para servicio y periodo seleccionados desde PostgreSQL. |
| CP024 | HU012 | CONSULTA SIN DATOS | **PASS** | Backend devolvió lista vacía para servicio/periodo sin registros y Angular mostró el estado vacío sin resultados simulados. |
| CP025 | HU013 | VISUALIZACIÓN DE INDICADORES | **PASS** | Dashboard mostró ingresos, egresos, estancias, pacientes-día, camas, días-cama, ocupación, presión y promedio de estancia. |
| CP026 | HU013 | INDICADOR NO DISPONIBLE | **PASS** | En Pediatría abril 2025, «Camas físicas habilitadas» llegó no disponible y se mostró N/D sin convertirlo en prioridad ALTA. |
| CP027 | HU014 | GRÁFICO DE INGRESOS CON DATOS | **PASS** | El gráfico de ingresos mostró seis meses ordenados y total del periodo para la ventana seleccionada. |
| CP028 | HU014 | GRÁFICO DE INGRESOS SIN DATOS | **PASS** | Test Angular renderizado: mensaje de ausencia de ingresos, cero barras y cero valores simulados. |
| CP029 | HU015 | TENDENCIA DE OCUPACIÓN DISPONIBLE | **PASS** | Se mostró tendencia mensual de ocupación y promedio para una ventana real; la referencia operacional visible es 90 %. |
| CP030 | HU015 | TENDENCIA SIN HISTORIAL SUFICIENTE | **PASS** | Test Angular renderizado: mensaje de ausencia de datos mensuales y cero puntos de ocupación. |
| CP031 | HU016 | ACCIONES SUGERIDAS CON DATOS | **PASS** | Las acciones sugeridas se alimentaron de valores reales y mostraron interpretación, responsable y aviso de apoyo no clínico. |
| CP032 | HU016 | INDICADOR NO EVALUABLE | **PASS** | Test Angular con `null`, `undefined` y `NaN`: N/D y NO EVALUABLE; ninguna prioridad ALTA por ausencia. |
| CP033 | HU017 | CONSULTA DE TRAZABILIDAD | **PASS** | La tabla real mostró probabilidad, threshold operacional y alerta. Para Pediatría abril 2025 → mayo 2025 presentó 34.0 %, 36.0 % y NO ACTIVA; el histórico contenía 4 reportes. |
| CP034 | HU017 | EXPORTACIÓN DEL RESULTADO FILTRADO | **PASS** | El usuario confirmó la descarga física de `reportes_filtrados_2026-09-12.csv`; la prueba Angular verificó además encabezados binarios, filas filtradas, threshold, alerta y ejecución del enlace de descarga. |
| CP035 | HU018 | CÁLCULO DE OCUPACIÓN | **PASS** | Pediatría abril 2025 mostró 680 pacientes-día / 720 días-cama = 94.4 % aproximadamente (94.0 % redondeado en tarjeta). |
| CP036 | HU018 | OCUPACIÓN NO CALCULABLE | **PASS** | Tras la corrección, días-cama inválidos producen `null` y Angular muestra N/D/NO EVALUABLE; pacientes-día cero con denominador válido sigue mostrando `0.0%`. |
| CP037 | HU019 | CÁLCULO DE PRESIÓN INGRESOS/CAMAS | **PASS** | Pediatría abril 2025 mostró 143 ingresos / 24 camas = 5.96. |
| CP038 | HU019 | PRESIÓN NO CALCULABLE | **PASS** | Tras la corrección, camas inválidas producen `null` y Angular muestra N/D/NO EVALUABLE; ingresos cero con camas válidas sigue mostrando `0`. |
| CP039 | HU020 | CÁLCULO DE PROMEDIO DE ESTANCIA | **PASS** | Pediatría abril 2025 mostró 640 estancias / 153 egresos = 4.18 días. |
| CP040 | HU020 | PROMEDIO DE ESTANCIA NO CALCULABLE | **PASS** | Tras la corrección, egresos inválidos producen `null` y Angular muestra N/D/NO EVALUABLE; estancias cero con egresos válidos sigue mostrando `0 días`. |
| CP041 | HU021 | DASHBOARD CON INFORMACIÓN | **PASS** | Dashboard real mostró datos observados, indicadores, gráficos, acciones y estado de predicción en una sola vista. |
| CP042 | HU021 | DASHBOARD SIN DATOS | **PASS** | Test Angular con respuestas vacías: estado informativo y ausencia de probabilidad o resultados simulados. |
| CP043 | HU022 | APLICACIÓN DE FILTROS | **PASS** | Cambiar servicio, año y mes actualizó los datos manteniendo fija la IPRESS autenticada. |
| CP044 | HU022 | LIMPIEZA DE FILTROS | **PASS** | Limpiar restableció los selectores y conservó la IPRESS asignada. |
| CP045 | HU023 | PREDICCIÓN CON VENTANA VÁLIDA | **PASS** | Oncología Médica base 2025-06 produjo 2025-07, p=27.3 %, MEDIO, threshold 36 %, alerta inactiva; Spring construyó la ventana y llamó FastAPI. |
| CP046 | HU023 | PREDICCIÓN BLOQUEADA POR VENTANA O CALIDAD | **PASS** | Tests Backend: hueco mensual → `MES_INTERMEDIO_FALTANTE`; dato pendiente → `CALIDAD_PENDIENTE`; ambos sin llamada a FastAPI. |
| CP047 | HU024 | ALERTA ACTIVA | **PASS** | Traumatología base 2025-08 produjo 2025-09, p=91.7 %, nivel ALTO y alerta ACTIVA. |
| CP048 | HU024 | ALERTA NO ACTIVA | **PASS** | Oncología Médica base 2025-06 produjo p=27.3 %, MEDIO y alerta NO ACTIVA; coherente con threshold 0.36. |

## Defectos corregidos y reejecutados en Fase 2

### CP004 / HU002 — ALTA — corregido

- Causa: Frontend y Backend no exigían IPRESS según el rol operativo.
- Corrección: validación en `usuarios.component.ts` y validación autoritativa en `UsuarioController` para Admisión y Atención de Hospitalización; Administrador permanece permitido sin IPRESS.
- Reejecución: la UI rechazó el alta sin IPRESS, mostró el mensaje esperado y no incrementó el total de usuarios.

### CP033 / HU017 — MEDIA — corregido

- Causa: `ReporteListDTO` y el modelo Angular ya contenían threshold y alerta, pero la plantilla no los renderizaba.
- Corrección: se añadieron las columnas Threshold operacional y Alerta operacional, manteniendo Backend y modelo sin cambios.
- Reejecución: 4 reportes visibles con valores coherentes; Pediatría abril 2025 mostró 34.0 %, 36.0 % y NO ACTIVA.

### CP034 / HU017 — MEDIA — corregido

- Causa probable: la URL temporal del Blob se revocaba inmediatamente, lo que podía interrumpir el consumo de la descarga según el navegador.
- Corrección: el enlace temporal se oculta, se pulsa y se conserva la URL durante 1 segundo antes de revocarla. El CSV incluye probabilidad binaria, threshold y alerta.
- Reejecución: el usuario confirmó que el archivo apareció en Descargas; la prueba Angular validó el contenido y la activación del enlace.

## Reevaluación de los 12 casos bloqueados — Fase 3

| CP | Estado anterior | Clasificación | Método utilizado | Evidencia | Estado nuevo |
|---|---|---|---|---|---|
| CP001 | BLOCKED | E. E2E / navegador | Alta real desde Angular contra Spring Boot y PostgreSQL; recarga posterior. | Mensaje de registro correcto; total 4→5; una sola fila persistente con correo, rol, IPRESS y estado correctos. El callback de éxito confirma respuesta HTTP 2xx; no se capturó el código numérico exacto. | **PASS** |
| CP016 | BLOCKED | C. Test Backend | `PrediccionBinariaAutomaticaServiceTest.pendienteCalidadNoLlegaAFastApiNiCatalogo`. | `CALIDAD_PENDIENTE`, catálogo sin el periodo y `verifyNoInteractions(modelo)`. | **PASS** |
| CP020 | BLOCKED | C+D. Tests Backend y Frontend | Repositorio de una IPRESS sin archivos y componente Historial con respuesta `[]`. | Backend devolvió lista vacía; Angular mostró 0 archivos, mensaje vacío y ninguna tabla. | **PASS** |
| CP024 | BLOCKED | C+D. Tests Backend y Frontend | Filtro Backend sin registros y Dashboard Angular con detalle vacío. | Lista vacía y mensaje «No hay datos observados para mostrar», sin probabilidad simulada. | **PASS** |
| CP028 | BLOCKED | D. Test Frontend | Dashboard renderizado con `datosActividad=[]`. | Mensaje de ausencia; cero barras `.bar-column`. | **PASS** |
| CP030 | BLOCKED | D. Test Frontend | Dashboard renderizado con `datosTendencia=[]`. | Mensaje de ausencia; cero puntos `.occupancy-point`. | **PASS** |
| CP032 | BLOCKED | D. Test Frontend | Acciones construidas con `null`, `undefined` y `NaN`. | Valores N/D, prioridades NO EVALUABLE y ninguna prioridad ALTA por ausencia. | **PASS** |
| CP036 | BLOCKED | C+D. Tests Backend y Frontend | Días-cama `null`, 0 y negativos; renderizado del resultado. | Backend guarda ocupación `0.0`; Angular muestra `0.0%`, no N/D. | **FAIL** |
| CP038 | BLOCKED | C+D. Tests Backend y Frontend | Camas `null`, 0 y negativas; renderizado del resultado. | Backend guarda presión `0.0`; Angular muestra `0`, no N/D. | **FAIL** |
| CP040 | BLOCKED | C+D. Tests Backend y Frontend | Egresos `null`, 0 y negativos; renderizado del resultado. | Backend guarda promedio `0.0`; Angular muestra `0 días`, no N/D. | **FAIL** |
| CP042 | BLOCKED | D. Test Frontend | Dashboard montado con todas las respuestas observadas vacías. | Estado vacío visible y ausencia de resultados inventados. | **PASS** |
| CP046 | BLOCKED | C. Test Backend | Casos de hueco mensual y calidad pendiente en servicio automático. | `MES_INTERMEDIO_FALTANTE` / `CALIDAD_PENDIENTE`; cero llamadas a FastAPI. | **PASS** |

No quedan casos BLOCKED ni verificaciones manuales pendientes. Las pruebas temporales se eliminaron después de ejecutarse y no forman parte del diff.

## Defectos confirmados en Fase 3

### CP036 / HU018 — MEDIA — ocupación no calculable se presenta como cero

- Esperado: N/D o mensaje de datos insuficientes cuando días-cama es cero, nulo o inválido.
- Obtenido: `IndicadorHospitalarioServiceImplements` transforma el caso en `0.0` y Dashboard lo presenta como `0.0%`.
- Componentes probables: `IndicadorHospitalarioServiceImplements` y contrato `DashboardDetalleDTO`/Angular, que no distingue cero real de cálculo no disponible.

### CP038 / HU019 — MEDIA — presión no calculable se presenta como cero

- Esperado: N/D o mensaje de datos insuficientes cuando camas totales es cero, nula o inválida.
- Obtenido: Backend transforma el caso en `0.0` y Dashboard lo presenta como `0`.
- Componentes probables: `IndicadorHospitalarioServiceImplements` y contrato `DashboardDetalleDTO`/Angular.

### CP040 / HU020 — MEDIA — promedio no calculable se presenta como cero

- Esperado: N/D o mensaje definido cuando no existen egresos válidos.
- Obtenido: Backend transforma el caso en `0.0` y Dashboard lo presenta como `0 días`.
- Componentes probables: `IndicadorHospitalarioServiceImplements` y contrato `DashboardDetalleDTO`/Angular.

## Corrección y regresión final — Fase 4

Los estados FAIL anteriores se conservan en la tabla de reevaluación de la Fase 3 como evidencia histórica. La Fase 4 corrigió la causa común sin modificar el componente predictivo:

- `IndicadorHospitalarioServiceImplements` devuelve `null` para ocupación, presión o promedio de estancia cuando su denominador es nulo, no positivo o inválido. Un numerador cero con denominador positivo conserva `0.0`.
- `DashboardServiceImplements` propaga los valores nullable y protege la lectura de ceros legados almacenados con denominadores inválidos. Los promedios sin observaciones calculables quedan en `null`.
- Angular conserva `null` en el contrato, muestra N/D para `null`, `undefined` o `NaN`, y mantiene cero como valor válido. Las acciones asociadas a datos ausentes quedan como NO EVALUABLE.
- Regresión CP035–CP040: **6/6 PASS**. CP035, CP037 y CP039 conservaron sus fórmulas válidas; CP036, CP038 y CP040 aprobaron los escenarios inválidos y la distinción de ceros válidos.
- Suites finales: Backend **85/85 PASS**, Frontend **25/25 PASS**, build Angular PASS.

Resultado final: **48/48 CP PASS**, **0 FAIL**, **0 BLOCKED**, **0 NOT IMPLEMENTED**; **24/24 HU cumplidas**, **0 parciales**, **0 no cumplidas**.

## Efectos de la auditoría en PostgreSQL

La auditoría funcional produjo datos de prueba en la base real: las cuentas `qa.incompleta.20260912@invalid.local` y `analista.prueba@ipress.pe`, ambas asociadas a NAC. DANIEL A. CARRION, y archivos auditados IDs 3–9. No se eliminaron porque la fase prohibió correcciones y una eliminación sería una modificación adicional; deben revisarse antes de limpiar el entorno.

## Restricciones respetadas

La Fase 2 modificó únicamente validación de usuarios operativos, presentación/exportación de reportes y sus tests. La Fase 3 no modificó código funcional: solo ejecutó pruebas existentes y pruebas temporales luego eliminadas, y actualizó estos documentos QA. La Fase 4 modificó únicamente la semántica nullable y presentación de tres indicadores observados y sus pruebas. No se modificaron target, clasificación binaria, threshold 0.36, ventana de seis meses, 62 características, XGBoost ni FastAPI. La carga no ejecutó inferencia. No se hizo entrenamiento, commit, push, merge ni despliegue.
