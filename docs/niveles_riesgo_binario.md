# Niveles de presentación del modelo binario

El modelo XGBoost continúa siendo binario. Su salida es la probabilidad estimada
de que la ocupación del servicio supere 90% durante el siguiente periodo. Los
niveles siguientes son una interpretación operativa de esa probabilidad; no son
clases del modelo ni porcentajes de ocupación esperada.

La fuente autoritativa es `NivelRiesgoPresentacion` en Backend:

- `BAJO`: probabilidad menor que 0.20.
- `MEDIO`: probabilidad mayor o igual que 0.20 y menor que 0.36.
- `ALTO`: probabilidad mayor o igual que 0.36.

`ALTO` coincide siempre y solamente con `alerta_alta_presion = true`. La
clasificación usa la probabilidad completa recibida; el redondeo mostrado en la
interfaz no participa en la decisión. Por ejemplo, 0.35996 permanece en MEDIO
aunque visualmente pueda formatearse como 36.00%.

El umbral 0.36 es el threshold operacional seleccionado en la validación
binaria. El corte 0.20 fue seleccionado usando exclusivamente probabilidades
out-of-fold de 2018-2024. En esa validación, las prevalencias observadas fueron
aproximadamente 6.77% para BAJO, 28.81% para MEDIO y 72.76% para ALTO. Estas
tasas describen la muestra histórica de validación y no son probabilidades fijas
garantizadas para observaciones futuras.

El sistema legacy conserva su clasificación histórica independiente.
