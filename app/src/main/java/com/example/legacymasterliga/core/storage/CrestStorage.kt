package com.example.legacymasterliga.core.storage

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrestStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val crestsDir = File(context.filesDir, "crests").apply { mkdirs() }

    /**
     * Copia uma imagem de uma URI externa para o armazenamento interno.
     * Se a URI já for local, retorna a própria URI.
     * @return URI da cópia interna em formato String.
     */
    fun saveCrest(uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null
        val uri = Uri.parse(uriString)
        
        // Se já for um arquivo local na pasta de escudos, não faz nada
        if (isLocal(uriString)) return uriString

        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "png"
            
            val destFile = File(crestsDir, "crest_${UUID.randomUUID()}.$extension")
            
            val inputStream = contentResolver.openInputStream(uri) 
                ?: throw Exception("Não foi possível abrir o escudo original.")
            
            inputStream.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (destFile.length() == 0L) {
                destFile.delete()
                throw Exception("O arquivo do escudo está vazio.")
            }

            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            throw Exception("Falha ao copiar escudo: ${e.message}")
        }
    }

    /**
     * Remove o arquivo físico se ele pertencer à pasta interna de escudos.
     */
    fun deleteIfLocal(uriString: String?) {
        if (isLocal(uriString)) {
            val path = Uri.parse(uriString).path
            if (path != null) {
                File(path).delete()
            }
        }
    }

    /**
     * Verifica se a URI aponta para um arquivo dentro da pasta interna de escudos.
     */
    fun isLocal(uriString: String?): Boolean {
        if (uriString.isNullOrBlank()) return false
        return try {
            val uri = Uri.parse(uriString)
            uri.scheme == "file" && uri.path?.contains("/files/crests/") == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retorna a pasta interna de escudos.
     */
    fun getCrestsDirectory(): File = crestsDir
}
