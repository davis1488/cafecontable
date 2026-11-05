package com.ethandev.cafecontable.data.repository

import androidx.sqlite.db.SupportSQLiteDatabase
import com.ethandev.cafecontable.data.local.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

data class CierreSemanalResumen(
    val comprasKg: Double = 0.0,
    val comprasValor: Double = 0.0,
    val comprasCantidad: Int = 0,
    val ventasKg: Double = 0.0,
    val ventasNeto: Double = 0.0,
    val ventasCantidad: Int = 0,
    val ventasBrutas: Double? = null,
    val ajustesFactor: Double? = null,
    val descuentosCooperativa: Double? = null,
    val otrosDescuentos: Double? = null,
    val desgloseVentasCompleto: Boolean = false,
    val gastosRegistrados: Double? = null,
    val gastosUtilidad: Double? = null,
    val costoCafeLiquidado: Double? = null,
    val utilidadNeta: Double? = null,
    val utilidadesCalculadas: Int = 0,
    val utilidadCompleta: Boolean = false,
    val avisos: List<String> = emptyList()
) {
    val precioPromedioCompra: Double get() = if (comprasKg > 0) comprasValor / comprasKg else 0.0
    val precioPromedioVenta: Double get() = if (ventasKg > 0) ventasNeto / ventasKg else 0.0
}

/** Reporte de movimientos por fechas, NO cierre contable definitivo.
 * El margen se muestra únicamente cuando todas las liquidaciones del período
 * están asociadas de forma inequívoca a una utilidad calculada.
 */
class CierreSemanalRepository(private val room: AppDatabase) {
    suspend fun consultar(desde: Long, hastaExclusivo: Long): CierreSemanalResumen =
        withContext(Dispatchers.IO) {
            require(hastaExclusivo > desde) { "El rango de fechas no es válido" }
            val db = room.openHelper.readableDatabase
            val avisos = mutableListOf<String>()
            fun tabla(nombre: String) = db.query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                arrayOf(nombre)
            ).use { it.moveToFirst() }
            fun columnas(nombre: String): Set<String> {
                if (!tabla(nombre)) return emptySet()
                return db.query("PRAGMA table_info(\"$nombre\")").use { c ->
                    val ix = c.getColumnIndexOrThrow("name")
                    buildSet { while (c.moveToNext()) add(c.getString(ix)) }
                }
            }
            fun elegir(cols: Set<String>, vararg nombres: String) =
                nombres.firstOrNull { it in cols }
            fun suma(sql: String, args: Array<Any>): Double = db.query(sql, args).use { c ->
                if (c.moveToFirst() && !c.isNull(0)) c.getDouble(0) else 0.0
            }
            fun conteo(sql: String, args: Array<Any>): Int = db.query(sql, args).use { c ->
                if (c.moveToFirst()) c.getInt(0) else 0
            }
            val rango = arrayOf<Any>(desde, hastaExclusivo)
            val compras = columnas("compra_cafe")
            require(compras.containsAll(setOf("fecha", "cantidad", "precioUnitCompra", "estado"))) {
                "No se encontró la estructura actual de compras"
            }
            val filtroCompras = "fecha >= ? AND fecha < ? AND UPPER(estado) != 'ANULADA'"
            val kgCompra = suma("SELECT COALESCE(SUM(cantidad),0) FROM compra_cafe WHERE $filtroCompras", rango)
            val valorCompra = suma("SELECT COALESCE(SUM(cantidad * precioUnitCompra),0) FROM compra_cafe WHERE $filtroCompras", rango)
            val nCompra = conteo("SELECT COUNT(*) FROM compra_cafe WHERE $filtroCompras", rango)

            // Las ventas deben venir de liquidaciones, nunca de anuncios no liquidados.
            val colsL = columnas("liquidacion_entrega").ifEmpty { columnas("liquidacion") }
            val tablaL = if (tabla("liquidacion_entrega")) "liquidacion_entrega" else "liquidacion"
            val fechaL = elegir(colsL, "fecha", "fechaLiquidacion")
            val valorL = elegir(colsL, "valorNeto", "valorNetoLiquidado")
            val kilosL = elegir(colsL, "cantidadKg", "cantidadLiquidada", "kilajeLiquidado")
            val idL = elegir(colsL, "id")
            val asignacionL = elegir(colsL, "asignacionId")
            var kgVenta = 0.0
            var netoVenta = 0.0
            var nVenta = 0
            val idsVentas = mutableSetOf<String>()
            val idsAsignacion = mutableSetOf<String>()
            if (fechaL != null && valorL != null && kilosL != null && idL != null) {
                db.query("SELECT \"$idL\", \"$kilosL\", \"$valorL\"" +
                        (if (asignacionL != null) ", \"$asignacionL\"" else "") +
                        " FROM \"$tablaL\" WHERE \"$fechaL\" >= ? AND \"$fechaL\" < ?", rango
                ).use { c ->
                    while (c.moveToNext()) {
                        idsVentas.add(c.getString(0))
                        kgVenta += c.getDouble(1)
                        netoVenta += c.getDouble(2)
                        if (asignacionL != null && !c.isNull(3)) idsAsignacion.add(c.getString(3))
                        nVenta++
                    }
                }
            } else {
                avisos.add("No se pudo identificar la tabla de liquidaciones. Las ventas aparecen en cero hasta ajustar sus nombres de columnas.")
            }

