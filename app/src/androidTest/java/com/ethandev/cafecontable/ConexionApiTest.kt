//
//package com.ethandev.cafecontable
//
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.ethandev.cafecontable.data.remote.RetrofitClient
//import kotlinx.coroutines.runBlocking
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertNotNull
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//class ConexionApiTest {
//
//    @Test
//    fun probarConexionConBackend() = runBlocking {
//
//        val response = RetrofitClient.api.obtenerCompras()
//
//        println("Codigo HTTP: ${response.code()}")
//
//        assertEquals(401, response.code())
//
//        assertNotNull(response.errorBody())
//    }
//}


package com.ethandev.cafecontable

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ethandev.cafecontable.data.remote.RetrofitClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import com.ethandev.cafecontable.data.remote.LoginRequestDto
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
//import org.junit.Assert.assertNotNull

@RunWith(AndroidJUnit4::class)
class ConexionApiTest {

    @Test
    fun probarConexionConBackend() = runBlocking {

        val response = RetrofitClient.api.obtenerCompras()

        println("Codigo HTTP: ${response.code()}")

        assertEquals(401, response.code())
    }


    @Test
    fun probarLoginConBackend() = runBlocking {

        val request = LoginRequestDto(
            correo = "TU_CORREO",
            password = "TU_CONTRASEÑA"
        )

        val response = RetrofitClient.api.login(request)

        println("HTTP Login: ${response.code()}")

        assertEquals(200, response.code())

        val login = response.body()
        assertNotNull(login)

        val token = requireNotNull(login).accessToken

        assertTrue(token.isNotBlank())

        println("Login exitoso. Token recibido.")

        val comprasResponse =
            RetrofitClient.api.obtenerComprasAutenticado(
                "Bearer $token"
            )

        println("HTTP Compras: ${comprasResponse.code()}")

        assertEquals(200, comprasResponse.code())
    }

}
