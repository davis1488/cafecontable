package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.session.SessionManager
import com.ethandev.cafecontable.data.remote.AuthTokenManager
import com.ethandev.cafecontable.data.remote.CafeContableApi
import com.ethandev.cafecontable.data.remote.dto.SincronizarCompraRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

data class ResultadoSyncCompras(
    val sincronizadas: Int = 0,
    val conflictos: Int = 0,
    val pendientes: Int = 0
)

class SincronizarComprasPendientes(
    private val db: AppDatabase,
    private val sessionManager: SessionManager,
    private val api: CafeContableApi
) {
    companion object {
        // Evita ejecuciones simultaneas dentro del proceso Android.
        private val mutex = Mutex()
    }

    private val gson = Gson()

    suspend operator fun invoke(): ResultadoSyncCompras = mutex.withLock {
        val sesion = sessionManager.usuarioSesion.first()
            ?: throw IllegalStateException("Debes iniciar sesion")

        if (AuthTokenManager.obtenerToken().isNullOrBlank()) {
            throw IllegalStateException(
                "Necesitas iniciar sesion online para sincronizar"
            )
        }

        val dao = db.operacionPendienteSyncDao()
        val pendientes = dao.obtenerPendientes(sesion.empresaId)
        var sincronizadas = 0
        var conflictos = 0
        var sinEnviar = 0

        for (pendiente in pendientes) {
            if (pendiente.empresaId != sesion.empresaId ||
                pendiente.usuarioId != sesion.usuarioId ||
                pendiente.tipoOperacion != "COMPRA"
            ) {
                sinEnviar++
                continue
            }

            // El JSON de la compra se guarda en Room para reintentos estables.
            val request = try {
                val json: JsonObject = gson.fromJson(
                    pendiente.payloadJson,
                    JsonObject::class.java
                ) ?: throw IllegalArgumentException("JSON no valido")

                fun tieneValor(campo: String): Boolean =
                    json.has(campo) && !json.get(campo).isJsonNull

                // Primero validamos los datos originales de la compra.
                val obligatoriosCompra = listOf(
                    "operacionSyncId", "compraLocalId", "productoNombre",
                    "unidad", "cantidad", "precioUnitCompra",
                    "esCredito", "abono", "operacionCompraId"
                )
                require(obligatoriosCompra.all(::tieneValor)) {
                    "Faltan datos originales de la compra"
                }
                require(
                    json.get("operacionSyncId").asString.equals(
                        pendiente.id, ignoreCase = true
                    )
                ) { "El UUID no coincide con Room" }
                require(
                    json.get("compraLocalId").asString == pendiente.referenciaLocal
                ) { "La referencia local no coincide" }

                val operacionId = json.get("operacionCompraId").asString
                val camposOperacion = listOf(
                    "operacionNombre", "operacionTipo", "operacionFecha",
                    "operacionEstado"
                )

                // Reparacion puntual de payloads anteriores a los nuevos campos.
                if (camposOperacion.any { !tieneValor(it) }) {
                    val operacion = db.operacionDao().obtenerPorId(operacionId)
                        ?: throw IllegalStateException(
                            "No se encontro la operacion local $operacionId"
                        )

                    require(operacion.id == operacionId && operacion.tipo == "COMPRA") {
                        "La operacion local no es de tipo COMPRA"
                    }

                    if (!tieneValor("operacionNombre")) {
                        json.addProperty("operacionNombre", operacion.nombre)
                    }
                    if (!tieneValor("operacionTipo")) {
                        json.addProperty("operacionTipo", operacion.tipo)
                    }
                    if (!tieneValor("operacionDescripcion")) {
                        json.addProperty("operacionDescripcion", operacion.descripcion)
                    }
                    if (!tieneValor("operacionFecha")) {
                        json.addProperty("operacionFecha", operacion.fecha)
                    }
                    if (!tieneValor("operacionEstado")) {
                        json.addProperty("operacionEstado", operacion.estado.name)
                    }
                }

                require(camposOperacion.all(::tieneValor)) {
                    "Datos de operacion incompletos"
                }
                val payloadCompleto = gson.toJson(json)
                val dto: SincronizarCompraRequest = gson.fromJson(
                    payloadCompleto,
                    SincronizarCompraRequest::class.java
                )
                require(dto.unidad.equals("KG", ignoreCase = true)) {
                    "Esta version solo sincroniza KG"
                }
                require(
                    dto.operacionTipo == "COMPRA" &&
                            !dto.operacionNombre.isNullOrBlank()
                ) { "Datos de operacion invalidos" }

                // Persistir la reparacion ANTES del envio. Los reintentos
                // deben usar el mismo payload para que coincida PayloadHash.
                if (payloadCompleto != pendiente.payloadJson) {
                    dao.actualizarPayload(pendiente.id, payloadCompleto)
                }
                dto
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                dao.actualizarEstado(
                    pendiente.id,
                    "PENDIENTE",
                    "Revision necesaria: ${e.message}"
                )
                sinEnviar++
                continue
            }

            dao.incrementarIntentos(pendiente.id)
            try {
                val respuesta = api.sincronizarCompra(request)
                when {
                    respuesta.isSuccessful -> {
                        val resultado = respuesta.body()
                        if (resultado != null &&
                            resultado.operacionSyncId.equals(pendiente.id, ignoreCase = true) &&
                            resultado.compraId > 0 &&
                            resultado.estado == "SINCRONIZADO"
                        ) {
                            dao.actualizarEstado(pendiente.id, "SINCRONIZADO", null)
                            sincronizadas++
                        } else {
                            dao.actualizarEstado(
                                pendiente.id, "PENDIENTE",
                                "Respuesta de servidor invalida"
                            )
                            sinEnviar++
                        }
                    }
                    respuesta.code() == 409 -> {
                        dao.actualizarEstado(
                            pendiente.id, "CONFLICTO",
                            "Conflicto HTTP 409: requiere revision"
                        )
                        conflictos++
                    }
                    respuesta.code() == 401 || respuesta.code() == 403 -> {
                        dao.actualizarEstado(
                            pendiente.id, "PENDIENTE",
                            "Autorizacion requerida: HTTP ${respuesta.code()}"
                        )
                        sinEnviar++
                        break
                    }
                    respuesta.code() == 400 || respuesta.code() == 422 -> {
                        dao.actualizarEstado(
                            pendiente.id, "REQUIERE_REVISION",
                            "Solicitud rechazada: HTTP ${respuesta.code()}"
                        )
                        conflictos++
                    }
                    else -> {
                        dao.actualizarEstado(
                            pendiente.id, "PENDIENTE",
                            "Error HTTP ${respuesta.code()}"
                        )
                        sinEnviar++
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                dao.actualizarEstado(
                    pendiente.id, "PENDIENTE", "Sin conexion con el servidor"
                )
                sinEnviar++
            } catch (e: Exception) {
                dao.actualizarEstado(
                    pendiente.id, "PENDIENTE",
                    "Error de sincronizacion: ${e.message}"
                )
                sinEnviar++
            }
        }

        ResultadoSyncCompras(
            sincronizadas = sincronizadas,
            conflictos = conflictos,
            pendientes = sinEnviar
        )
    }
}
