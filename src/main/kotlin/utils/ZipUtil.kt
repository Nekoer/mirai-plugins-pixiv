package com.hcyacg.utils

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


object ZipUtil {
//    fun testZipFile() {
//        val f1 = File("./build/ziptest/中文.txt")
//        createFile(f1.path)
//        val f2 = File("./build/ziptest/2.txt")
//        createFile(f2.path)
//        val f3 = File("./build/ziptest/3.txt")
//        createFile(f3.path)
//
//        zip(listOf(f1, f2, f3), "./build/ziptest.zip")
//
//        val res = "./build/zipresult"
//        unzip("./build/ziptest.zip", res)
//
//    }


    fun unzip(zipFile: String, descDir: String) {
        val buffer = ByteArray(1024)
        var outputStream: OutputStream? = null
        var inputStream: InputStream? = null
        try {
            val zf = ZipFile(zipFile)
            val entries = zf.entries()
            while (entries.hasMoreElements()) {
                val zipEntry: ZipEntry = entries.nextElement() as ZipEntry
                val zipEntryName: String = zipEntry.name

                inputStream = zf.getInputStream(zipEntry)
                val descFilePath: String = descDir + File.separator + zipEntryName
                val descFile: File = createFile(descFilePath)
                outputStream = FileOutputStream(descFile)

                var len: Int
                while (inputStream.read(buffer).also { len = it } > 0) {
                    outputStream.write(buffer, 0, len)
                }
                inputStream.close()
                outputStream.close()
            }
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

     private fun createFile(filePath: String): File {
        val file = File(filePath)
        val parentFile = file.parentFile!!
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    fun zip(files: List<File>, zipFilePath: String) {
        if (files.isEmpty()) return

        val zipFile = createFile(zipFilePath)
        val buffer = ByteArray(1024)
        var zipOutputStream: ZipOutputStream? = null
        var inputStream: FileInputStream? = null
        try {
            zipOutputStream = ZipOutputStream(FileOutputStream(zipFile))
            for (file in files) {
                if (!file.exists()) continue
                zipOutputStream.putNextEntry(ZipEntry(file.name))
                inputStream = FileInputStream(file)
                var len: Int
                while (inputStream.read(buffer).also { len = it } > 0) {
                    zipOutputStream.write(buffer, 0, len)
                }
                zipOutputStream.closeEntry()
            }
        } finally {
            inputStream?.close()
            zipOutputStream?.close()
        }
    }

    fun zipByFolder(fileDir: String, zipFilePath: String) {
        val folder = File(fileDir)
        if (folder.exists() && folder.isDirectory) {
            val files = folder.listFiles()
            val filesList: List<File> = files.toList()
            zip(filesList, zipFilePath)
        }
    }
}