            // Desglose de liquidaciones: los descuentos ya están incluidos en valorNeto.
            // Este bloque es informativo: NO resta descuentos nuevamente de la utilidad.
            val camposDesglose = setOf(
                "valorBase", "ajusteFactor", "descuentoCooperativa", "otrosDescuentos"
            )
            var ventasBrutas: Double? = null
            var ajustesFactor: Double? = null
            var descuentosCooperativa: Double? = null
            var otrosDescuentos: Double? = null
            var desgloseVentasCompleto = false
            if (fechaL != null && valorL != null && camposDesglose.all { it in colsL }) {
                val filtroLiquidaciones = "\"$fechaL\" >= ? AND \"$fechaL\" < ?"
                fun totalCampo(campo: String): Double = suma(
                    "SELECT COALESCE(SUM(\"$campo\"), 0) FROM \"$tablaL\" WHERE $filtroLiquidaciones",
                    rango
                )
                ventasBrutas = totalCampo("valorBase")
                ajustesFactor = totalCampo("ajusteFactor")
                descuentosCooperativa = totalCampo("descuentoCooperativa")
                otrosDescuentos = totalCampo("otrosDescuentos")
                val netoReconstruido = ventasBrutas + ajustesFactor -
                        descuentosCooperativa - otrosDescuentos
                desgloseVentasCompleto = kotlin.math.abs(netoReconstruido - netoVenta) <=
                        (0.01 * nVenta + 1.0)
                if (!desgloseVentasCompleto) {
                    avisos.add("El desglose de liquidaciones no coincide con las ventas netas. Revisa los registros de este período.")
                }
            } else {
                avisos.add("No se pudo mostrar el desglose: faltan columnas de valor base, factor o descuentos en liquidaciones.")
            }

            // Gastos registrados EN EL PERÍODO: indicador de caja/actividad, no necesariamente
            // los gastos imputados a las ventas de ese período.
            val colsG = columnas("gasto_operacion")
            val fechaG = elegir(colsG, "fecha", "fechaRegistro", "createdAt")
            val valorG = elegir(colsG, "valor", "monto")
            val gastosRegistrados = if (fechaG != null && valorG != null) {
                suma("SELECT COALESCE(SUM(\"$valorG\"),0) FROM gasto_operacion " +
                        "WHERE \"$fechaG\" >= ? AND \"$fechaG\" < ?", rango)
            } else {
                avisos.add("Gastos por fecha: falta verificar nombres de tabla/campos de gastos.")
                null
            }

            // Utilidad SOLO si se relaciona cada venta liquidada con su utilidad calculada.
            // Nunca repartir por fechas de compra ni sumar todas las compras como costo de ventas.
            val colsU = columnas("utilidad_operacion")
            val refU = elegir(colsU, "liquidacionId", "asignacionId")
            val netoU = elegir(colsU, "utilidadNeta")
            val costoU = elegir(colsU, "costoCafe")
            val gastosU = elegir(colsU, "totalGastos")
            val ventasU = elegir(colsU, "valorVentaNeto")
            var utilidad: Double? = null
            var costo: Double? = null
            var gastosImputados: Double? = null
            var nUtilidades = 0
            var completo = false
            if (nVenta == 0) {
                avisos.add("No hay ventas liquidadas durante este período.")
            } else if (refU != null && netoU != null && costoU != null && gastosU != null && ventasU != null) {
                val pares = if (refU == "liquidacionId") idsVentas else idsAsignacion
                if (pares.size == nVenta) {
                    val valores = mutableMapOf<String, DoubleArray>()
                    db.query("SELECT \"$refU\", \"$netoU\", \"$costoU\", \"$gastosU\", \"$ventasU\" FROM utilidad_operacion").use { c ->
                        while (c.moveToNext()) {
                            if (!c.isNull(0)) {
                                val key = c.getString(0)
                                if (key in pares) {
                                    if (valores.containsKey(key)) { avisos.add("Hay utilidades duplicadas para la misma venta."); valores.clear(); break }
                                    valores[key] = doubleArrayOf(c.getDouble(1), c.getDouble(2), c.getDouble(3), c.getDouble(4))
                                }
                            }
                        }
                    }
                    nUtilidades = valores.size
                    completo = valores.size == nVenta &&
                            kotlin.math.abs(valores.values.sumOf { it[3] } - netoVenta) < (0.01 * nVenta + 1.0)
                    if (completo) {
                        utilidad = valores.values.sumOf { it[0] }
                        costo = valores.values.sumOf { it[1] }
                        gastosImputados = valores.values.sumOf { it[2] }
                    }
                }
                if (!completo) avisos.add("Utilidad pendiente de conciliación: calcula todas las utilidades y verifica que coincidan con cada liquidación.")
            } else {
                avisos.add("Utilidad no disponible: hay que confirmar cómo se vincula utilidad_operacion con la liquidación.")
            }
            CierreSemanalResumen(
                comprasKg = kgCompra, comprasValor = valorCompra, comprasCantidad = nCompra,
                ventasKg = kgVenta, ventasNeto = netoVenta, ventasCantidad = nVenta,
                ventasBrutas = ventasBrutas,
                ajustesFactor = ajustesFactor,
                descuentosCooperativa = descuentosCooperativa,
                otrosDescuentos = otrosDescuentos,
                desgloseVentasCompleto = desgloseVentasCompleto,
                gastosRegistrados = gastosRegistrados, gastosUtilidad = gastosImputados,
                costoCafeLiquidado = costo, utilidadNeta = utilidad,
                utilidadesCalculadas = nUtilidades, utilidadCompleta = completo,
                avisos = avisos.distinct()
            )
        }
}
