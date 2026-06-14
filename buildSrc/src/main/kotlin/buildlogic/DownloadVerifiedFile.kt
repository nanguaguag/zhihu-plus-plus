package buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URL
import java.security.MessageDigest
import java.util.Base64

abstract class DownloadVerifiedFile : DefaultTask() {
    @get:Input
    abstract val url: Property<String>

    @get:Input
    abstract val sha256SRI: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun run() {
        val outFile = outputFile.get().asFile
        outFile.parentFile?.mkdirs()

        fun sha256SRI(bytes: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            return "sha256-" + Base64.getEncoder().encodeToString(digest)
        }

        val expected = sha256SRI.get()

        if (outFile.exists()) {
            val existingBytes = outFile.readBytes()
            if (sha256SRI(existingBytes) == expected) return
        }

        val downloadedBytes = URL(url.get()).readBytes()
        val actual = sha256SRI(downloadedBytes)
        check(actual == expected) {
            "File integrity mismatch: expected=$expected actual=$actual"
        }
        outFile.writeBytes(downloadedBytes)
    }
}

