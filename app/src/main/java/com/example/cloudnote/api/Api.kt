package com.example.cloudnote.api

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class Api
{
    companion object
    {
        private const val BASE_URL = "http://{URL}/api"
        fun get(path: String, accessToken: String? = null): ApiResponse
        {
            try
            {
                val url = "$BASE_URL/$path"
                val connection = URL(url).openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                connection.doInput = true
                connection.doOutput = true

                val reader =
                    if (connection.responseCode == HttpURLConnection.HTTP_OK) connection.inputStream.bufferedReader() else connection.errorStream.bufferedReader()
                val response = reader.readText()

                reader.close()

                return ApiResponse(connection.responseCode, response)
            }
            catch (e: IOException)
            {
                e.printStackTrace()
                return ApiResponse(403, "{ \"message\": \"${e.message?.toString()}\"}")
            }
        }

        fun post(path: String, data: String,accessToken: String? = null): ApiResponse
        {
            try
            {
                val url = "$BASE_URL/$path"
                val connection = URL(url).openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                connection.doInput = true
                connection.doOutput = true

                val writer = connection.outputStream.bufferedWriter()

                writer.write(data)
                writer.close()

                val reader =
                    if (connection.responseCode == HttpURLConnection.HTTP_OK) connection.inputStream.bufferedReader() else connection.errorStream.bufferedReader()
                val response = reader.readText()

                reader.close()

                return ApiResponse(connection.responseCode, response)
            }
            catch (e: IOException)
            {
                e.printStackTrace()
                return ApiResponse(403, "{ \"message\": \"${e.message?.toString()}\"}")
            }
        }
    }
}