
package com.ethandev.cafecontable.data.remote.auth

import kotlinx.coroutines.sync.Mutex

object CoordinadorSesion {
    val mutex = Mutex()
}
