package com.roastos.app

interface RoastAiProvider {

    suspend fun generate(
        request: RoastAiRequest
    ): RoastAiResponse

}
